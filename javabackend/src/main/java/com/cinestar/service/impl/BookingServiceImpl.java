package com.cinestar.service.impl;

import com.cinestar.dto.request.CreateBookingRequest;
import com.cinestar.dto.response.BookingResponse;
import com.cinestar.entity.*;
import com.cinestar.entity.enums.BookingStatus;
import com.cinestar.entity.enums.SeatBookingStatus;
import com.cinestar.exception.BadRequestException;
import com.cinestar.exception.ConflictException;
import com.cinestar.exception.ResourceNotFoundException;
import com.cinestar.kafka.BookingEvent;
import com.cinestar.kafka.BookingEventProducer;
import com.cinestar.mapper.BookingMapper;
import com.cinestar.repository.*;
import com.cinestar.service.BookingService;
import com.cinestar.service.SeatHoldService;
import com.cinestar.util.SeatPriceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter SHOWTIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final ComboRepository comboRepository;
    private final UserRepository userRepository;
    private final SeatHoldService seatHoldService;
    private final BookingEventProducer bookingEventProducer;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {
        Showtime showtime = showtimeRepository.findWithDetailsById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy suất chiếu id=" + request.getShowtimeId()));

        if (!showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Suất chiếu đã bắt đầu hoặc đã kết thúc");
        }

        List<Long> seatIds = request.getSeatIds().stream().distinct().toList();
        List<Seat> seats = seatRepository.findAllById(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new BadRequestException("Có ghế không tồn tại trong hệ thống");
        }
        for (Seat seat : seats) {
            if (!seat.getRoom().getId().equals(showtime.getRoom().getId())) {
                throw new BadRequestException(
                        "Ghế " + seat.displayName() + " không thuộc phòng chiếu của suất này");
            }
        }

        // ============ LOP 1: GIU GHE BANG REDIS (SET NX + TTL 5 phut) ============
        // Neu 2 user cung bam 1 ghe cung luc: chi 1 nguoi SET NX thanh cong.
        List<Long> acquired = new ArrayList<>();
        try {
            for (Long seatId : seatIds) {
                if (!seatHoldService.tryHold(showtime.getId(), seatId, userId)) {
                    String seatName = seats.stream()
                            .filter(s -> s.getId().equals(seatId))
                            .findFirst()
                            .map(Seat::displayName)
                            .orElse(String.valueOf(seatId));
                    throw new ConflictException("Ghế " + seatName
                            + " đang được người khác giữ, vui lòng chọn ghế khác");
                }
                acquired.add(seatId);
            }
        } catch (ConflictException ex) {
            seatHoldService.releaseAll(showtime.getId(), acquired);
            throw ex;
        }

        // ============ LOP 2: KIEM TRA TRONG DATABASE ============
        // Ghe co the da duoc ban boi don khac ma Redis hold da het han (2 che do
        // khong dong bo). Neu phat hien -> nha hold vua lay va bao xung dot.
        if (bookingSeatRepository.existsActiveSeats(showtime.getId(), seatIds,
                List.of(SeatBookingStatus.HELD, SeatBookingStatus.BOOKED), null)) {
            seatHoldService.releaseAll(showtime.getId(), acquired);
            throw new ConflictException("Ghế vừa được người khác đặt, vui lòng chọn ghế khác");
        }

        // ============ TAO DON + BOOKING SEATS (status=HELD) ============
        BigDecimal totalAmount = BigDecimal.ZERO;
        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .user(userRepository.getReferenceById(userId))
                .showtime(showtime)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .expiresAt(LocalDateTime.now().plusMinutes(seatHoldService.getHoldMinutes()))
                .build();

        for (Seat seat : seats) {
            BigDecimal price = SeatPriceCalculator.priceOf(showtime.getPriceBase(), seat.getSeatType());
            totalAmount = totalAmount.add(price);
            booking.getSeats().add(BookingSeat.builder()
                    .booking(booking)
                    .seat(seat)
                    .showtime(showtime)
                    .price(price)
                    .status(SeatBookingStatus.HELD)
                    .build());
        }

        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                Combo combo = comboRepository.findById(itemReq.getComboId())
                        .orElseThrow(() -> new BadRequestException(
                                "Không tìm thấy combo id=" + itemReq.getComboId()));
                BigDecimal subtotal = combo.getPrice()
                        .multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                totalAmount = totalAmount.add(subtotal);
                booking.getItems().add(BookingItem.builder()
                        .booking(booking)
                        .combo(combo)
                        .quantity(itemReq.getQuantity())
                        .price(combo.getPrice())
                        .build());
            }
        }

        booking.setTotalAmount(totalAmount);
        booking = bookingRepository.save(booking);

        /**
         * LOP 3 (chot ha): bang booking_seats co UNIQUE PARTIAL INDEX
         * (showtime_id, seat_id) WHERE status IN ('HELD','BOOKED'). Neu 2 request
         * lot qua ca Redis va kiem tra tren (doi toc dieu kien rac), INSERT
         * booking_seats van bi database tu choi -> DataIntegrityViolationException
         * -> GlobalExceptionHandler tra 409 "Ghế vừa được người khác đặt".
         */

        log.info("User {} dat ve {} - {} ghe - tam {} VND - giu den {}",
                userId, booking.getBookingCode(), seatIds.size(), totalAmount, booking.getExpiresAt());
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getByCode(Long userId, String bookingCode) {
        Booking booking = mustGetBookingOfUser(userId, bookingCode);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long userId, String bookingCode) {
        Booking booking = mustGetBookingOfUser(userId, bookingCode);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Đơn vé đã bị hủy trước đó");
        }
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Đơn vé đã hết hạn");
        }
        if (booking.getShowtime().getStartTime()
                .isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("Chỉ có thể hủy vé trước giờ chiếu ít nhất 1 tiếng");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.getSeats().forEach(bs -> bs.setStatus(SeatBookingStatus.CANCELLED));

        // Nha ghe Redis (neu con)
        List<Long> seatIds = booking.getSeats().stream().map(bs -> bs.getSeat().getId()).toList();
        seatHoldService.releaseAll(booking.getShowtime().getId(), seatIds);

        BookingEvent event = buildEvent(booking, BookingEvent.TYPE_CANCELLED);
        bookingEventProducer.sendBookingEvent(event);

        log.info("User {} huy don {}", userId, bookingCode);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse confirmBookingPayment(String bookingCode, String vnpTransactionNo,
                                                 String vnpResponseCode, String vnpBankCode, String vnpPayDate) {
        Booking booking = bookingRepository.findWithDetailsByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn vé: " + bookingCode));

        // Idempotent: IPN va Return co the goi nhieu lan
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return bookingMapper.toResponse(booking);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Đơn vé không ở trạng thái chờ thanh toán");
        }
        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.getSeats().forEach(bs -> bs.setStatus(SeatBookingStatus.CANCELLED));
            throw new BadRequestException("Đơn vé đã hết thời gian giữ ghế");
        }

        List<Long> seatIds = booking.getSeats().stream().map(bs -> bs.getSeat().getId()).toList();

        // ============ LOP 2 (LAN CUOI): KHOA BI QUAN CAC GHE ============
        // SELECT ... FOR UPDATE: hai giao dich thanh toan tranh cung ghe se
        // chay tuan tu; giao dich sau thay ghese da BOOKED -> bao xung dot.
        seatRepository.findAllByIdForUpdate(seatIds);

        boolean conflict = bookingSeatRepository.existsActiveSeats(booking.getShowtime().getId(), seatIds,
                List.of(SeatBookingStatus.HELD, SeatBookingStatus.BOOKED), booking.getId());
        if (conflict) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getSeats().forEach(bs -> bs.setStatus(SeatBookingStatus.CANCELLED));
            seatHoldService.releaseAll(booking.getShowtime().getId(), seatIds);
            log.warn("XUNG DOT GHE khi xac nhan don {} - ghe da co nguoi khac dat", bookingCode);
            throw new ConflictException("Ghế đã được người khác đặt trước. Tiền sẽ được hoàn lại.");
        }

        // ============ THANH CONG: BOOKED + CONFIRMED ============
        booking.getSeats().forEach(bs -> bs.setStatus(SeatBookingStatus.BOOKED));
        booking.setStatus(BookingStatus.CONFIRMED);
        Payment payment = booking.getPayment();
        if (payment != null) {
            payment.setStatus(com.cinestar.entity.enums.PaymentStatus.SUCCESS);
            payment.setVnpTransactionNo(vnpTransactionNo);
            payment.setVnpResponseCode(vnpResponseCode);
            payment.setVnpBankCode(vnpBankCode);
            payment.setVnpPayDate(vnpPayDate);
            payment.setPaidAt(LocalDateTime.now());
        }

        // Thanh toan xong -> nha ghe Redis (ghese da la BOOKED trong DB, khong can hold nua)
        seatHoldService.releaseAll(booking.getShowtime().getId(), seatIds);

        // ============ KAFKA: phat su kien CONFIRMED ============
        // Email ve + xoa cache duoc xu ly BAT DONG BO boi consumer,
        // API tra ve user ngay lap tuc.
        bookingEventProducer.sendBookingEvent(buildEvent(booking, BookingEvent.TYPE_CONFIRMED));

        log.info("Da xac nhan thanh toan don {} - {} VND", bookingCode, booking.getTotalAmount());
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    @Transactional
    public void expirePendingBookings() {
        List<Booking> expired = bookingRepository
                .findByStatusAndExpiresAtBefore(BookingStatus.PENDING, LocalDateTime.now());
        for (Booking booking : expired) {
            booking.setStatus(BookingStatus.EXPIRED);
            booking.getSeats().forEach(bs -> bs.setStatus(SeatBookingStatus.CANCELLED));
            List<Long> seatIds = booking.getSeats().stream().map(bs -> bs.getSeat().getId()).toList();
            seatHoldService.releaseAll(booking.getShowtime().getId(), seatIds);
            log.info("Don {} het han giu ghe - da nha {} ghe", booking.getBookingCode(), seatIds.size());
        }
    }

    // ================== Helper ==================

    private Booking mustGetBookingOfUser(Long userId, String bookingCode) {
        Booking booking = bookingRepository.findWithDetailsByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn vé: " + bookingCode));
        // Phan quyen du lieu: user chi xem duoc don cua chinh minh
        if (!booking.getUser().getId().equals(userId)) {
            throw new com.cinestar.exception.UnauthorizedException("Bạn không có quyền xem đơn vé này");
        }
        return booking;
    }

    private String generateBookingCode() {
        long stamp = System.currentTimeMillis() % 100_000_000_000L;
        int random = RANDOM.nextInt(90_000) + 10_000;
        return "CS" + stamp + random;
    }

    private BookingEvent buildEvent(Booking booking, String eventType) {
        Showtime showtime = booking.getShowtime();
        return new BookingEvent(
                eventType,
                booking.getBookingCode(),
                booking.getUser().getFullName(),
                booking.getUser().getEmail(),
                showtime.getMovie().getTitle(),
                showtime.getRoom().getCinema().getName(),
                showtime.getRoom().getName(),
                showtime.getStartTime().format(SHOWTIME_FORMATTER),
                booking.getSeats().stream()
                        .map(bs -> bs.getSeat().displayName())
                        .toList(),
                booking.getTotalAmount(),
                LocalDateTime.now());
    }
}
