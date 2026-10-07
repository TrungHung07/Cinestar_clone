package com.cinestar.controller;

import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.SeatLayoutResponse;
import com.cinestar.dto.response.ShowtimeResponse;
import com.cinestar.security.CustomUserDetails;
import com.cinestar.security.SecurityUtils;
import com.cinestar.service.ShowtimeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Suất chiếu", description = "Lịch chiếu và sơ đồ ghế (public)")
@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    /**
     * Lich chieu: /api/v1/showtimes?movieId=1&cinemaId=2&date=2026-10-08
     * Bo qua movieId/cinemaId de lay tat ca. Mac dinh ngay hom nay.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getShowtimes(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) String date) {
        String dateIso = date != null ? date : LocalDate.now().toString();
        return ResponseEntity.ok(ApiResponse.<List<ShowtimeResponse>>builder()
                .success(true)
                .message("Lịch chiếu")
                .data(showtimeService.getShowtimes(movieId, cinemaId, dateIso))
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * So do ghe cua suat chieu. Public doc duoc, nhung neu dang dang nhap thi
     * biet ghe nao do chinh user do giu (heldByMe).
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<SeatLayoutResponse>> getSeatLayout(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.<SeatLayoutResponse>builder()
                .success(true)
                .message("Sơ đồ ghế")
                .data(showtimeService.getSeatLayout(id, userId))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
