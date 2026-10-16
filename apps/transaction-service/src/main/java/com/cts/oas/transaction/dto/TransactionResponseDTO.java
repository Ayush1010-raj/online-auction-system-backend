package com.cts.oas.transaction.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {
    private Long transactionId;
    private Long buyerId;
    private Long auctionId;
    private BigDecimal amount;
    private String paymentStatus;
    private LocalDateTime paymentDate;

    private String checkoutUrl;
    private String stripeSessionId;
}

