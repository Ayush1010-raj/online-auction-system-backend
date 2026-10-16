package com.cts.oas.transaction.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {

    private Long buyerId;  // ← no @NotNull — set by controller from JWT

    @NotNull(message = "Auction ID is required")
    private Long auctionId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency = "inr";
}