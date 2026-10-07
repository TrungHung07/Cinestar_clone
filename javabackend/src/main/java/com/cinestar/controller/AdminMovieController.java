package com.cinestar.controller;

import com.cinestar.dto.request.MovieRequest;
import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.MovieResponse;
import com.cinestar.service.MovieService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** CRUD phim - chi ADMIN */
@Tag(name = "Quản trị — Phim", description = "CRUD phim (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/movies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<ApiResponse<MovieResponse>> create(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MovieResponse>builder()
                        .success(true)
                        .message("Tạo phim thành công")
                        .data(movieService.create(request))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponse>> update(@PathVariable Long id,
                                                             @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(ApiResponse.<MovieResponse>builder()
                .success(true)
                .message("Cập nhật phim thành công")
                .data(movieService.update(id, request))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa phim thành công")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
