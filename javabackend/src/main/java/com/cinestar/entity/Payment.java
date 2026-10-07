package com.cinestar.entity;

import com.cinestar.entity.enums.PaymentMethod;
import com.cinestar.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amount;

    /** Ma giao dich gui sang VNPAY (= bookingCode) */
    @Column(name = "vnp_txn_ref", length = 50)
    private String vnpTxnRef;

    /** Ma giao dich VNPAY tra ve */
    @Column(name = "vnp_transaction_no", length = 50)
    private String vnpTransactionNo;

    @Column(name = "vnp_response_code", length = 5)
    private String vnpResponseCode;

    @Column(name = "vnp_bank_code", length = 20)
    private String vnpBankCode;

    @Column(name = "vnp_pay_date", length = 20)
    private String vnpPayDate;

    private LocalDateTime paidAt;
}
