package com.cinestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class PaymentUrlResponse {

    private String paymentUrl;
    private String bookingCode;
    private BigDecimal amount;
}
