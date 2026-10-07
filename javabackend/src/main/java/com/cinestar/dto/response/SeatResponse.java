package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Mot ghe trong so do ghe.
 * status: AVAILABLE (trong) / HELD (dang giu boi ai do) / SOLD (da ban)
 * heldByMe: ghese dang giu boi chinh user goi API (cho phep thao tac tiep)
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class SeatResponse {

    private Long seatId;
    private String rowLabel;
    private Integer seatNumber;
    private String seatType;
    private BigDecimal price;
    private String status;
    private Boolean heldByMe;

    public String displayName() {
        return rowLabel + seatNumber;
    }
}
