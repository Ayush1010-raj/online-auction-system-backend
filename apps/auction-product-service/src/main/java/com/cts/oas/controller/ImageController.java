package com.cts.oas.controller;

import com.cts.oas.dto.request.ImageRequestDTO;
import com.cts.oas.dto.response.ImageResponseDTO;
import com.cts.oas.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Image", description = "Endpoints for managing product images")
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "Add an image to a product")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Image added"),
            @ApiResponse(responseCode = "400", description = "Invalid URL or product ID"),
            @ApiResponse(responseCode = "403", description = "Access Denied"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    public ResponseEntity<?> addImage(
            @Valid @RequestBody ImageRequestDTO dto,
            @RequestHeader("X-User-Role") String userRole) {

        if (!userRole.equals("SELLER") && !userRole.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only SELLER can add images.");
        }

        log.info("POST /api/images - Adding image to productId={}", dto.getProductId());
        ImageResponseDTO response = imageService.addImage(dto);
        log.info("Image id={} added to productId={}", response.getImageId(), dto.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all images for a product")
    @ApiResponse(responseCode = "200", description = "Images retrieved")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ImageResponseDTO>> getImagesByProduct(
            @Parameter(description = "Product ID", example = "1") @PathVariable Integer productId) {
        log.info("GET /api/images/product/{} - Fetching images", productId);
        List<ImageResponseDTO> images = imageService.getImagesByProduct(productId);
        log.debug("Found {} images for productId={}", images.size(), productId);
        return ResponseEntity.ok(images);
    }

    @Operation(summary = "Delete an image")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Image deleted"),
            @ApiResponse(responseCode = "403", description = "Access Denied"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(
            @Parameter(description = "Image ID", example = "1") @PathVariable Integer id,
            @RequestHeader("X-User-Role") String userRole) {

        if (!userRole.equals("SELLER") && !userRole.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only SELLER can delete images.");
        }

        log.info("DELETE /api/images/{} - Deleting image", id);
        imageService.deleteImage(id);
        log.info("Image id={} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}