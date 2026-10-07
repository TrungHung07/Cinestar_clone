package com.cinestar.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Topic Kafka cua he thong:
 * "booking-events" - su kien dat ve (xac nhan thanh toan / huy ve)
 * - consumer "cinestar-email": gui email xac nhan + ve
 * - consumer "cinestar-cache-invalidator": xoa cache Redis lien quan
 * => Tach biet (decouple) luong thanh toan khoi cac viec phu, thanh toan
 *    tra ket qua cho user ngay, email that bai khong anh huong dat ve.
 */
@Configuration
public class KafkaTopicConfig {

    public static final String BOOKING_EVENTS_TOPIC = "booking-events";

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name(BOOKING_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
