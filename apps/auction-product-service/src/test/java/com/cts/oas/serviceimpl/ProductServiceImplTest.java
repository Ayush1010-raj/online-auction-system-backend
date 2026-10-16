//package com.cts.oas.serviceimpl;
//
//import com.cts.oas.dto.request.ProductRequestDTO;
//import com.cts.oas.dto.response.ProductResponseDTO;
//import com.cts.oas.dto.response.UserResponseDTO;
//import com.cts.oas.exception.ResourceNotFoundException;
//
//import com.cts.oas.model.Product;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("ProductServiceImpl Tests")
//class ProductServiceImplTest {
//
//    @Mock private ProductRepository productRepo;
//    @Mock private ImageRepository imageRepo;
//
//    @InjectMocks
//    private ProductServiceImpl productService;
//
//    private Product product;
//    private ProductRequestDTO requestDTO;
//    private UserResponseDTO sellerDTO;
//    private static final String TEST_TOKEN = "Bearer test-token";
//
//    @BeforeEach
//    void setUp() {
//        product = Product.builder()
//                .productId(1)
//                .sellerId(1)
//                .title("MacBook Pro M2")
//                .description("Apple MacBook Pro 14 inch")
//                .startPrice(new BigDecimal("1200.00"))
//                .category("Electronics")
//                .status("AVAILABLE")
//                .build();
//
//        requestDTO = new ProductRequestDTO();
//        requestDTO.setSellerId(1);
//        requestDTO.setTitle("MacBook Pro M2");
//        requestDTO.setDescription("Apple MacBook Pro 14 inch");
//        requestDTO.setStartPrice(new BigDecimal("1200.00"));
//        requestDTO.setCategory("Electronics");
//        requestDTO.setImageUrls(List.of("https://example.com/images/macbook.jpg"));
//
//        sellerDTO = new UserResponseDTO();
//        sellerDTO.setUserId(1);
//        sellerDTO.setName("John Seller");
//        sellerDTO.setRole("SELLER");
//    }
//
//    // CREATE PRODUCT
//    @Nested
//    @DisplayName("createProduct()")
//    class CreateProduct {
//
//        @Test
//        @DisplayName("Should create product successfully when seller is valid")
//        void shouldCreateProductSuccessfully() {
//            when(userServiceClient.getUserById(eq(1), anyString())).thenReturn(sellerDTO); // ← updated
//            when(productRepo.save(any(Product.class))).thenReturn(product);
//            when(imageRepo.saveAll(anyList())).thenReturn(List.of());
//            when(imageRepo.findByProduct_ProductId(1))
//                    .thenReturn(List.of(Image.builder()
//                            .imageId(1).product(product)
//                            .url("https://example.com/images/macbook.jpg").build()));
//
//            ProductResponseDTO response = productService.createProduct(requestDTO, TEST_TOKEN); // ← updated
//
//            assertThat(response).isNotNull();
//            assertThat(response.getTitle()).isEqualTo("MacBook Pro M2");
//            assertThat(response.getStatus()).isEqualTo("AVAILABLE");
//            assertThat(response.getSellerId()).isEqualTo(1);
//            verify(productRepo, times(1)).save(any(Product.class));
//        }
//
//        @Test
//        @DisplayName("Should create product even when User service is unavailable (graceful skip)")
//        void shouldCreateProductWhenUserServiceDown() {
//            when(userServiceClient.getUserById(eq(1), anyString()))
//                    .thenThrow(new RuntimeException("User service down")); // ← updated
//            when(productRepo.save(any(Product.class))).thenReturn(product);
//            when(imageRepo.findByProduct_ProductId(1)).thenReturn(List.of());
//
//            ProductResponseDTO response = productService.createProduct(requestDTO, TEST_TOKEN); // ← updated
//
//            assertThat(response).isNotNull();
//            assertThat(response.getTitle()).isEqualTo("MacBook Pro M2");
//            verify(productRepo, times(1)).save(any(Product.class));
//        }
//
//        @Test
//        @DisplayName("Should create product with no images when imageUrls is null")
//        void shouldCreateProductWithNoImages() {
//            requestDTO.setImageUrls(null);
//            when(userServiceClient.getUserById(eq(1), anyString())).thenReturn(sellerDTO); // ← updated
//            when(productRepo.save(any(Product.class))).thenReturn(product);
//            when(imageRepo.findByProduct_ProductId(1)).thenReturn(List.of());
//
//            ProductResponseDTO response = productService.createProduct(requestDTO, TEST_TOKEN); // ← updated
//
//            assertThat(response.getImageUrls()).isEmpty();
//            verify(imageRepo, never()).saveAll(anyList());
//        }
//    }
//
//    // GET PRODUCT BY ID
//    @Nested
//    @DisplayName("getProductById()")
//    class GetProductById {
//
//        @Test
//        @DisplayName("Should return product when found")
//        void shouldReturnProductWhenFound() {
//            when(productRepo.findById(1)).thenReturn(Optional.of(product));
//            when(imageRepo.findByProduct_ProductId(1)).thenReturn(List.of());
//
//            ProductResponseDTO response = productService.getProductById(1);
//
//            assertThat(response).isNotNull();
//            assertThat(response.getProductId()).isEqualTo(1);
//            assertThat(response.getTitle()).isEqualTo("MacBook Pro M2");
//        }
//
//        @Test
//        @DisplayName("Should throw ResourceNotFoundException when product not found")
//        void shouldThrowWhenProductNotFound() {
//            when(productRepo.findById(999)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> productService.getProductById(999))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining("999");
//        }
//    }
//
//    // GET ALL PRODUCTS
//    @Nested
//    @DisplayName("getAllProducts()")
//    class GetAllProducts {
//
//        @Test
//        @DisplayName("Should return all products")
//        void shouldReturnAllProducts() {
//            Product p2 = Product.builder().productId(2).sellerId(2)
//                    .title("Guitar").startPrice(new BigDecimal("500.00"))
//                    .status("AVAILABLE").build();
//            when(productRepo.findAll()).thenReturn(List.of(product, p2));
//            when(imageRepo.findByProduct_ProductId(anyInt())).thenReturn(List.of());
//
//            List<ProductResponseDTO> result = productService.getAllProducts();
//
//            assertThat(result).hasSize(2);
//            assertThat(result).extracting(ProductResponseDTO::getTitle)
//                    .containsExactlyInAnyOrder("MacBook Pro M2", "Guitar");
//        }
//
//        @Test
//        @DisplayName("Should return empty list when no products exist")
//        void shouldReturnEmptyListWhenNoProducts() {
//            when(productRepo.findAll()).thenReturn(List.of());
//
//            List<ProductResponseDTO> result = productService.getAllProducts();
//
//            assertThat(result).isEmpty();
//        }
//    }
//
//    // GET BY SELLER
//    @Nested
//    @DisplayName("getProductsBySeller()")
//    class GetProductsBySeller {
//
//        @Test
//        @DisplayName("Should return products for given seller")
//        void shouldReturnProductsForSeller() {
//            when(productRepo.findBySellerId(1)).thenReturn(List.of(product));
//            when(imageRepo.findByProduct_ProductId(1)).thenReturn(List.of());
//
//            List<ProductResponseDTO> result = productService.getProductsBySeller(1);
//
//            assertThat(result).hasSize(1);
//            assertThat(result.get(0).getSellerId()).isEqualTo(1);
//        }
//
//        @Test
//        @DisplayName("Should return empty list for seller with no products")
//        void shouldReturnEmptyForSellerWithNoProducts() {
//            when(productRepo.findBySellerId(99)).thenReturn(List.of());
//
//            List<ProductResponseDTO> result = productService.getProductsBySeller(99);
//
//            assertThat(result).isEmpty();
//        }
//    }
//
//    // UPDATE PRODUCT
//    @Nested
//    @DisplayName("updateProduct()")
//    class UpdateProduct {
//
//        @Test
//        @DisplayName("Should update product successfully")
//        void shouldUpdateProductSuccessfully() {
//            requestDTO.setTitle("MacBook Pro M2 - Updated");
//            requestDTO.setStartPrice(new BigDecimal("999.00"));
//            Product updated = Product.builder().productId(1).sellerId(1)
//                    .title("MacBook Pro M2 - Updated")
//                    .startPrice(new BigDecimal("999.00"))
//                    .category("Electronics").status("AVAILABLE").build();
//
//            when(productRepo.findById(1)).thenReturn(Optional.of(product));
//            when(imageRepo.findByProduct_ProductId(1)).thenReturn(List.of());
//            when(productRepo.save(any(Product.class))).thenReturn(updated);
//
//            ProductResponseDTO response = productService.updateProduct(1, requestDTO);
//
//            assertThat(response.getTitle()).isEqualTo("MacBook Pro M2 - Updated");
//            assertThat(response.getStartPrice()).isEqualByComparingTo("999.00");
//        }
//
//        @Test
//        @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
//        void shouldThrowWhenUpdatingNonExistentProduct() {
//            when(productRepo.findById(999)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> productService.updateProduct(999, requestDTO))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining("999");
//        }
//    }
//
//    // DELETE PRODUCT
//    @Nested
//    @DisplayName("deleteProduct()")
//    class DeleteProduct {
//
//        @Test
//        @DisplayName("Should delete product successfully")
//        void shouldDeleteProductSuccessfully() {
//            when(productRepo.findById(1)).thenReturn(Optional.of(product));
//            doNothing().when(productRepo).delete(product);
//
//            assertThatCode(() -> productService.deleteProduct(1)).doesNotThrowAnyException();
//            verify(productRepo, times(1)).delete(product);
//        }
//
//        @Test
//        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
//        void shouldThrowWhenDeletingNonExistentProduct() {
//            when(productRepo.findById(999)).thenReturn(Optional.empty());
//
//            assertThatThrownBy(() -> productService.deleteProduct(999))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining("999");
//
//            verify(productRepo, never()).delete(any());
//        }
//    }
//}