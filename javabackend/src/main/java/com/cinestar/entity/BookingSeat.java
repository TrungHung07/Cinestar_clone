package com.cinestar.entity;

import com.cinestar.entity.enums.SeatBookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Ghi nhan mot ghe thuoc ve mot don dat.
 *
 * Co CHONG TRUNG GHE o muc database:
 * unique partial index (showtime_id, seat_id) WHERE status IN ('HELD','BOOKED')
 * (tao boi DatabaseSchemaInitializer) - 2 user khong the mua cung 1 ghe
 * cua cung 1 suat chieu duoi moi hinh nao.
 */
@Entity
@Table(name = "booking_seats", indexes = {
        @Index(name = "ix_booking_seat_showtime", columnList = "showtime_id, seat_id"),
        @Index(name = "ix_booking_seat_booking", columnList = "booking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SeatBookingStatus status;
}
