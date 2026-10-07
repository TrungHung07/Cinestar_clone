package com.cinestar.entity.enums;

/**
 * Trang thai tung ghe trong don dat (dung cho unique index chong trung ghe)
 */
public enum SeatBookingStatus {
    HELD,      // dang giu cho thanh toan
    BOOKED,    // da ban
    CANCELLED  // da huy / het han
}
