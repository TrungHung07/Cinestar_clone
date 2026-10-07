package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Bao cao doanh thu cho ADMIN */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class RevenueResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal totalRevenue;
    private Long totalBookings;
    private List<RevenueDateRow> byDate;
    private List<RevenueMovieRow> topMovies;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RevenueDateRow {
        private LocalDate date;
        private Long bookings;
        private BigDecimal revenue;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class RevenueMovieRow {
        private String movieTitle;
        private Long bookings;
        private BigDecimal revenue;
    }
}
