package com.cinestar.controller;

import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.ComboResponse;
import com.cinestar.service.ComboService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Combo", description = "Bắp nước, đồ ăn kèm (public)")
@RestController
@RequestMapping("/api/v1/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;

    /** Danh sach bap nuoc / combo (dung buoc chon do an khi dat ve) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ComboResponse>>> getCombos() {
        return ResponseEntity.ok(ApiResponse.<List<ComboResponse>>builder()
                .success(true)
                .message("Danh sách combo")
                .data(comboService.getCombos())
                .timestamp(LocalDateTime.now())
                .build());
    }
}
