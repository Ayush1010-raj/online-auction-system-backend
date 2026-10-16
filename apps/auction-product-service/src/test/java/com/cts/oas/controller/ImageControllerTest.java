package com.cts.oas.controller;

import com.cts.oas.dto.request.ImageRequestDTO;
import com.cts.oas.dto.response.ImageResponseDTO;
import com.cts.oas.exception.GlobalExceptionHandler;
import com.cts.oas.exception.ResourceNotFoundException;
import com.cts.oas.service.ImageService;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageController Tests")
class ImageControllerTest {

    @Mock private ImageService imageService;

    @InjectMocks private ImageController imageController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ImageResponseDTO responseDTO;
    private ImageRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        responseDTO = new ImageResponseDTO();
        responseDTO.setImageId(1);
        responseDTO.setProductId(1);
        responseDTO.setUrl("https://example.com/images/macbook.jpg");

        requestDTO = new ImageRequestDTO();
        requestDTO.setProductId(1);
        requestDTO.setUrl("https://example.com/images/macbook.jpg");
    }

    // POST /api/images
    @Nested
    @DisplayName("POST /api/images")
    class AddImage {

        @Test
        @DisplayName("Should return 201 when image added successfully")
        void shouldReturn201WhenAdded() throws Exception {
            when(imageService.addImage(any())).thenReturn(responseDTO);

            mockMvc.perform(post("/api/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.imageId").value(1))
                    .andExpect(jsonPath("$.productId").value(1))
                    .andExpect(jsonPath("$.url").value("https://example.com/images/macbook.jpg"));
        }

        @Test
        @DisplayName("Should return 400 when productId is missing")
        void shouldReturn400WhenProductIdMissing() throws Exception {
            requestDTO.setProductId(null);

            mockMvc.perform(post("/api/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.productId").value("Product ID is required"));
        }

        @Test
        @DisplayName("Should return 400 when URL is blank")
        void shouldReturn400WhenUrlBlank() throws Exception {
            requestDTO.setUrl("");

            mockMvc.perform(post("/api/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.url").exists());
        }

        @Test
        @DisplayName("Should return 404 when product not found")
        void shouldReturn404WhenProductNotFound() throws Exception {
            when(imageService.addImage(any()))
                    .thenThrow(new ResourceNotFoundException("Product not found with id: 1"));

            mockMvc.perform(post("/api/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Product not found")));
        }
    }

    // GET /api/images/product/{productId}
    @Nested
    @DisplayName("GET /api/images/product/{productId}")
    class GetImagesByProduct {

        @Test
        @DisplayName("Should return 200 with images for product")
        void shouldReturn200WithImages() throws Exception {
            ImageResponseDTO img2 = new ImageResponseDTO();
            img2.setImageId(2);
            img2.setProductId(1);
            img2.setUrl("https://example.com/images/macbook2.jpg");

            when(imageService.getImagesByProduct(1)).thenReturn(List.of(responseDTO, img2));

            mockMvc.perform(get("/api/images/product/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].url").value("https://example.com/images/macbook.jpg"))
                    .andExpect(jsonPath("$[1].url").value("https://example.com/images/macbook2.jpg"));
        }

        @Test
        @DisplayName("Should return 200 with empty list when no images")
        void shouldReturn200WithEmptyList() throws Exception {
            when(imageService.getImagesByProduct(1)).thenReturn(List.of());

            mockMvc.perform(get("/api/images/product/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // DELETE /api/images/{id}
    @Nested
    @DisplayName("DELETE /api/images/{id}")
    class DeleteImage {

        @Test
        @DisplayName("Should return 204 when image deleted")
        void shouldReturn204WhenDeleted() throws Exception {
            doNothing().when(imageService).deleteImage(1);

            mockMvc.perform(delete("/api/images/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when image not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Image not found with id: 999"))
                    .when(imageService).deleteImage(999);

            mockMvc.perform(delete("/api/images/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("999")));
        }
    }
}