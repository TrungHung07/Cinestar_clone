package com.cinestar.util;

import com.cinestar.entity.enums.SeatType;

import java.math.BigDecimal;

/** Cong thuc tinh gia ve theo loai ghe - dung chung cho so do ghe + dat ve */
public final class SeatPriceCalculator {

    /** Ghe VIP cong them 20.000 so voi gia co ban */
    public static final BigDecimal VIP_SURCHARGE = new BigDecimal("20000");

    private SeatPriceCalculator() {
    }

    public static BigDecimal priceOf(BigDecimal priceBase, SeatType seatType) {
        return switch (seatType) {
            case VIP -> priceBase.add(VIP_SURCHARGE);
            // Ghe doi = gia 2 ve rieng le
            case COUPLE -> priceBase.multiply(BigDecimal.valueOf(2));
            case STANDARD -> priceBase;
        };
    }
}
