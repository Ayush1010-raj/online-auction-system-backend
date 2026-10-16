package com.cts.oas.bid.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AuctionResponseDTO {
    private Integer auctionId;
    private Integer productId;
    private String productTitle;  // ← add this
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal currentBid;
    private String status;
}