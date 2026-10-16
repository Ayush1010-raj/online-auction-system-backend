package com.cts.oas.bid.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cts.oas.bid.client.AuctionClient;
import com.cts.oas.bid.dto.AuctionResponseDTO;
import com.cts.oas.bid.dto.BidRequestDTO;
import com.cts.oas.bid.dto.BidResponseDTO;
import com.cts.oas.bid.dto.NotificationResponseDTO;
import com.cts.oas.bid.exception.AuctionNotFoundException;
import com.cts.oas.bid.exception.BidValidationException;
import com.cts.oas.bid.model.Bid;
import com.cts.oas.bid.model.Notification;
import com.cts.oas.bid.repository.BidRepository;
import com.cts.oas.bid.repository.NotificationRepository;

@Service
public class BidService {

    private static final Logger log = LoggerFactory.getLogger(BidService.class);

    private final BidRepository          bidRepository;
    private final AuctionClient          auctionClient;
    private final NotificationRepository notificationRepository;

    public BidService(BidRepository bidRepository,
                      AuctionClient auctionClient,
                      NotificationRepository notificationRepository) {
        this.bidRepository          = bidRepository;
        this.auctionClient          = auctionClient;
        this.notificationRepository = notificationRepository;
    }

    // ── Place a bid ───────────────────────────────────────────────────────────

    public BidResponseDTO placeBid(Integer buyerId, BidRequestDTO bidRequestDTO) {

        Integer    auctionId = bidRequestDTO.getAuctionId();
        BigDecimal amount    = bidRequestDTO.getAmount();

        log.info("placeBid called: buyerId={}, auctionId={}, amount={}", buyerId, auctionId, amount);

        AuctionResponseDTO auction = auctionClient.getAuctionById(auctionId);
        if (auction == null) {
            throw new AuctionNotFoundException("Auction not found: " + auctionId);
        }

        log.info("Auction found: auctionId={}, status={}", auctionId, auction.getStatus());

        if (!auction.getStatus().equals("ACTIVE")) {
            throw new BidValidationException("Auction is not active");
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(auction.getEndDate())) {
            throw new BidValidationException("Auction was ended");
        }
        if (now.isBefore(auction.getStartDate())) {
            throw new BidValidationException("Auction not yet started");
        }

        BigDecimal currentBid = auction.getCurrentBid();
        if (currentBid != null && amount.compareTo(currentBid) <= 0) {
            throw new BidValidationException("Bid must be higher than current bid");
        }

        // Remember who was previously leading
        Integer previousBidderId = bidRepository
                .findHighestBidByAuctionId(auctionId)
                .map(Bid::getBuyerId)
                .orElse(null);

        // Save the new bid
        Bid bid = new Bid();
        bid.setAmount(amount);
        bid.setAuctionId(auctionId);
        bid.setBidTime(now);
        bid.setBuyerId(buyerId);
        Bid savedBid = bidRepository.save(bid);
        log.info("Bid saved: bidId={}", savedBid.getBidId());

        // Notify the outbid buyer (only if someone else was leading, not the same person)
        if (previousBidderId != null && !previousBidderId.equals(buyerId)) {
            log.warn("OUTBID: User #{} has been outbid on Auction #{}", previousBidderId, auctionId);

            String title   = (auction.getProductTitle() != null && !auction.getProductTitle().isBlank())
                             ? auction.getProductTitle()
                             : "Auction #" + auctionId;
            String message = "You've been outbid on \"" + title
                             + "\" — new highest bid is ₹" + amount.toPlainString();

            Notification notification = Notification.builder()
                    .userId(previousBidderId)
                    .auctionId(auctionId)
                    .message(message)
                    .read(false)
                    .createdAt(now)
                    .build();
            notificationRepository.save(notification);
            log.info("Outbid notification saved for userId={}", previousBidderId);
        }

        // Update auction current bid
        auctionClient.updateCurrentBid(auctionId, amount);
        log.info("Auction currentBid updated to {}", amount);

        return BidResponseDTO.builder()
                .bidId(savedBid.getBidId())
                .auctionId(auctionId)
                .buyerId(buyerId)
                .amount(amount)
                .bidTime(now)
                .message("Bid placed successfully! You are the highest bidder.")
                .build();
    }

    // ── Get bids ──────────────────────────────────────────────────────────────

    public List<Bid> getBidsByAuction(Integer auctionId) {
        return bidRepository.findByAuctionIdOrderByAmountDesc(auctionId);
    }

    // ── Notification helpers ──────────────────────────────────────────────────

    /** Returns all unread outbid notifications for a buyer, newest first */
    public List<NotificationResponseDTO> getUnreadNotifications(Integer userId) {
        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapNotification)
                .collect(Collectors.toList());
    }

    /** Mark a single notification as read */
    public void markNotificationRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    /** Mark ALL unread notifications for a user as read */
    public void markAllNotificationsRead(Integer userId) {
        List<Notification> unread =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponseDTO mapNotification(Notification n) {
        return NotificationResponseDTO.builder()
                .notificationId(n.getNotificationId())
                .userId(n.getUserId())
                .auctionId(n.getAuctionId())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
