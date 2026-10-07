package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    private ShowtimeResponse showtime;
    private List<BookingSeatResponse> seats;
    private List<BookingItemResponse> items;

    /** Thong tin thanh toan (neu co) */
    private String paymentStatus;
    private String paymentMethod;

    /** Ten nguoi dat (dung tren ve) */
    private String bookedBy;
}
