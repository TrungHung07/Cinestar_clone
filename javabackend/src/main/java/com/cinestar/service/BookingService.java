package com.cinestar.service;

import com.cinestar.dto.request.CreateBookingRequest;
import com.cinestar.dto.response.BookingResponse;
import org.springframework.data.domain.Page;

public interface BookingService {

    /**
     * Tao don dat ve + giu ghe 5 phut.
     * Chong trung ghe: Redis hold (lop 1) + kiem tra DB (lop 2) +
     * unique index database (lop 3).
     */
    BookingResponse createBooking(Long userId, CreateBookingRequest request);

    Page<BookingResponse> getMyBookings(Long userId, int page, int size);

    BookingResponse getByCode(Long userId, String bookingCode);

    BookingResponse cancelBooking(Long userId, String bookingCode);

    /**
     * Xac nhan thanh toan thanh cong (duoc goi boi PaymentService sau khi
     * kiem tra chu ky VNPAY). Su dung khoa bi quan + unique index de chong
     * 2 giao dich thanh toan tranh nhau cung mot ghe.
     */
    BookingResponse confirmBookingPayment(String bookingCode, String vnpTransactionNo,
                                          String vnpResponseCode, String vnpBankCode, String vnpPayDate);

    /** Cong viec dinh ky: don PENDING qua han giu ghe -> EXPIRED + nha ghe */
    void expirePendingBookings();
}
