package com.cts.oas.bid.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cts.oas.bid.dto.BidRequestDTO;
import com.cts.oas.bid.dto.BidResponseDTO;
import com.cts.oas.bid.dto.NotificationResponseDTO;
import com.cts.oas.bid.model.Bid;
import com.cts.oas.bid.service.BidService;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/bids")
@Tag(name = "Bid Management", description = "APIs for placing bids, retrieving bid history, and outbid notifications")
public class BidController {

    private final BidService bidService;

    public BidController(BidService bidService) {
        this.bidService = bidService;
    }

    // ── Place a bid ───────────────────────────────────────────────────────────

    @Operation(summary = "Place a bid",
            description = "Allows an authenticated BUYER to place a bid on an active auction.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bid placed successfully",
                    content = @Content(schema = @Schema(implementation = BidResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "User is not a BUYER"),
            @ApiResponse(responseCode = "400", description = "Bid validation failed")
    })
    @PostMapping
    public ResponseEntity<?> placeBid(
            @Valid @RequestBody BidRequestDTO bidRequestDto,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id")   String userId) {

        log.info("POST /api/bids - userId={}, auctionId={}", userId, bidRequestDto.getAuctionId());

        if (!role.equals("BUYER")) {
            log.warn("Access denied - userId={} has role={}", userId, role);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only BUYER role can place bids.");
        }

        BidResponseDTO response = bidService.placeBid(Integer.parseInt(userId), bidRequestDto);
        log.info("Bid placed successfully - bidId={}", response.getBidId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Get bids for an auction ────────────────────────────────────────────────

    @Operation(summary = "Get bids by auction",
            description = "Returns all bids for a given auction sorted by amount descending.")
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<Bid>> getBidsByAuction(
            @Parameter(description = "Auction ID") @PathVariable Integer auctionId) {

        log.info("GET /api/bids/auction/{} - Fetching bids", auctionId);
        List<Bid> bids = bidService.getBidsByAuction(auctionId);
        log.info("Found {} bid(s) for auctionId={}", bids.size(), auctionId);
        return ResponseEntity.ok(bids);
    }

    // ── Outbid notifications ──────────────────────────────────────────────────

    @Operation(summary = "Get unread outbid notifications",
            description = "Returns all unread outbid notifications for the logged-in buyer.")
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @RequestHeader("X-User-Id") String userId) {

        log.info("GET /api/bids/notifications - userId={}", userId);
        List<NotificationResponseDTO> list =
                bidService.getUnreadNotifications(Integer.parseInt(userId));
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("PUT /api/bids/notifications/{}/read - userId={}", id, userId);
        bidService.markNotificationRead(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllRead(
            @RequestHeader("X-User-Id") String userId) {

        log.info("PUT /api/bids/notifications/read-all - userId={}", userId);
        bidService.markAllNotificationsRead(Integer.parseInt(userId));
        return ResponseEntity.ok().build();
    }
}
