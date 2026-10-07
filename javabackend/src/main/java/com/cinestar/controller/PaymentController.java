package com.cinestar.controller;

import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.BookingResponse;
import com.cinestar.dto.response.PaymentUrlResponse;
import com.cinestar.dto.response.VnPayIpnResponse;
import com.cinestar.security.SecurityUtils;
import com.cinestar.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "Thanh toán VNPAY", description = "Tạo URL thanh toán, IPN và Return từ VNPAY Sandbox")
@Slf4j
@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** Tao URL thanh toan VNPAY cho don ve */
    @PostMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentUrlResponse>> createPayment(
            @PathVariable Long bookingId, HttpServletRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        PaymentUrlResponse response = paymentService.createVnPayUrl(userId, bookingId, request);
        return ResponseEntity.ok(ApiResponse.<PaymentUrlResponse>builder()
                .success(true)
                .message("Tạo liên kết thanh toán VNPAY thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * VNPAY redirect nguoi dung quay lai day (vnp_ReturnUrl).
     * Frontend (web/mobile) doc cac tham so tren URL roi goi endpoint nay
     * de backend xac minh chu ky + cap nhat don. Trong localhost, IPN
     * khong goi duoc nen day la kenh xac nhan chinh cho demo.
     */
    @GetMapping("/return")
    public ResponseEntity<ApiResponse<BookingResponse>> handleReturn(@RequestParam Map<String, String> params) {
        log.info("[VNPAY/Return] Nhan ket qua cho TxnRef {}", params.get("vnp_TxnRef"));
        return ResponseEntity.ok(paymentService.handleReturn(params));
    }

    /** IPN server-to-server (dung khi backend co public domain) */
    @GetMapping("/ipn")
    public ResponseEntity<VnPayIpnResponse> handleIpn(@RequestParam Map<String, String> params) {
        log.info("[VNPAY/IPN] Nhan thong bao cho TxnRef {}", params.get("vnp_TxnRef"));
        return ResponseEntity.ok(paymentService.handleIpn(params));
    }
}
