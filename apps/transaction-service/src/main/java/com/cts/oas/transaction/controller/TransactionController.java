package com.cts.oas.transaction.controller;

import com.cts.oas.transaction.dto.*;
import com.cts.oas.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @Valid @RequestBody TransactionRequestDTO request,
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("BUYER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only BUYER role can create transactions.");
        }
        request.setBuyerId(Long.parseLong(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request));
    }

    @GetMapping("/confirm")
    public ResponseEntity<TransactionResponseDTO> confirmPayment(
            @RequestParam("session_id") String sessionId) {
        return ResponseEntity.ok(transactionService.confirmPayment(sessionId));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getById(
            @PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @GetMapping("/buyer")
    public ResponseEntity<?> getMyTransactions(
            @RequestHeader("X-User-Id")   String userId,
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("BUYER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only BUYER role can view transactions.");
        }
        return ResponseEntity.ok(
                transactionService.getTransactionsByBuyer(Long.parseLong(userId)));
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<TransactionResponseDTO> getByAuction(
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(transactionService.getTransactionByAuction(auctionId));
    }

    @GetMapping("/verify/{buyerId}/{auctionId}")
    public ResponseEntity<Boolean> verifyTransaction(
            @PathVariable Long buyerId,
            @PathVariable Long auctionId) {
        return ResponseEntity.ok(
                transactionService.hasCompletedTransaction(buyerId, auctionId));
    }

    // ← NEW: Admin — get all transactions
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllTransactions(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can view all transactions.");
        }
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }
}