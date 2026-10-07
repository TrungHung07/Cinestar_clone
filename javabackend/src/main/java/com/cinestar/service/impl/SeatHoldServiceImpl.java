package com.cinestar.service.impl;

import com.cinestar.service.SeatHoldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * LOP PHONG THU 1 trong co che chong 2 user dat cung 1 ghe:
 * Redis SET NX EX - khoan ghe co TTL tu dong het han, khong lo "ghe ma".
 */
@Slf4j
@Service
public class SeatHoldServiceImpl implements SeatHoldService {

    /** Dinh dang key: seat:hold:{showtimeId}:{seatId} -> {userId} */
    private static final String KEY_PREFIX = "seat:hold:";

    private final StringRedisTemplate redisTemplate;
    private final long holdMinutes;

    public SeatHoldServiceImpl(StringRedisTemplate redisTemplate,
                               @Value("${app.booking.hold-minutes}") long holdMinutes) {
        this.redisTemplate = redisTemplate;
        this.holdMinutes = holdMinutes;
    }

    private String key(Long showtimeId, Long seatId) {
        return KEY_PREFIX + showtimeId + ":" + seatId;
    }

    @Override
    public boolean tryHold(Long showtimeId, Long seatId, Long userId) {
        String key = key(showtimeId, seatId);
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(userId), Duration.ofMinutes(holdMinutes));
        if (Boolean.TRUE.equals(acquired)) {
            return true;
        }
        // Da co nguoi giu - cho phep neu chinh la chu nhan (user refresh trang, re-hold)
        String holder = redisTemplate.opsForValue().get(key);
        boolean mine = userId != null && String.valueOf(userId).equals(holder);
        if (mine) {
            // Lam moi TTL cho chu nhan
            redisTemplate.expire(key, Duration.ofMinutes(holdMinutes));
        }
        return mine;
    }

    @Override
    public void release(Long showtimeId, Long seatId) {
        redisTemplate.delete(key(showtimeId, seatId));
    }

    @Override
    public void releaseAll(Long showtimeId, List<Long> seatIds) {
        if (seatIds == null || seatIds.isEmpty()) {
            return;
        }
        redisTemplate.delete(seatIds.stream()
                .map(seatId -> key(showtimeId, seatId))
                .toList());
    }

    @Override
    public Optional<Long> getHolder(Long showtimeId, Long seatId) {
        String holder = redisTemplate.opsForValue().get(key(showtimeId, seatId));
        if (holder == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(holder));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isHeldByOther(Long showtimeId, Long seatId, Long userId) {
        Optional<Long> holder = getHolder(showtimeId, seatId);
        return holder.isPresent() && !holder.get().equals(userId);
    }

    @Override
    public long getHoldMinutes() {
        return holdMinutes;
    }
}
