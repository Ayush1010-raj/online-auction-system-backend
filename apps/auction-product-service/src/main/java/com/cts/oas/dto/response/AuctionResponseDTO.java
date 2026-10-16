package com.cts.oas.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuctionResponseDTO {
    private Integer auctionId;
    private Integer productId;
    private Integer sellerId;        // ← seller of the product
    private String productTitle;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal currentBid;
    private String status;
    private List<String> imageUrls;
}