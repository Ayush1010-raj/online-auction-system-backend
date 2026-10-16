package com.cts.oas.transaction.service;

import java.util.List;

import com.cts.oas.transaction.dto.*;

public interface TransactionService {
    TransactionResponseDTO createTransaction(TransactionRequestDTO request);
    TransactionResponseDTO getTransactionById(Long transactionId);
    List<TransactionResponseDTO> getTransactionsByBuyer(Long buyerId);
    TransactionResponseDTO getTransactionByAuction(Long auctionId);
    boolean hasCompletedTransaction(Long buyerId, Long auctionId);  // called by review-service
    TransactionResponseDTO confirmPayment(String sessionId);
    List<TransactionResponseDTO> getAllTransactions();
}