package com.cinestar.controller;

import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.RevenueResponse;
import com.cinestar.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Bao cao doanh thu - chi ADMIN */
@Tag(name = "Quản trị — Báo cáo", description = "Doanh thu theo khoảng thời gian (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueResponse>> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.<RevenueResponse>builder()
                .success(true)
                .message("Báo cáo doanh thu")
                .data(reportService.getRevenue(from, to))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
