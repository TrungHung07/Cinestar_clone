package com.cinestar.controller;

import com.cinestar.dto.request.CinemaRequest;
import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.CinemaResponse;
import com.cinestar.service.CinemaService;
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

/** CRUD rap - chi ADMIN */
@Tag(name = "Quản trị — Rạp", description = "CRUD rạp chiếu (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/cinemas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCinemaController {

    private final CinemaService cinemaService;

    @PostMapping
    public ResponseEntity<ApiResponse<CinemaResponse>> create(@Valid @RequestBody CinemaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CinemaResponse>builder()
                        .success(true)
                        .message("Tạo rạp thành công")
                        .data(cinemaService.create(request))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CinemaResponse>> update(@PathVariable Long id,
                                                              @Valid @RequestBody CinemaRequest request) {
        return ResponseEntity.ok(ApiResponse.<CinemaResponse>builder()
                .success(true)
                .message("Cập nhật rạp thành công")
                .data(cinemaService.update(id, request))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        cinemaService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa rạp thành công")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
