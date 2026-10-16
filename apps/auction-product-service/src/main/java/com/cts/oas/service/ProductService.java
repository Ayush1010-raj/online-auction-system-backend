package com.cts.oas.service;

import com.cts.oas.dto.request.ProductRequestDTO;
import com.cts.oas.dto.response.ProductResponseDTO;

import java.util.List;

public interface ProductService {

    ProductResponseDTO createProduct(ProductRequestDTO dto); // ← keep only this

    ProductResponseDTO getProductById(Integer productId);

    List<ProductResponseDTO> getAllProducts();

    List<ProductResponseDTO> getProductsBySeller(Integer sellerId);

    List<ProductResponseDTO> getProductsByCategory(String category);

    ProductResponseDTO updateProduct(Integer productId, ProductRequestDTO dto);

    void deleteProduct(Integer productId);
}