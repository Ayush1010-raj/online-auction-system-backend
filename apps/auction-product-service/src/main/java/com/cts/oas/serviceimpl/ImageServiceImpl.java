package com.cts.oas.serviceimpl;

import com.cts.oas.dto.request.ImageRequestDTO;
import com.cts.oas.dto.response.ImageResponseDTO;
import com.cts.oas.exception.ResourceNotFoundException;
import com.cts.oas.model.Image;
import com.cts.oas.model.Product;
import com.cts.oas.repository.ImageRepository;
import com.cts.oas.repository.ProductRepository;
import com.cts.oas.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepo;
    private final ProductRepository productRepo;

    @Override
    public ImageResponseDTO addImage(ImageRequestDTO dto) {
        log.debug("Adding image to productId={}, url={}", dto.getProductId(), dto.getUrl());
        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found with id={}", dto.getProductId());
                    return new ResourceNotFoundException("Product not found with id: " + dto.getProductId());
                });

        Image image = Image.builder()
                .product(product)
                .url(dto.getUrl())
                .build();

        Image saved = imageRepo.save(image);
        log.info("Image id={} added to productId={}", saved.getImageId(), dto.getProductId());
        return mapToResponse(saved);
    }

    @Override
    public List<ImageResponseDTO> getImagesByProduct(Integer productId) {
        log.debug("Fetching images for productId={}", productId);
        List<ImageResponseDTO> images = imageRepo.findByProduct_ProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        log.debug("Found {} images for productId={}", images.size(), productId);
        return images;
    }

    @Override
    public void deleteImage(Integer imageId) {
        log.debug("Deleting image id={}", imageId);
        Image image = imageRepo.findById(imageId)
                .orElseThrow(() -> {
                    log.error("Image not found with id={}", imageId);
                    return new ResourceNotFoundException("Image not found with id: " + imageId);
                });
        imageRepo.delete(image);
        log.info("Image id={} deleted", imageId);
    }

    // Mapper
    private ImageResponseDTO mapToResponse(Image img) {
        ImageResponseDTO dto = new ImageResponseDTO();
        dto.setImageId(img.getImageId());
        dto.setProductId(img.getProduct().getProductId());
        dto.setUrl(img.getUrl());
        return dto;
    }
}