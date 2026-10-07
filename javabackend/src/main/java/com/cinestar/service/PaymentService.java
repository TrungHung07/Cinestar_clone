package com.cinestar.service;

import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.BookingResponse;
import com.cinestar.dto.response.PaymentUrlResponse;
import com.cinestar.dto.response.VnPayIpnResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {

    /** Tao URL thanh toan VNPAY cho mot don ve */
    PaymentUrlResponse createVnPayUrl(Long userId, Long bookingId, HttpServletRequest request);

    /** Xu ly IPN (server-to-server VNPAY goi khi backend co public domain) */
    VnPayIpnResponse handleIpn(Map<String, String> params);

    /**
     * Xu ly RETURN (VNPAY redirect nguoi dung quay lai voi tham so trong URL).
     * Trong moi truong localhost IPN khong goi duoc nen frontend se goi endpoint
     * nay de backend xac minh chu ky + cap nhat don.
     */
    ApiResponse<BookingResponse> handleReturn(Map<String, String> params);
}
