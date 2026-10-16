package com.cts.oas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request payload for creating an auction")
public class AuctionRequestDTO {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    @Schema(description = "ID of the product to auction", example = "1", required = true)
    private Integer productId;

    @NotNull(message = "Start date is required")
    @Schema(description = "Auction start date and time", example = "2026-07-16T13:30:00", required = true)
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @Schema(description = "Auction end date and time", example = "2026-07-23T13:30:00", required = true)
    private LocalDateTime endDate;
}