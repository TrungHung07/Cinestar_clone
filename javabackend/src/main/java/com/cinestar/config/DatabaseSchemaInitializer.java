package com.cinestar.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Tao cac index database ma Hibernate khong the khai bao qua annotation:
 *
 * UNIQUE PARTIAL INDEX (showtime_id, seat_id) WHERE status IN ('HELD','BOOKED')
 * tren bang booking_seats - lop chong TRUNG GHE cuoi cung o muc database.
 * Khi user huy/het han, status doi thanh CANCELLED nen ghe duoc cho dat lai.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_booking_seats_active
                ON booking_seats (showtime_id, seat_id)
                WHERE status IN ('HELD', 'BOOKED')
                """);
        log.info("Khoi tao schema hoan tat: unique partial index cho booking_seats");
    }
}
