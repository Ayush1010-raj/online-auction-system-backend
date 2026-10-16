package com.cts.oas.scheduler;

import com.cts.oas.model.Auction;
import com.cts.oas.repository.AuctionRepository;
import com.cts.oas.util.AuctionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks that run every 60 seconds to keep auction statuses in sync:
 *
 *  1. SCHEDULED → ACTIVE  : when startDate has been reached
 *  2. ACTIVE    → SOLD    : when endDate has passed AND currentBid > startPrice
 *  3. ACTIVE    → UNSOLD  : when endDate has passed AND no real bids were placed
 *
 * The linked product's status is updated accordingly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepo;

    // ─── 1. Activate scheduled auctions whose start time has arrived ──────────

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void activateScheduledAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> scheduled = auctionRepo.findByStatus(AuctionStatus.SCHEDULED);
        for (Auction auction : scheduled) {
            if (auction.getStartDate() != null && auction.getStartDate().isBefore(now)) {
                auction.setStatus(AuctionStatus.ACTIVE);
                auction.getProduct().setStatus("IN_AUCTION");
                auctionRepo.save(auction);
                log.info("AuctionScheduler: auction {} SCHEDULED → ACTIVE", auction.getAuctionId());
            }
        }
    }

    // ─── 2 & 3. Close expired ACTIVE auctions ────────────────────────────────

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expired = auctionRepo.findExpiredAuctions(AuctionStatus.ACTIVE, now);

        if (expired.isEmpty()) {
            log.debug("AuctionScheduler: no expired active auctions at {}", now);
            return;
        }

        log.info("AuctionScheduler: closing {} expired auction(s) at {}", expired.size(), now);

        for (Auction auction : expired) {
            boolean hasBid = auction.getCurrentBid() != null
                    && auction.getCurrentBid().compareTo(
                            auction.getProduct().getStartPrice()) > 0;

            AuctionStatus newAuctionStatus = hasBid ? AuctionStatus.SOLD : AuctionStatus.UNSOLD;
            String newProductStatus        = hasBid ? "SOLD" : "AVAILABLE";

            auction.setStatus(newAuctionStatus);
            auction.getProduct().setStatus(newProductStatus);

            auctionRepo.save(auction);

            log.info("AuctionScheduler: auction {} → {} | product {} → {}",
                    auction.getAuctionId(), newAuctionStatus,
                    auction.getProduct().getProductId(), newProductStatus);
        }
    }
}
