package com.cinestar.controller;

import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.MovieDetailResponse;
import com.cinestar.dto.response.MovieResponse;
import com.cinestar.service.MovieService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Phim", description = "Danh sách, tìm kiếm, chi tiết phim (public)")
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /** Phim dang chieu (CACHE Redis) */
    @GetMapping("/now-showing")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getNowShowing() {
        return ResponseEntity.ok(ApiResponse.<List<MovieResponse>>builder()
                .success(true)
                .message("Phim đang chiếu")
                .data(movieService.getNowShowing())
                .timestamp(LocalDateTime.now())
                .build());
    }

    /** Phim sap chieu (CACHE Redis) */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getUpcoming() {
        return ResponseEntity.ok(ApiResponse.<List<MovieResponse>>builder()
                .success(true)
                .message("Phim sắp chiếu")
                .data(movieService.getUpcoming())
                .timestamp(LocalDateTime.now())
                .build());
    }

    /** Phim noi bat - banner trang chu */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.<List<MovieResponse>>builder()
                .success(true)
                .message("Phim nổi bật")
                .data(movieService.getFeatured())
                .timestamp(LocalDateTime.now())
                .build());
    }

    /** Danh sach phim co phan trang + loc status + tim kiem */
    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getMovies(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        com.cinestar.entity.enums.MovieStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = com.cinestar.entity.enums.MovieStatus.valueOf(status);
            } catch (IllegalArgumentException ex) {
                throw new com.cinestar.exception.BadRequestException(
                        "status phải là một trong: NOW_SHOWING, UPCOMING, ENDED");
            }
        }
        var result = movieService.getMovies(statusEnum, search, page, size);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Danh sách phim")
                .data(result)
                .timestamp(LocalDateTime.now())
                .build());
    }

    /** Chi tiet phim theo slug (CACHE Redis 30 phut) */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<MovieDetailResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.<MovieDetailResponse>builder()
                .success(true)
                .message("Chi tiết phim")
                .data(movieService.getBySlug(slug))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
