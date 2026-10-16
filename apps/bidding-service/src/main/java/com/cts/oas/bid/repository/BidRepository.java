package com.cts.oas.bid.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cts.oas.bid.model.Bid;

public interface BidRepository extends JpaRepository<Bid, Integer> {

    List<Bid> findByAuctionIdOrderByAmountDesc(Integer auctionId);

    List<Bid> findByBuyerId(Integer buyerId);

    @Query("SELECT b FROM Bid b WHERE b.auctionId = :auctionId ORDER BY b.amount DESC LIMIT 1")
    Optional<Bid> findHighestBidByAuctionId(Integer auctionId);

  
}
