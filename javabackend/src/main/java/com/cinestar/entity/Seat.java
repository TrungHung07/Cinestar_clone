package com.cinestar.entity;

import com.cinestar.entity.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "row_label", "seat_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /** Hang ghe: A, B, C... */
    @Column(name = "row_label", nullable = false, length = 3)
    private String rowLabel;

    /** So ghe trong hang: 1, 2, 3... */
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SeatType seatType;

    private Boolean active = true;

    public String displayName() {
        return rowLabel + seatNumber;
    }
}
