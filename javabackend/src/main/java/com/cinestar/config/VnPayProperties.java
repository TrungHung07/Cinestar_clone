package com.cinestar.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Cau hinh ket noi VNPAY sandbox */
@Component
@Getter
public class VnPayProperties {

    private final String tmnCode;
    private final String hashSecret;
    private final String paymentUrl;
    private final String apiUrl;
    private final String returnUrl;
    private final String ipnUrl;

    public VnPayProperties(@Value("${vnpay.tmn-code}") String tmnCode,
                           @Value("${vnpay.hash-secret}") String hashSecret,
                           @Value("${vnpay.payment-url}") String paymentUrl,
                           @Value("${vnpay.api-url}") String apiUrl,
                           @Value("${vnpay.return-url}") String returnUrl,
                           @Value("${vnpay.ipn-url:}") String ipnUrl) {
        this.tmnCode = tmnCode;
        this.hashSecret = hashSecret;
        this.paymentUrl = paymentUrl;
        this.apiUrl = apiUrl;
        this.returnUrl = returnUrl;
        this.ipnUrl = ipnUrl;
    }
}
