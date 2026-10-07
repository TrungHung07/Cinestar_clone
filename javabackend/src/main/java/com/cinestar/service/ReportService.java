package com.cinestar.service;

import com.cinestar.dto.response.RevenueResponse;

import java.time.LocalDate;

public interface ReportService {

    /** Bao cao doanh thu theo khoang ngay (chi ADMIN) */
    RevenueResponse getRevenue(LocalDate from, LocalDate to);
}
