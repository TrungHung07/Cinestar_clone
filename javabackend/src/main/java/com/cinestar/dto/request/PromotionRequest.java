package com.cinestar.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PromotionRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    private String imageUrl;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active;
}
