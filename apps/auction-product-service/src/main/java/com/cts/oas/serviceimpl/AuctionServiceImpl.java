package com.cts.oas.serviceimpl;

import com.cts.oas.dto.request.AuctionRequestDTO;
import com.cts.oas.dto.response.AuctionResponseDTO;
import com.cts.oas.exception.BadRequestException;
import com.cts.oas.exception.ResourceNotFoundException;
import com.cts.oas.model.Auction;
import com.cts.oas.model.Image;
import com.cts.oas.model.Product;
import com.cts.oas.repository.AuctionRepository;
import com.cts.oas.repository.ProductRepository;
import com.cts.oas.service.AuctionService;
import com.cts.oas.util.AuctionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepo;
    private final ProductRepository productRepo;

    @Override
    @Transactional
    public AuctionResponseDTO createAuction(AuctionRequestDTO dto) {
        log.debug("Creating auction for productId={}", dto.getProductId());

        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            log.warn("Auction creation failed: end date {} is not after start date {}", dto.getEndDate(), dto.getStartDate());
            throw new BadRequestException("End date must be after start date");
        }

        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found with id={}", dto.getProductId());
                    return new ResourceNotFoundException("Product not found with id: " + dto.getProductId());
                });

        auctionRepo.findByProduct_ProductId(dto.getProductId()).ifPresent(a -> {
            log.warn("Duplicate auction attempt for productId={}", dto.getProductId());
            throw new BadRequestException("An auction already exists for product id: " + dto.getProductId());
        });

        Auction auction = Auction.builder()
                .product(product)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .currentBid(product.getStartPrice())
                .status(AuctionStatus.ACTIVE)
                .build();

        product.setStatus("IN_AUCTION");
        productRepo.save(product);
        log.debug("Product id={} status updated to IN_AUCTION", product.getProductId());

        Auction saved = auctionRepo.save(auction);
        log.info("Auction id={} created for productId={}", saved.getAuctionId(), dto.getProductId());
        return mapToResponse(saved);
    }
    @Transactional
    @Override
    public AuctionResponseDTO updateCurrentBid(Integer auctionId, BigDecimal amount) {
        Auction auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Auction not found with id: " + auctionId));
        auction.setCurrentBid(amount);
        return mapToResponse(auctionRepo.save(auction));
    }
    @Override
    public AuctionResponseDTO getAuctionById(Integer auctionId) {
        log.debug("Fetching auction id={}", auctionId);
        Auction auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> {
                    log.error("Auction not found with id={}", auctionId);
                    return new ResourceNotFoundException("Auction not found with id: " + auctionId);
                });
        return mapToResponse(auction);
    }

    @Override
    public AuctionResponseDTO getAuctionByProductId(Integer productId) {
        log.debug("Fetching auction for productId={}", productId);
        Auction auction = auctionRepo.findByProduct_ProductId(productId)
                .orElseThrow(() -> {
                    log.error("No auction found for productId={}", productId);
                    return new ResourceNotFoundException("No auction found for product id: " + productId);
                });
        return mapToResponse(auction);
    }

    @Override
    public List<AuctionResponseDTO> getAllAuctions() {
        log.debug("Fetching all auctions");
        return auctionRepo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuctionResponseDTO> getAuctionsByStatus(AuctionStatus status) {
        log.debug("Fetching auctions with status={}", status);
        return auctionRepo.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuctionResponseDTO updateAuctionStatus(Integer auctionId, AuctionStatus status) {
        log.debug("Updating auction id={} to status={}", auctionId, status);
        Auction auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> {
                    log.error("Auction not found with id={}", auctionId);
                    return new ResourceNotFoundException("Auction not found with id: " + auctionId);
                });

        AuctionStatus oldStatus = auction.getStatus();
        auction.setStatus(status);

        if (status == AuctionStatus.SOLD) {
            auction.getProduct().setStatus("SOLD");
            productRepo.save(auction.getProduct());
            log.info("Product id={} marked as SOLD", auction.getProduct().getProductId());
        } else if (status == AuctionStatus.CANCELLED) {
            auction.getProduct().setStatus("AVAILABLE");
            productRepo.save(auction.getProduct());
            log.info("Product id={} reverted to AVAILABLE after auction cancellation", auction.getProduct().getProductId());
        }

        log.info("Auction id={} status changed from {} to {}", auctionId, oldStatus, status);
        return mapToResponse(auctionRepo.save(auction));
    }

    @Override
    @Transactional
    public void deleteAuction(Integer auctionId) {
        log.debug("Deleting auction id={}", auctionId);
        Auction auction = auctionRepo.findById(auctionId)
                .orElseThrow(() -> {
                    log.error("Auction not found with id={}", auctionId);
                    return new ResourceNotFoundException("Auction not found with id: " + auctionId);
                });
        auctionRepo.delete(auction);
        log.info("Auction id={} deleted", auctionId);
    }

    // Mapper
    private AuctionResponseDTO mapToResponse(Auction a) {
        AuctionResponseDTO dto = new AuctionResponseDTO();
        dto.setAuctionId(a.getAuctionId());
        dto.setProductId(a.getProduct().getProductId());
        dto.setSellerId(a.getProduct().getSellerId());
        dto.setProductTitle(a.getProduct().getTitle());
        dto.setCategory(a.getProduct().getCategory());
        dto.setStartDate(a.getStartDate());
        dto.setEndDate(a.getEndDate());
        dto.setCurrentBid(a.getCurrentBid());
        dto.setStatus(a.getStatus().name());

        // ← ADD THIS — image field is called "url" not "imageUrl"
        if (a.getProduct().getImages() != null) {
            dto.setImageUrls(
                    a.getProduct().getImages().stream()
                            .map(Image::getUrl)
                            .collect(java.util.stream.Collectors.toList())
            );
        }

        return dto;
    }
}