package com.cinestar.mapper;

import com.cinestar.dto.response.BookingItemResponse;
import com.cinestar.dto.response.BookingResponse;
import com.cinestar.dto.response.BookingSeatResponse;
import com.cinestar.entity.Booking;
import com.cinestar.entity.enums.SeatBookingStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingMapper {

    private final ShowtimeMapper showtimeMapper;

    public BookingMapper(ShowtimeMapper showtimeMapper) {
        this.showtimeMapper = showtimeMapper;
    }

    public BookingResponse toResponse(Booking booking) {
        List<BookingSeatResponse> seats = booking.getSeats().stream()
                .filter(bs -> bs.getStatus() != SeatBookingStatus.CANCELLED)
                .map(bs -> new BookingSeatResponse(
                        bs.getSeat().getId(),
                        bs.getSeat().getRowLabel(),
                        bs.getSeat().getSeatNumber(),
                        bs.getSeat().getSeatType().name(),
                        bs.getPrice()))
                .toList();

        List<BookingItemResponse> items = booking.getItems().stream()
                .map(item -> new BookingItemResponse(
                        item.getCombo().getId(),
                        item.getCombo().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity()))))
                .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus().name())
                .totalAmount(booking.getTotalAmount())
                .expiresAt(booking.getExpiresAt())
                .createdAt(booking.getCreatedAt())
                .showtime(showtimeMapper.toResponse(booking.getShowtime()))
                .seats(seats)
                .items(items)
                .paymentStatus(booking.getPayment() != null ? booking.getPayment().getStatus().name() : null)
                .paymentMethod(booking.getPayment() != null ? booking.getPayment().getMethod().name() : null)
                .bookedBy(booking.getUser() != null ? booking.getUser().getFullName() : null)
                .build();
    }
}
