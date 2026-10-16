package com.cts.oas.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductResponseDTO {
    private Integer productId;
    private Integer sellerId;
    private String title;
    private String description;
    private BigDecimal startPrice;
    private String category;
    private String status;
    private List<String> imageUrls;
}