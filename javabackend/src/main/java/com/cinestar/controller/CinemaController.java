package com.cinestar.controller;

import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.CinemaResponse;
import com.cinestar.service.CinemaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Rạp chiếu", description = "Hệ thống rạp Cinestar (public)")
@RestController
@RequestMapping("/api/v1/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CinemaResponse>>> getCinemas() {
        return ResponseEntity.ok(ApiResponse.<List<CinemaResponse>>builder()
                .success(true)
                .message("Danh sách rạp")
                .data(cinemaService.getCinemas())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<CinemaResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.<CinemaResponse>builder()
                .success(true)
                .message("Chi tiết rạp")
                .data(cinemaService.getBySlug(slug))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
