package com.cts.oas.controller;

import com.cts.oas.dto.request.AuctionRequestDTO;
import com.cts.oas.dto.response.AuctionResponseDTO;
import com.cts.oas.service.AuctionService;
import com.cts.oas.util.AuctionStatus;
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

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
@Tag(name = "Auction", description = "Endpoints for managing auctions")
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<?> createAuction(
            @Valid @RequestBody AuctionRequestDTO dto,
            @RequestHeader("X-User-Role") String role) {  // ← add this

        log.info("POST /api/auctions - Creating auction for productId={}", dto.getProductId());

        // Only SELLER can create auction
        if (!role.equals("SELLER")) {
            log.warn("Access denied - role={} tried to create auction", role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only SELLER role can create auctions.");
        }

        AuctionResponseDTO response = auctionService.createAuction(dto);
        log.info("Auction created successfully with id={}", response.getAuctionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AuctionResponseDTO>> getAllAuctions() {
        log.info("GET /api/auctions - Fetching all auctions");
        return ResponseEntity.ok(auctionService.getAllAuctions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponseDTO> getAuctionById(
            @Parameter(description = "Auction ID", example = "1") @PathVariable Integer id) {
        log.info("GET /api/auctions/{} - Fetching auction", id);
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<AuctionResponseDTO> getAuctionByProduct(
            @Parameter(description = "Product ID", example = "1") @PathVariable Integer productId) {
        log.info("GET /api/auctions/product/{} - Fetching auction by productId", productId);
        return ResponseEntity.ok(auctionService.getAuctionByProductId(productId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AuctionResponseDTO>> getAuctionsByStatus(
            @Parameter(description = "Auction status", example = "ACTIVE") @PathVariable AuctionStatus status) {
        log.info("GET /api/auctions/status/{} - Fetching auctions by status", status);
        return ResponseEntity.ok(auctionService.getAuctionsByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AuctionResponseDTO> updateStatus(
            @Parameter(description = "Auction ID", example = "1") @PathVariable Integer id,
            @Parameter(description = "New status", example = "SOLD") @RequestParam AuctionStatus status) {
        log.info("PATCH /api/auctions/{}/status - Updating status to {}", id, status);
        AuctionResponseDTO response = auctionService.updateAuctionStatus(id, status);
        log.info("Auction id={} status updated to {}", id, status);
        return ResponseEntity.ok(response);
    }

    // ← NEW: Update current bid from bidding-service
    @PutMapping("/{id}/currentBid")
    public ResponseEntity<AuctionResponseDTO> updateCurrentBid(
            @PathVariable Integer id,
            @RequestParam BigDecimal amount) {
        log.info("PUT /api/auctions/{}/currentBid - Updating currentBid to {}", id, amount);
        AuctionResponseDTO response = auctionService.updateCurrentBid(id, amount);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuction(
            @Parameter(description = "Auction ID", example = "1") @PathVariable Integer id) {
        log.info("DELETE /api/auctions/{} - Deleting auction", id);
        auctionService.deleteAuction(id);
        log.info("Auction id={} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}