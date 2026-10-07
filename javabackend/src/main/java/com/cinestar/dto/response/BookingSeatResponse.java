package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class BookingSeatResponse {

    private Long seatId;
    private String rowLabel;
    private Integer seatNumber;
    private String seatType;
    private BigDecimal price;
}
