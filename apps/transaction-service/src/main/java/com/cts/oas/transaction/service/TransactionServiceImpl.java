package com.cts.oas.transaction.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cts.oas.transaction.client.AuctionClient;
import com.cts.oas.transaction.dto.TransactionRequestDTO;
import com.cts.oas.transaction.dto.TransactionResponseDTO;
import com.cts.oas.transaction.exception.TransactionNotFoundException;
import com.cts.oas.transaction.model.Transaction;
import com.cts.oas.transaction.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuctionClient auctionClient;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Override
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request) {
        Stripe.apiKey = stripeSecretKey;
        String currency = (request.getCurrency() != null && !request.getCurrency().isBlank())
                ? request.getCurrency().toLowerCase()
                : "inr";

        long amountInCents = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        Session session;
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl + "?cancelled=true")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency)
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Auction #" + request.getAuctionId())
                                                                    .setDescription("Payment for winning bid by Buyer #" + request.getBuyerId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            session = Session.create(params);

        } catch (StripeException e) {
            throw new RuntimeException("Stripe session creation failed: " + e.getMessage(), e);
        }

        Transaction transaction = Transaction.builder()
                .buyerId(request.getBuyerId())
                .auctionId(request.getAuctionId())
                .amount(request.getAmount())
                .paymentStatus("PENDING")
                .paymentDate(LocalDateTime.now())
                .stripeSessionId(session.getId())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        TransactionResponseDTO response = mapToResponse(saved);
        response.setCheckoutUrl(session.getUrl());
        response.setStripeSessionId(session.getId());
        return response;
    }

    @Override
    public TransactionResponseDTO confirmPayment(String sessionId) {
        Transaction transaction = transactionRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "No transaction found for Stripe session: " + sessionId));

        Session session;
        try {
            SessionRetrieveParams params = SessionRetrieveParams.builder().build();
            session = Session.retrieve(sessionId, params, null);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve Stripe session: " + e.getMessage(), e);
        }

        String stripeStatus = session.getPaymentStatus();
        String newStatus = "paid".equals(stripeStatus) ? "SUCCESS" : "FAILED";

        transaction.setPaymentStatus(newStatus);
        transaction.setPaymentDate(LocalDateTime.now());
        Transaction updated = transactionRepository.save(transaction);

        if ("SUCCESS".equals(newStatus)) {
            notifySeller(transaction.getAuctionId(), transaction.getBuyerId());
            try {
                auctionClient.updateAuctionStatus(transaction.getAuctionId().intValue(), "SOLD");
                System.out.println("[AUCTION] Auction #" + transaction.getAuctionId() + " marked as SOLD ✅");
            } catch (Exception e) {
                System.out.println("[WARN] Could not update auction status: " + e.getMessage());
            }
        }

        return mapToResponse(updated);
    }

    @Override
    public TransactionResponseDTO getTransactionById(Long transactionId) {
        Transaction t = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with ID: " + transactionId));
        return mapToResponse(t);
    }

    @Override
    public List<TransactionResponseDTO> getTransactionsByBuyer(Long buyerId) {
        return transactionRepository.findByBuyerId(buyerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponseDTO getTransactionByAuction(Long auctionId) {
        Transaction t = transactionRepository.findByAuctionId(auctionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "No transaction found for auction ID: " + auctionId));
        return mapToResponse(t);
    }

    @Override
    public boolean hasCompletedTransaction(Long buyerId, Long auctionId) {
        return transactionRepository
                .findByBuyerIdAndAuctionIdAndPaymentStatus(buyerId, auctionId, "SUCCESS")
                .isPresent();
    }

    // ← NEW: Admin — get all transactions
    @Override
    public List<TransactionResponseDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void notifySeller(Long auctionId, Long buyerId) {
        System.out.println("[NOTIFICATION] Seller notified for AuctionID: " + auctionId + " | BuyerID: " + buyerId);
    }

    private TransactionResponseDTO mapToResponse(Transaction t) {
        return TransactionResponseDTO.builder()
                .transactionId(t.getTransactionId())
                .buyerId(t.getBuyerId())
                .auctionId(t.getAuctionId())
                .amount(t.getAmount())
                .paymentStatus(t.getPaymentStatus())
                .paymentDate(t.getPaymentDate())
                .stripeSessionId(t.getStripeSessionId())
                .build();
    }
}