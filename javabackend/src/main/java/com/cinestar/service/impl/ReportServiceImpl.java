package com.cinestar.service.impl;

import com.cinestar.dto.response.RevenueResponse;
import com.cinestar.exception.BadRequestException;
import com.cinestar.repository.BookingRepository;
import com.cinestar.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookingRepository bookingRepository;

    @Override
    @Transactional(readOnly = true)
    public RevenueResponse getRevenue(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Vui lòng truyền from và to (định dạng yyyy-MM-dd)");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        var fromDate = from.atStartOfDay();
        var toDate = to.plusDays(1).atStartOfDay();

        List<RevenueResponse.RevenueDateRow> byDate = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalBookings = 0;

        for (Object[] row : bookingRepository.revenueByDate(fromDate, toDate)) {
            LocalDate date = ((Date) row[0]).toLocalDate();
            long bookings = ((Number) row[1]).longValue();
            BigDecimal revenue = (BigDecimal) row[2];
            totalBookings += bookings;
            totalRevenue = totalRevenue.add(revenue != null ? revenue : BigDecimal.ZERO);
            byDate.add(new RevenueResponse.RevenueDateRow(date, bookings, revenue));
        }

        List<RevenueResponse.RevenueMovieRow> topMovies = bookingRepository.topMovies(fromDate, toDate, 5)
                .stream()
                .map(row -> new RevenueResponse.RevenueMovieRow(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]))
                .toList();

        return RevenueResponse.builder()
                .fromDate(from)
                .toDate(to)
                .totalRevenue(totalRevenue)
                .totalBookings(totalBookings)
                .byDate(byDate)
                .topMovies(topMovies)
                .build();
    }
}
