package com.cinestar.controller;

import com.cinestar.dto.request.ComboRequest;
import com.cinestar.dto.request.PromotionRequest;
import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.ComboResponse;
import com.cinestar.dto.response.PromotionResponse;
import com.cinestar.service.ComboService;
import com.cinestar.service.PromotionService;
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

/** CRUD combo bap nuoc + khuyen mai - chi ADMIN */
@Tag(name = "Quản trị — Combo & Khuyến mãi", description = "CRUD combo bắp nước và khuyến mãi (ROLE_ADMIN)")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final ComboService comboService;
    private final PromotionService promotionService;

    // ===== Combo =====

    @PostMapping("/combos")
    public ResponseEntity<ApiResponse<ComboResponse>> createCombo(@Valid @RequestBody ComboRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ComboResponse>builder()
                        .success(true)
                        .message("Tạo combo thành công")
                        .data(comboService.create(request))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PutMapping("/combos/{id}")
    public ResponseEntity<ApiResponse<ComboResponse>> updateCombo(@PathVariable Long id,
                                                                  @Valid @RequestBody ComboRequest request) {
        return ResponseEntity.ok(ApiResponse.<ComboResponse>builder()
                .success(true)
                .message("Cập nhật combo thành công")
                .data(comboService.update(id, request))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/combos/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCombo(@PathVariable Long id) {
        comboService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa combo thành công")
                .timestamp(LocalDateTime.now())
                .build());
    }

    // ===== Khuyen mai =====

    @PostMapping("/promotions")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PromotionResponse>builder()
                        .success(true)
                        .message("Tạo khuyến mãi thành công")
                        .data(promotionService.create(request))
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @PutMapping("/promotions/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(
            @PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                .success(true)
                .message("Cập nhật khuyến mãi thành công")
                .data(promotionService.update(id, request))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable Long id) {
        promotionService.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa khuyến mãi thành công")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
