package com.cinestar.kafka;

import com.cinestar.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventProducer {

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    /**
     * Phat su kien booking. Key = bookingCode de cac su kien cung don dat
     * nam cung partition (giu thu tu xu ly).
     */
    public void sendBookingEvent(BookingEvent event) {
        log.info("[Kafka] Phat su kien {} cho don {}", event.getEventType(), event.getBookingCode());
        kafkaTemplate.send(KafkaTopicConfig.BOOKING_EVENTS_TOPIC, event.getBookingCode(), event);
    }
}
