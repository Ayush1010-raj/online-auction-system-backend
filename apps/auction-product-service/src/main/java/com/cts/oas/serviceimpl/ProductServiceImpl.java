package com.cts.oas.serviceimpl;

import com.cts.oas.dto.request.ProductRequestDTO;
import com.cts.oas.dto.response.ProductResponseDTO;
import com.cts.oas.exception.ResourceNotFoundException;
import com.cts.oas.model.Image;
import com.cts.oas.model.Product;
import com.cts.oas.repository.AuctionRepository;
import com.cts.oas.repository.ImageRepository;
import com.cts.oas.repository.ProductRepository;
import com.cts.oas.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final AuctionRepository auctionRepo;
    private final ImageRepository imageRepo;

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        log.info("Creating product for sellerId={}", dto.getSellerId());

        Product product = Product.builder()
                .sellerId(dto.getSellerId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startPrice(dto.getStartPrice())
                .category(dto.getCategory())
                .status("AVAILABLE")
                .build();

        Product saved = productRepo.save(product);

        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            List<Image> images = dto.getImageUrls().stream()
                    .map(url -> Image.builder().product(saved).url(url).build())
                    .collect(Collectors.toList());
            imageRepo.saveAll(images);
            saved.setImages(images);
        }

        return mapToResponse(saved);
    }

    @Override
    public ProductResponseDTO getProductById(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getProductsBySeller(Integer sellerId) {
        return productRepo.findBySellerId(sellerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDTO> getProductsByCategory(String category) {
        return productRepo.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Integer productId, ProductRequestDTO dto) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setStartPrice(dto.getStartPrice());
        product.setCategory(dto.getCategory());

        // Use collection management so orphanRemoval handles deletion cleanly
        // (avoids Hibernate double-delete conflict from calling imageRepo.deleteBy directly)
        if (product.getImages() == null) {
            product.setImages(new java.util.ArrayList<>());
        }
        product.getImages().clear();   // orphanRemoval will delete these from DB on flush

        if (dto.getImageUrls() != null && !dto.getImageUrls().isEmpty()) {
            dto.getImageUrls().forEach(url ->
                    product.getImages().add(Image.builder().product(product).url(url).build())
            );
        }

        return mapToResponse(productRepo.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Integer productId) {
        if (!productRepo.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        // Delete children directly in FK order — no entity loading, no cascade conflicts
        // 1. Images  (FK: image.ProductID → product.ProductID)
        imageRepo.deleteByProduct_ProductId(productId);

        // 2. Auction (FK: auction.ProductID → product.ProductID)
        auctionRepo.findByProduct_ProductId(productId)
                   .ifPresent(a -> auctionRepo.deleteById(a.getAuctionId()));

        // 3. Product
        productRepo.deleteById(productId);
    }

    private ProductResponseDTO mapToResponse(Product p) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(p.getProductId());
        dto.setSellerId(p.getSellerId());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setStartPrice(p.getStartPrice());
        dto.setCategory(p.getCategory());
        dto.setStatus(p.getStatus());

        List<String> urls = imageRepo.findByProduct_ProductId(p.getProductId())
                .stream()
                .map(Image::getUrl)
                .collect(Collectors.toList());
        dto.setImageUrls(urls);

        return dto;
    }
}