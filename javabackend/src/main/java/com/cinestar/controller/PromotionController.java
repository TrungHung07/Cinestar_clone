package com.cinestar.controller;

import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.PromotionResponse;
import com.cinestar.service.PromotionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Khuyến mãi", description = "Các chương trình khuyến mãi (public)")
@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getPromotions() {
        return ResponseEntity.ok(ApiResponse.<List<PromotionResponse>>builder()
                .success(true)
                .message("Danh sách khuyến mãi")
                .data(promotionService.getPromotions())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
