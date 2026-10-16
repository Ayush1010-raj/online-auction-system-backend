package com.cts.oas.service;

import com.cts.oas.dto.request.ImageRequestDTO;
import com.cts.oas.dto.response.ImageResponseDTO;

import java.util.List;

public interface ImageService {
    ImageResponseDTO addImage(ImageRequestDTO dto);
    List<ImageResponseDTO> getImagesByProduct(Integer productId);
    void deleteImage(Integer imageId);
}