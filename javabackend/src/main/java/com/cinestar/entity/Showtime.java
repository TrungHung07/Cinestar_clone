package com.cinestar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtimes", indexes = {
        @Index(name = "ix_showtime_movie_start", columnList = "movie_id, start_time"),
        @Index(name = "ix_showtime_room_start", columnList = "room_id, start_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showtime extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    /** Gia ve co ban, ghe VIP/DOI se cong them khi dat */
    @Column(name = "price_base", nullable = false, precision = 10, scale = 0)
    private BigDecimal priceBase;

    @Column(length = 10)
    private String format;

    /** PHU_DE hoac LONG_TIENG */
    @Column(name = "subtitle_type", length = 20)
    private String subtitleType;
}
