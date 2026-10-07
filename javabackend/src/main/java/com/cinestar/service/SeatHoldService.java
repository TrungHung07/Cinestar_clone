package com.cinestar.service;

import java.util.List;
import java.util.Optional;

/**
 * Giu ghe tam thoi bang Redis (SET NX + TTL).
 * Khi user chon ghe, ghe bi khoa 5 phut cho user do - user khac khong chon
 * duoc ghe do trong thoi gian nay. Het TTL tu dong nha ghe.
 */
public interface SeatHoldService {

    /**
     * Co gang giu ghe cho userId.
     * - Neu chua ai giu: giu thanh cong (return true)
     * - Neu chinh user do giu truoc do: van thanh cong (re-hold)
     * - Neu user khac giu: that bai (return false)
     */
    boolean tryHold(Long showtimeId, Long seatId, Long userId);

    /** Nha ghe ngay lap tuc (sau khi thanh toan xong / huy don) */
    void release(Long showtimeId, Long seatId);

    void releaseAll(Long showtimeId, List<Long> seatIds);

    /** User id dang giu ghe (neu co) */
    Optional<Long> getHolder(Long showtimeId, Long seatId);

    /** Ghe co dang bi giu boi mot user khac khong */
    boolean isHeldByOther(Long showtimeId, Long seatId, Long userId);

    /** Thoi gian giu ghe (phut) - frontend dung de countdown */
    long getHoldMinutes();
}
