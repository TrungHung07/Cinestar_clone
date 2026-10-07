package com.cinestar.service.impl;

import com.cinestar.dto.request.ShowtimeRequest;
import com.cinestar.dto.response.SeatLayoutResponse;
import com.cinestar.dto.response.SeatResponse;
import com.cinestar.dto.response.ShowtimeResponse;
import com.cinestar.entity.Movie;
import com.cinestar.entity.Room;
import com.cinestar.entity.Seat;
import com.cinestar.entity.Showtime;
import com.cinestar.entity.enums.SeatBookingStatus;
import com.cinestar.exception.BadRequestException;
import com.cinestar.exception.ResourceNotFoundException;
import com.cinestar.mapper.ShowtimeMapper;
import com.cinestar.repository.BookingSeatRepository;
import com.cinestar.repository.MovieRepository;
import com.cinestar.repository.RoomRepository;
import com.cinestar.repository.SeatRepository;
import com.cinestar.repository.ShowtimeRepository;
import com.cinestar.service.SeatHoldService;
import com.cinestar.service.ShowtimeService;
import com.cinestar.util.SeatPriceCalculator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final MovieRepository movieRepository;
    private final SeatHoldService seatHoldService;
    private final ShowtimeMapper showtimeMapper;
    private final EntityManager entityManager;

    @Override
    @Cacheable(value = "showtimes",
            key = "(#movieId ?: 'all') + ':' + (#cinemaId ?: 'all') + ':' + #dateIso")
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimes(Long movieId, Long cinemaId, String dateIso) {
        log.info("[CACHE MISS] Truy van DB: lich chieu movie={} cinema={} date={}", movieId, cinemaId, dateIso);
        LocalDate date;
        try {
            date = LocalDate.parse(dateIso);
        } catch (Exception ex) {
            throw new BadRequestException("Ngày không hợp lệ, định dạng phải là yyyy-MM-dd");
        }

        /**
         * CHONG N+1: findWithDetails la JOIN FETCH 1 query lay ca
         * suat chieu + phim + phong + rap. Neu viet "showtime.getMovie().getTitle()"
         * moi vong lap ma khong fetch truoc se sinh ra 1 + 3N query.
         */
        List<Showtime> showtimes = showtimeRepository.findWithDetails(
                movieId, cinemaId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        return showtimes.stream().map(showtimeMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SeatLayoutResponse getSeatLayout(Long showtimeId, Long currentUserId) {
        Showtime showtime = showtimeRepository.findWithDetailsById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất chiếu id=" + showtimeId));

        // Toan bo ghe cua phong (bo cuc tinh - co the cache them neu can)
        List<Seat> roomSeats = seatRepository
                .findByRoomIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(showtime.getRoom().getId());

        // Ghe da giu/ban trong database - join fetch booking + user de biet ai giu (1 query)
        Map<Long, SeatBookingStatus> dbStatuses = new HashMap<>();
        Map<Long, Long> dbHolders = new HashMap<>();
        for (var bs : bookingSeatRepository.findActiveByShowtimeIdWithHolder(showtimeId,
                List.of(SeatBookingStatus.HELD, SeatBookingStatus.BOOKED))) {
            dbStatuses.put(bs.getSeat().getId(), bs.getStatus());
            dbHolders.put(bs.getSeat().getId(), bs.getBooking().getUser().getId());
        }

        List<SeatResponse> seatResponses = new ArrayList<>();
        for (Seat seat : roomSeats) {
            String status;
            Boolean heldByMe = false;

            SeatBookingStatus dbStatus = dbStatuses.get(seat.getId());
            if (dbStatus == SeatBookingStatus.BOOKED) {
                status = "SOLD";
            } else if (dbStatus == SeatBookingStatus.HELD) {
                status = "HELD";
                heldByMe = currentUserId != null && currentUserId.equals(dbHolders.get(seat.getId()));
            } else {
                // Khong co trong DB -> kiem tra Redis hold (ghe user khac dang chon)
                var holder = seatHoldService.getHolder(showtimeId, seat.getId());
                if (holder.isPresent() && !holder.get().equals(currentUserId)) {
                    status = "HELD";
                } else if (holder.isPresent()) {
                    status = "HELD";
                    heldByMe = true;
                } else {
                    status = "AVAILABLE";
                }
            }

            seatResponses.add(SeatResponse.builder()
                    .seatId(seat.getId())
                    .rowLabel(seat.getRowLabel())
                    .seatNumber(seat.getSeatNumber())
                    .seatType(seat.getSeatType().name())
                    .price(SeatPriceCalculator.priceOf(showtime.getPriceBase(), seat.getSeatType()))
                    .status(status)
                    .heldByMe(heldByMe)
                    .build());
        }

        return SeatLayoutResponse.builder()
                .showtime(showtimeMapper.toResponse(showtime))
                .seats(seatResponses)
                .holdMinutes((int) seatHoldService.getHoldMinutes())
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "showtimes", allEntries = true),
            @CacheEvict(value = "movies", allEntries = true)
    })
    public ShowtimeResponse create(ShowtimeRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng id=" + request.getRoomId()));
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim id=" + request.getMovieId()));

        LocalDateTime start = request.getStartTime();
        // Suat chieu ket thuc sau thoi luong phim + 15 phut nghi giua cac suat
        LocalDateTime end = start.plusMinutes(movie.getDuration() != null ? movie.getDuration() + 15 : 135);

        // Kiem tra trung lich trong cung phong
        boolean conflict = showtimeRepository.findWithDetails(null, room.getCinema().getId(),
                        start.toLocalDate().atStartOfDay(), start.toLocalDate().plusDays(1).atStartOfDay())
                .stream()
                .anyMatch(st -> st.getRoom().getId().equals(room.getId())
                        && start.isBefore(st.getEndTime())
                        && end.isAfter(st.getStartTime()));
        if (conflict) {
            throw new BadRequestException("Phòng chiếu đã có suất chiếu khác trong khung giờ này");
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(start)
                .endTime(end)
                .priceBase(request.getPriceBase())
                .format(request.getFormat() == null || request.getFormat().isBlank() ? "2D" : request.getFormat())
                .subtitleType(request.getSubtitleType() == null || request.getSubtitleType().isBlank()
                        ? "PHU_DE" : request.getSubtitleType())
                .build();

        showtime = showtimeRepository.save(showtime);
        log.info("ADMIN tao suat chieu id={} room={} luc {}", showtime.getId(), room.getName(), start);
        return showtimeMapper.toResponse(showtime);
    }

    @Override
    @Transactional
    @CacheEvict(value = "showtimes", allEntries = true)
    public void delete(Long id) {
        showtimeRepository.deleteById(id);
    }
}
