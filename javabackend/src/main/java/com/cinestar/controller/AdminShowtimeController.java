package com.cinestar.controller;

import com.cinestar.dto.request.ShowtimeRequest;
import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.ShowtimeResponse;
import com.cinestar.service.ShowtimeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** CRUD suat chieu - chi ADMIN */
@Tag(name = "Quản trị — Suất chiếu", description = "Tạo/xóa suất chiếu (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/admin/showtimes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShowtimeResponse>> create(@Valid @RequestBody ShowtimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ShowtimeResponse>builder()
                        .success(true)
                        .message("Tạo suất chiếu thành công")
                        .data(showtimeService.create(request))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        showtimeService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa suất chiếu thành công")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
