package com.cts.oas.transaction.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long auctionId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String paymentStatus;   // "PENDING", "SUCCESS", "FAILED"

    private LocalDateTime paymentDate;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;
}
