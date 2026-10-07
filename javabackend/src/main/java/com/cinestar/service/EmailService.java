package com.cinestar.service;

import com.cinestar.kafka.BookingEvent;

/** Gui email thong bao dat ve (xay ra bat dong bo qua Kafka consumer) */
public interface EmailService {

    void sendBookingConfirmation(BookingEvent event);

    void sendBookingCancellation(BookingEvent event);
}
