package com.cts.oas.dto.response;

import lombok.Data;

@Data
public class ImageResponseDTO {
    private Integer imageId;
    private Integer productId;
    private String url;
}