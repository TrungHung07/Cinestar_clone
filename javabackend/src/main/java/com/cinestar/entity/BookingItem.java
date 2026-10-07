package com.cinestar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Bap nuoc/thuc an di kem don dat ve */
@Entity
@Table(name = "booking_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @Column(nullable = false)
    private Integer quantity;

    /** Don gia tai thoi diem dat */
    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal price;
}
