package com.cinestar.controller;

import com.cinestar.dto.request.CreateBookingRequest;
import com.cinestar.dto.response.ApiResponse;
import com.cinestar.dto.response.BookingResponse;
import com.cinestar.security.SecurityUtils;
import com.cinestar.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Đặt vé", description = "Tạo đơn + giữ ghế 5 phút, vé của tôi, hủy vé (cần đăng nhập)")
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** Tao don dat ve + giu ghe 5 phut */
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        BookingResponse response = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<BookingResponse>builder()
                        .success(true)
                        .message("Tạo đơn vé thành công. Ghế được giữ 5 phút, vui lòng thanh toán trước khi hết hạn.")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /** Danh sach ve cua toi (co phan trang) */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.<Page<BookingResponse>>builder()
                .success(true)
                .message("Vé của tôi")
                .data(bookingService.getMyBookings(userId, page, size))
                .timestamp(LocalDateTime.now())
                .build());
    }

    /** Chi tiet don theo bookingCode - chi chu don duoc xem */
    @GetMapping("/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> getByCode(@PathVariable String bookingCode) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Chi tiết đơn vé")
                .data(bookingService.getByCode(userId, bookingCode))
                .timestamp(LocalDateTime.now())
                .build());
    }

    /** Huy ve (truoc gio chieu 1 tieng) - nha ghe cho nguoi khac */
    @PostMapping("/{bookingCode}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable String bookingCode) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Hủy vé thành công")
                .data(bookingService.cancelBooking(userId, bookingCode))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
