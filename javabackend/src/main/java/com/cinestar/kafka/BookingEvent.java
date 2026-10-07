package com.cinestar.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Su kien dat ve phat len Kafka topic "booking-events".
 * Jackson can constructor khong tham + setter de deserialize.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {

    public static final String TYPE_CONFIRMED = "BOOKING_CONFIRMED";
    public static final String TYPE_CANCELLED = "BOOKING_CANCELLED";

    private String eventType;
    private String bookingCode;
    private String customerName;
    private String customerEmail;
    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private String showtimeTime;
    private List<String> seatNames;
    private BigDecimal totalAmount;
    private LocalDateTime occurredAt;
}
