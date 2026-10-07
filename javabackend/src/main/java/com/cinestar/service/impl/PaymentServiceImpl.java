package com.cinestar.service.impl;

import com.cinestar.config.VnPayProperties;
import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.BookingResponse;
import com.cinestar.dto.response.PaymentUrlResponse;
import com.cinestar.dto.response.VnPayIpnResponse;
import com.cinestar.entity.Booking;
import com.cinestar.entity.Payment;
import com.cinestar.entity.enums.BookingStatus;
import com.cinestar.entity.enums.PaymentMethod;
import com.cinestar.entity.enums.PaymentStatus;
import com.cinestar.exception.BadRequestException;
import com.cinestar.exception.ConflictException;
import com.cinestar.exception.ResourceNotFoundException;
import com.cinestar.exception.UnauthorizedException;
import com.cinestar.repository.BookingRepository;
import com.cinestar.repository.PaymentRepository;
import com.cinestar.service.BookingService;
import com.cinestar.service.PaymentService;
import com.cinestar.util.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Tich hop VNPAY Sandbox.
 *
 * LUONG:
 * 1. POST /api/v1/payments/vnpay/{bookingId} -> backend tao URL da ky
 *    HMAC-SHA512 (vnp_TxnRef = bookingCode).
 * 2. Frontend mo URL bang WebView/browser - user thanh toan bang the demo
 *    NCB: 9704198526171432198, ten NGUYEN VAN A, OTP 123456.
 * 3. VNPAY:
 *    - goi IPN (server-to-server) neu backend co public domain
 *    - redirect nguoi dung ve vnp_ReturnUrl voi tham so ket qua
 * 4. Backend xac minh chu ky + so tien -> CONFIRMED -> phat Kafka event.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final DateTimeFormatter VNP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final VnPayProperties vnPayProperties;

    @Override
    @Transactional
    public PaymentUrlResponse createVnPayUrl(Long userId, Long bookingId, HttpServletRequest request) {
        Booking booking = bookingRepository.findWithDetailsById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn vé id=" + bookingId));

        // Phan quyen du lieu: chi chu don moi duoc thanh toan
        if (!booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền thanh toán đơn vé này");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Đơn vé không ở trạng thái chờ thanh toán");
        }
        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Đơn vé đã hết thời gian giữ ghế, vui lòng đặt lại");
        }

        // Ton tai payment PENDING truoc do (user bam lai "thanh toan") -> dung lai
        Payment payment = paymentRepository.findByVnpTxnRef(booking.getBookingCode())
                .orElseGet(() -> Payment.builder()
                        .booking(booking)
                        .method(PaymentMethod.VNPAY)
                        .status(PaymentStatus.PENDING)
                        .amount(booking.getTotalAmount())
                        .vnpTxnRef(booking.getBookingCode())
                        .build());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        // VNPAY quy doi: amount * 100 (VND khong co le)
        params.put("vnp_Amount", booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CreateDate", LocalDateTime.now().format(VNP_DATE_FORMAT));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", getClientIp(request));
        params.put("vnp_Locale", "vn");
        // OrderInfo khong dau, khong ky tu dac biet (#, ', ")
        params.put("vnp_OrderInfo", "Thanh toan ve " + booking.getBookingCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        params.put("vnp_TxnRef", booking.getBookingCode());
        params.put("vnp_ExpireDate", booking.getExpiresAt().format(VNP_DATE_FORMAT));

        String hashData = VnPayUtil.buildHashData(params, true);
        String secureHash = VnPayUtil.hmacSHA512(vnPayProperties.getHashSecret(), hashData);

        payment = paymentRepository.save(payment);
        String paymentUrl = vnPayProperties.getPaymentUrl() + "?" + hashData + "&vnp_SecureHash=" + secureHash;

        log.info("Tao URL VNPAY cho don {} - {} VND", booking.getBookingCode(), booking.getTotalAmount());
        return PaymentUrlResponse.builder()
                .paymentUrl(paymentUrl)
                .bookingCode(booking.getBookingCode())
                .amount(booking.getTotalAmount())
                .build();
    }

    /** Verify chu ky: thu ca 2 cach (gia tri goc + gia tri da encode) + log chi tiet de chuan doan */
    private boolean verifyChecksum(Map<String, String> params, String tag) {
        String received = params.get("vnp_SecureHash");
        if (received == null || received.isBlank()) {
            log.warn("[VNPAY/{}] Thieu vnp_SecureHash", tag);
            return false;
        }
        String rawHash = VnPayUtil.computeChecksum(params, vnPayProperties.getHashSecret());
        String encodedHash = VnPayUtil.computeChecksumEncoded(params, vnPayProperties.getHashSecret());
        boolean ok = received.equalsIgnoreCase(rawHash) || received.equalsIgnoreCase(encodedHash);
        log.info("[VNPAY/{}] TxnRef={} received={}", tag, params.get("vnp_TxnRef"), received);
        log.info("[VNPAY/{}] raw={} | encoded={} | match={}", tag, rawHash, encodedHash, ok);
        if (!ok) {
            log.warn("[VNPAY/{}] Params nhan duoc: {}", tag, params);
        }
        return ok;
    }

    @Override
    @Transactional
    public VnPayIpnResponse handleIpn(Map<String, String> params) {
        // 1. Kiem tra chu ky (thu ca raw va encoded)
        if (!verifyChecksum(params, "IPN")) {
            return VnPayIpnResponse.checksumFail();
        }

        // 2. Tim don
        String txnRef = params.get("vnp_TxnRef");
        Payment payment = paymentRepository.findByVnpTxnRef(txnRef).orElse(null);
        if (payment == null) {
            return VnPayIpnResponse.orderNotFound();
        }

        // 3. Doi chieu so tien
        long vnpAmount = Long.parseLong(params.getOrDefault("vnp_Amount", "0")) / 100;
        if (vnpAmount != payment.getAmount().longValue()) {
            log.warn("[VNPAY/IPN] So tien khong khop cho don {}: VNPAY={} vs DB={}",
                    txnRef, vnpAmount, payment.getAmount());
            return VnPayIpnResponse.amountInvalid();
        }

        // 4. Idempotency - da xac nhan truoc do
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return VnPayIpnResponse.orderAlreadyConfirmed();
        }

        // 5. Xac nhan thanh toan thanh cong?
        String responseCode = params.getOrDefault("vnp_ResponseCode", "");
        String transactionStatus = params.getOrDefault("vnp_TransactionStatus", "");
        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            try {
                bookingService.confirmBookingPayment(txnRef, params.get("vnp_TransactionNo"),
                        responseCode, params.get("vnp_BankCode"), params.get("vnp_PayDate"));
            } catch (ConflictException ex) {
                // Xung dot ghe: giao dich tien da thanh cong nhung khong the ban ghe
                log.error("[VNPAY/IPN] Xung dot ghe khi xac nhan don {}", txnRef, ex);
            }
            return VnPayIpnResponse.success();
        }

        // Giao dich that bai / bi huy
        payment.setStatus(PaymentStatus.FAILED);
        payment.setVnpResponseCode(responseCode);
        log.info("[VNPAY/IPN] Giao dich that bai don {} - code {}", txnRef, responseCode);
        return VnPayIpnResponse.success();
    }

    @Override
    @Transactional
    public ApiResponse<BookingResponse> handleReturn(Map<String, String> params) {
        if (!verifyChecksum(params, "Return")) {
            throw new BadRequestException("Chữ ký VNPAY không hợp lệ");
        }

        String txnRef = params.get("vnp_TxnRef");
        Booking booking = bookingRepository.findWithDetailsByBookingCode(txnRef)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn vé: " + txnRef));

        String responseCode = params.getOrDefault("vnp_ResponseCode", "");
        if (!"00".equals(responseCode)) {
            log.info("[VNPAY/Return] User huy hoac giao dich that bai don {} - code {}", txnRef, responseCode);
            return ApiResponse.<BookingResponse>builder()
                    .success(false)
                    .message("Thanh toán không thành công (mã lỗi VNPAY: " + responseCode + ")")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        try {
            BookingResponse confirmed = bookingService.confirmBookingPayment(
                    txnRef, params.get("vnp_TransactionNo"), responseCode,
                    params.get("vnp_BankCode"), params.get("vnp_PayDate"));
            return ApiResponse.<BookingResponse>builder()
                    .success(true)
                    .message("Thanh toán thành công! Vé đã được gửi qua email của bạn.")
                    .data(confirmed)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (ConflictException ex) {
            return ApiResponse.<BookingResponse>builder()
                    .success(false)
                    .message(ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (BadRequestException ex) {
            return ApiResponse.<BookingResponse>builder()
                    .success(false)
                    .message(ex.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
