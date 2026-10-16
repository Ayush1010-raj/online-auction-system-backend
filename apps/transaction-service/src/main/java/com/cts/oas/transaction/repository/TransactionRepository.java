package com.cts.oas.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.oas.transaction.model.Transaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBuyerId(Long buyerId);

    Optional<Transaction> findByAuctionId(Long auctionId);

    Optional<Transaction> findByBuyerIdAndAuctionIdAndPaymentStatus(Long buyerId, Long auctionId, String paymentStatus);

    Optional<Transaction> findByStripeSessionId(String stripeSessionId);
}