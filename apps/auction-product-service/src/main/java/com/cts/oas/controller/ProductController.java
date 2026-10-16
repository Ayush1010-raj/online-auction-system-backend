package com.cts.oas.controller;

import com.cts.oas.dto.request.ProductRequestDTO;
import com.cts.oas.dto.response.ProductResponseDTO;
import com.cts.oas.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Endpoints for managing auction products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductRequestDTO dto,
            @RequestHeader("X-User-Role") String userRole) {

        if (!userRole.equals("SELLER") && !userRole.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only SELLER can create products.");
        }

        log.info("POST /api/products - Creating product: title='{}', sellerId={}", dto.getTitle(), dto.getSellerId());
        ProductResponseDTO response = productService.createProduct(dto);
        log.info("Product created successfully with id={}", response.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        log.info("GET /api/products - Fetching all products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(description = "Product ID", example = "1") @PathVariable Integer id) {
        log.info("GET /api/products/{} - Fetching product", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsBySeller(
            @Parameter(description = "Seller's user ID", example = "1") @PathVariable Integer sellerId) {
        log.info("GET /api/products/seller/{} - Fetching products by seller", sellerId);
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(
            @Parameter(description = "Category name", example = "Electronics") @PathVariable String category) {
        log.info("GET /api/products/category/{} - Fetching products by category", category);
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(
            @Parameter(description = "Product ID", example = "1") @PathVariable Integer id,
            @Valid @RequestBody ProductRequestDTO dto,
            @RequestHeader("X-User-Role") String userRole) {

        if (!userRole.equals("SELLER") && !userRole.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only SELLER can update products.");
        }

        log.info("PUT /api/products/{} - Updating product", id);
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @Parameter(description = "Product ID", example = "1") @PathVariable Integer id,
            @RequestHeader("X-User-Role") String userRole) {

        if (!userRole.equals("SELLER") && !userRole.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only SELLER can delete products.");
        }

        log.info("DELETE /api/products/{} - Deleting product", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}