package com.cts.oas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Request payload for creating or updating a product")
public class ProductRequestDTO {

    @NotNull(message = "Seller ID is required")
    @Positive(message = "Seller ID must be a positive number")
    @Schema(description = "ID of the seller listing this product", example = "1", required = true)
    private Integer sellerId;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    @Schema(description = "Product title", example = "MacBook Pro M2", required = true)
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Schema(description = "Detailed product description", example = "Apple MacBook Pro 14 inch, M2 chip")
    private String description;

    @NotNull(message = "Start price is required")
    @DecimalMin(value = "0.01", message = "Start price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Start price must have at most 8 digits and 2 decimal places")
    @Schema(description = "Starting bid price", example = "1200.00", required = true)
    private BigDecimal startPrice;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    @Schema(description = "Product category", example = "Electronics")
    private String category;

    @Schema(description = "List of image URLs for the product")
    private List<
            @NotBlank(message = "Image URL cannot be blank")
            @Size(max = 2048, message = "Image URL cannot exceed 2048 characters")
                    String> imageUrls;
}