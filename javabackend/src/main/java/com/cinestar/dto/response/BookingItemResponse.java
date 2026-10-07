package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class BookingItemResponse {

    private Long comboId;
    private String comboName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
