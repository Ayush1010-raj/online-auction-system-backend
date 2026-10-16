package com.cts.oas.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Request payload for adding an image to a product")
public class ImageRequestDTO {

    @NotNull(message = "Product ID is required")
    @Positive(message = "Product ID must be a positive number")
    @Schema(description = "ID of the product this image belongs to", example = "1", required = true)
    private Integer productId;

    @NotBlank(message = "Image URL is required")
    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    @Pattern(
            regexp = "^(https?://).+\\.(jpg|jpeg|png|gif|webp|svg)(\\?.*)?$",
            message = "Must be a valid image URL (jpg, jpeg, png, gif, webp, svg)"
    )
    @Schema(description = "URL of the image", example = "https://example.com/images/product.jpg", required = true)
    private String url;
}