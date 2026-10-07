package com.cinestar.service.impl;

import com.cinestar.kafka.BookingEvent;
import com.cinestar.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Gui email ve xac nhan. Neu chua cau hinh SMTP (MAIL_HOST rong) thi chi
 * IN NOI DUNG EMAIL ra console - de demo khong can SMTP that.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.mail.from}")
    private String from;

    @Async
    @Override
    public void sendBookingConfirmation(BookingEvent event) {
        String subject = "Cinestar - Xác nhận đặt vé " + event.getBookingCode();
        String html = buildHtml(event, "đặt vé thành công",
                "Cảm ơn bạn đã đặt vé tại Cinestar. Mã vé của bạn là <b>" + event.getBookingCode()
                        + "</b>. Vui lòng xuất trình mã vé (QR) tại quầy check-in trước giờ chiếu 15 phút.");
        send(event, subject, html);
    }

    @Async
    @Override
    public void sendBookingCancellation(BookingEvent event) {
        String subject = "Cinestar - Hủy vé " + event.getBookingCode();
        String html = buildHtml(event, "đã hủy vé",
                "Đơn vé <b>" + event.getBookingCode() + "</b> của bạn đã được hủy. "
                        + "Ghế đã được giải phóng cho khách hàng khác.");
        send(event, subject, html);
    }

    private void send(BookingEvent event, String subject, String html) {
        if (mailHost == null || mailHost.isBlank()) {
            log.info("""

                            ============================================================
                            [EMAIL DEMO - chua cau hinh SMTP] Gui den: {}
                            Tieu de: {}
                            ------------------------------------------------------------
                            Kinh gui {},
                            {}
                            Phim:        {}
                            Rap:         {} - {}
                            Suat chieu:  {}
                            Ghe:         {}
                            Tong tien:   {} VND
                            ============================================================""",
                    event.getCustomerEmail(), subject, event.getCustomerName(),
                    "Trang thai: " + subject, event.getMovieTitle(), event.getCinemaName(),
                    event.getRoomName(), event.getShowtimeTime(),
                    String.join(", ", event.getSeatNames()), event.getTotalAmount());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(event.getCustomerEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Da gui email xac nhan ve {} den {}", event.getBookingCode(), event.getCustomerEmail());
        } catch (MessagingException ex) {
            throw new IllegalStateException("Gui email that bai", ex);
        }
    }

    private String buildHtml(BookingEvent event, String headline, String body) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
                  <div style="background:#e50914;color:#fff;padding:16px;text-align:center">
                    <h2 style="margin:0">CINESTAR</h2>
                  </div>
                  <div style="padding:20px;border:1px solid #eee">
                    <h3>Bạn đã %s</h3>
                    <p>%s</p>
                    <table style="width:100%%;border-collapse:collapse">
                      <tr><td>Mã vé</td><td><b>%s</b></td></tr>
                      <tr><td>Phim</td><td>%s</td></tr>
                      <tr><td>Rạp</td><td>%s - %s</td></tr>
                      <tr><td>Suất chiếu</td><td>%s</td></tr>
                      <tr><td>Ghế</td><td>%s</td></tr>
                      <tr><td>Tổng tiền</td><td>%s VND</td></tr>
                    </table>
                  </div>
                </div>
                """.formatted(headline, body, event.getBookingCode(), event.getMovieTitle(),
                event.getCinemaName(), event.getRoomName(), event.getShowtimeTime(),
                String.join(", ", event.getSeatNames()), event.getTotalAmount());
    }
}
