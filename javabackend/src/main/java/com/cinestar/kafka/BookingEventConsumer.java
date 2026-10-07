package com.cinestar.kafka;

import com.cinestar.config.KafkaTopicConfig;
import com.cinestar.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consumers cua topic "booking-events" - 2 consumer group doc doc lap nhau:
 *
 * 1. "cinestar-email": gui email xac nhan dat ve (async, that bai khong anh
 *    huong giao dich thanh toan - co the retry bang cach replay topic).
 *
 * 2. "cinestar-cache-invalidator": don dep cache Redis khi co thay doi du lieu
 *    (so do ghe, suat chieu...) de toan bo instance backend nhin thay du lieu moi.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final EmailService emailService;
    private final CacheManager cacheManager;

    @KafkaListener(topics = KafkaTopicConfig.BOOKING_EVENTS_TOPIC, groupId = "cinestar-email")
    public void onBookingEventForEmail(BookingEvent event) {
        log.info("[Kafka/email] Nhan su kien {} don {}", event.getEventType(), event.getBookingCode());
        try {
            if (BookingEvent.TYPE_CONFIRMED.equals(event.getEventType())) {
                emailService.sendBookingConfirmation(event);
            } else if (BookingEvent.TYPE_CANCELLED.equals(event.getEventType())) {
                emailService.sendBookingCancellation(event);
            }
        } catch (Exception ex) {
            // Nuot loi: message van da tieu thu; trong thuc te dung DLT (dead letter topic) de retry
            log.error("[Kafka/email] Gui email that bai cho don {}", event.getBookingCode(), ex);
        }
    }

    @KafkaListener(topics = KafkaTopicConfig.BOOKING_EVENTS_TOPIC, groupId = "cinestar-cache-invalidator")
    public void onBookingEventForCache(BookingEvent event) {
        log.info("[Kafka/cache] Nhan su kien {} don {} - xoa cache lien quan",
                event.getEventType(), event.getBookingCode());
        for (String cacheName : List.of("showtimes", "movies", "movie-details")) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
