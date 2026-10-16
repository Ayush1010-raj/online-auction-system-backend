package com.cts.oas.serviceimpl;

import com.cts.oas.dto.request.ImageRequestDTO;
import com.cts.oas.dto.response.ImageResponseDTO;
import com.cts.oas.exception.ResourceNotFoundException;
import com.cts.oas.model.Image;
import com.cts.oas.model.Product;
import com.cts.oas.repository.ImageRepository;
import com.cts.oas.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageServiceImpl Tests")
class ImageServiceImplTest {

    @Mock private ImageRepository imageRepo;
    @Mock private ProductRepository productRepo;

    @InjectMocks
    private ImageServiceImpl imageService;

    private Product product;
    private Image image;
    private ImageRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .productId(1)
                .sellerId(1)
                .title("MacBook Pro M2")
                .startPrice(new BigDecimal("1200.00"))
                .status("AVAILABLE")
                .build();

        image = Image.builder()
                .imageId(1)
                .product(product)
                .url("https://example.com/images/macbook.jpg")
                .build();

        requestDTO = new ImageRequestDTO();
        requestDTO.setProductId(1);
        requestDTO.setUrl("https://example.com/images/macbook.jpg");
    }

    // ADD IMAGE
    @Nested
    @DisplayName("addImage()")
    class AddImage {

        @Test
        @DisplayName("Should add image successfully")
        void shouldAddImageSuccessfully() {
            when(productRepo.findById(1)).thenReturn(Optional.of(product));
            when(imageRepo.save(any(Image.class))).thenReturn(image);

            ImageResponseDTO response = imageService.addImage(requestDTO);

            assertThat(response).isNotNull();
            assertThat(response.getImageId()).isEqualTo(1);
            assertThat(response.getProductId()).isEqualTo(1);
            assertThat(response.getUrl()).isEqualTo("https://example.com/images/macbook.jpg");
            verify(imageRepo, times(1)).save(any(Image.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFound() {
            when(productRepo.findById(99)).thenReturn(Optional.empty());
            requestDTO.setProductId(99);

            assertThatThrownBy(() -> imageService.addImage(requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(imageRepo, never()).save(any());
        }
    }

    // GET IMAGES BY PRODUCT
    @Nested
    @DisplayName("getImagesByProduct()")
    class GetImagesByProduct {

        @Test
        @DisplayName("Should return all images for a product")
        void shouldReturnImagesForProduct() {
            Image image2 = Image.builder().imageId(2).product(product)
                    .url("https://example.com/images/macbook2.jpg").build();
            when(imageRepo.findByProduct_ProductId(1)).thenReturn(List.of(image, image2));

            List<ImageResponseDTO> result = imageService.getImagesByProduct(1);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ImageResponseDTO::getUrl)
                    .containsExactlyInAnyOrder(
                            "https://example.com/images/macbook.jpg",
                            "https://example.com/images/macbook2.jpg"
                    );
        }

        @Test
        @DisplayName("Should return empty list when product has no images")
        void shouldReturnEmptyWhenNoImages() {
            when(imageRepo.findByProduct_ProductId(1)).thenReturn(List.of());

            List<ImageResponseDTO> result = imageService.getImagesByProduct(1);

            assertThat(result).isEmpty();
        }
    }

    // DELETE IMAGE
    @Nested
    @DisplayName("deleteImage()")
    class DeleteImage {

        @Test
        @DisplayName("Should delete image successfully")
        void shouldDeleteImageSuccessfully() {
            when(imageRepo.findById(1)).thenReturn(Optional.of(image));
            doNothing().when(imageRepo).delete(image);

            assertThatCode(() -> imageService.deleteImage(1)).doesNotThrowAnyException();
            verify(imageRepo, times(1)).delete(image);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when image not found")
        void shouldThrowWhenImageNotFound() {
            when(imageRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> imageService.deleteImage(999))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(imageRepo, never()).delete(any());
        }
    }
}