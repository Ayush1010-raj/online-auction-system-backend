package com.cts.oas.repository;

import com.cts.oas.model.Auction;
import com.cts.oas.util.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Integer> {
    List<Auction> findByStatus(AuctionStatus status);
    Optional<Auction> findByProduct_ProductId(Integer productId);

    @Query("SELECT a FROM Auction a WHERE a.status = :status AND a.endDate < :now")
    List<Auction> findExpiredAuctions(@Param("status") AuctionStatus status,
                                      @Param("now") LocalDateTime now);
}
