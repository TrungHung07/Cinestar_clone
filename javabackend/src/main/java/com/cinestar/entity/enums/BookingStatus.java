package com.cinestar.entity.enums;

/**
 * Trang thai don dat ve
 */
public enum BookingStatus {
    PENDING,     // da tao don, dang giu ghe, cho thanh toan
    CONFIRMED,   // thanh toan thanh cong
    CANCELLED,   // user huy
    EXPIRED      // het thoi gian giu ghe ma khong thanh toan
}
