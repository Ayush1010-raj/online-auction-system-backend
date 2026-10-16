package com.cts.oas.controller;

import com.cts.oas.dto.request.ProductRequestDTO;
import com.cts.oas.dto.response.ProductResponseDTO;
import com.cts.oas.exception.GlobalExceptionHandler;
import com.cts.oas.exception.ResourceNotFoundException;
import com.cts.oas.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Mock private ProductService productService;

    @InjectMocks private ProductController productController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ProductResponseDTO responseDTO;
    private ProductRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        responseDTO = new ProductResponseDTO();
        responseDTO.setProductId(1);
        responseDTO.setSellerId(1);
        responseDTO.setTitle("MacBook Pro M2");
        responseDTO.setDescription("Apple MacBook Pro 14 inch");
        responseDTO.setStartPrice(new BigDecimal("1200.00"));
        responseDTO.setCategory("Electronics");
        responseDTO.setStatus("AVAILABLE");
        responseDTO.setImageUrls(List.of("https://example.com/images/macbook.jpg"));

        requestDTO = new ProductRequestDTO();
        requestDTO.setSellerId(1);
        requestDTO.setTitle("MacBook Pro M2");
        requestDTO.setDescription("Apple MacBook Pro 14 inch");
        requestDTO.setStartPrice(new BigDecimal("1200.00"));
        requestDTO.setCategory("Electronics");
        requestDTO.setImageUrls(List.of("https://example.com/images/macbook.jpg"));
    }

    // POST /api/products
    @Nested
    @DisplayName("POST /api/products")
    class CreateProduct {

        @Test
        @DisplayName("Should return 201 when product created successfully")
        void shouldReturn201WhenProductCreated() throws Exception {
            when(productService.createProduct(any())).thenReturn(responseDTO);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.title").value("MacBook Pro M2"))
                    .andExpect(jsonPath("$.status").value("AVAILABLE"));
        }

        @Test
        @DisplayName("Should return 400 when sellerId is missing")
        void shouldReturn400WhenSellerIdMissing() throws Exception {
            requestDTO.setSellerId(null);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.sellerId").value("Seller ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when title is blank")
        void shouldReturn400WhenTitleBlank() throws Exception {
            requestDTO.setTitle("");

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title").exists());
        }

        @Test
        @DisplayName("Should return 400 when startPrice is null")
        void shouldReturn400WhenStartPriceNull() throws Exception {
            requestDTO.setStartPrice(null);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.startPrice").exists());
        }

        @Test
        @DisplayName("Should return 400 when startPrice is zero")
        void shouldReturn400WhenStartPriceZero() throws Exception {
            requestDTO.setStartPrice(BigDecimal.ZERO);

            mockMvc.perform(post("/api/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.startPrice").exists());
        }
    }

    // GET /api/products
    @Nested
    @DisplayName("GET /api/products")
    class GetAllProducts {

        @Test
        @DisplayName("Should return 200 with list of products")
        void shouldReturn200WithProducts() throws Exception {
            when(productService.getAllProducts()).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("MacBook Pro M2"));
        }

        @Test
        @DisplayName("Should return 200 with empty list when no products")
        void shouldReturn200WithEmptyList() throws Exception {
            when(productService.getAllProducts()).thenReturn(List.of());

            mockMvc.perform(get("/api/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // GET /api/products/{id}
    @Nested
    @DisplayName("GET /api/products/{id}")
    class GetProductById {

        @Test
        @DisplayName("Should return 200 when product found")
        void shouldReturn200WhenFound() throws Exception {
            when(productService.getProductById(1)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.title").value("MacBook Pro M2"));
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(productService.getProductById(999))
                    .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

            mockMvc.perform(get("/api/products/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("999")));
        }
    }

    // PUT /api/products/{id}
    @Nested
    @DisplayName("PUT /api/products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("Should return 200 when product updated")
        void shouldReturn200WhenUpdated() throws Exception {
            responseDTO.setTitle("MacBook Pro M2 - Updated");
            when(productService.updateProduct(eq(1), any())).thenReturn(responseDTO);

            mockMvc.perform(put("/api/products/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("MacBook Pro M2 - Updated"));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent product")
        void shouldReturn404WhenProductNotFound() throws Exception {
            when(productService.updateProduct(eq(999), any()))
                    .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

            mockMvc.perform(put("/api/products/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    // DELETE /api/products/{id}
    @Nested
    @DisplayName("DELETE /api/products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("Should return 204 when product deleted")
        void shouldReturn204WhenDeleted() throws Exception {
            doNothing().when(productService).deleteProduct(1);

            mockMvc.perform(delete("/api/products/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent product")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Product not found with id: 999"))
                    .when(productService).deleteProduct(999);

            mockMvc.perform(delete("/api/products/999"))
                    .andExpect(status().isNotFound());
        }
    }
}