package com.cinestar.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ComboRequest {

    @NotBlank(message = "Tên combo không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "1000", message = "Giá không hợp lệ")
    private BigDecimal price;

    private String imageUrl;

    private Boolean active;
}
