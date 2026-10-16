package com.cts.oas.transaction_service.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.cts.oas.transaction.dto.*;
import com.cts.oas.transaction.exception.*;
import com.cts.oas.transaction.model.Transaction;
import com.cts.oas.transaction.repository.TransactionRepository;
import com.cts.oas.transaction.service.TransactionServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Transaction pendingTransaction;
    private Transaction successTransaction;

    @BeforeEach
    void setUp() {
        pendingTransaction = Transaction.builder()
                .transactionId(1L)
                .buyerId(1L)
                .auctionId(101L)
                .amount(new BigDecimal("2500.00"))
                .paymentStatus("PENDING")
                .paymentDate(LocalDateTime.now())
                .stripeSessionId("cs_test_dummy_session")
                .build();

        successTransaction = Transaction.builder()
                .transactionId(1L)
                .buyerId(1L)
                .auctionId(101L)
                .amount(new BigDecimal("2500.00"))
                .paymentStatus("SUCCESS")
                .paymentDate(LocalDateTime.now())
                .stripeSessionId("cs_test_dummy_session")
                .build();
    }

    @Test
    @DisplayName("Should throw TransactionNotFoundException when session ID not found")
    void testConfirmPayment_SessionNotFound() {
        when(transactionRepository.findByStripeSessionId("cs_test_invalid"))
                .thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.confirmPayment("cs_test_invalid"));
    }

    @Test
    @DisplayName("Should return transaction when found by ID")
    void testGetTransactionById_Found() {
        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(successTransaction));

        TransactionResponseDTO result = transactionService.getTransactionById(1L);

        assertEquals(1L, result.getTransactionId());
        assertEquals("SUCCESS", result.getPaymentStatus());
    }

    @Test
    @DisplayName("Should throw TransactionNotFoundException when ID not found")
    void testGetTransactionById_NotFound() {
        when(transactionRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(99L));
    }

    @Test
    @DisplayName("Should return list of transactions for valid buyer")
    void testGetTransactionsByBuyer_Found() {
        when(transactionRepository.findByBuyerId(1L))
                .thenReturn(List.of(successTransaction));

        List<TransactionResponseDTO> result = transactionService.getTransactionsByBuyer(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBuyerId());
    }

    @Test
    @DisplayName("Should throw TransactionNotFoundException when no transactions for buyer")
    void testGetTransactionsByBuyer_NotFound() {
        when(transactionRepository.findByBuyerId(99L))
                .thenReturn(List.of());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionsByBuyer(99L));
    }

    @Test
    @DisplayName("Should return transaction when found by auction ID")
    void testGetTransactionByAuction_Found() {
        when(transactionRepository.findByAuctionId(101L))
                .thenReturn(Optional.of(successTransaction));

        TransactionResponseDTO result = transactionService.getTransactionByAuction(101L);

        assertEquals(101L, result.getAuctionId());
    }

    @Test
    @DisplayName("Should throw TransactionNotFoundException when auction ID not found")
    void testGetTransactionByAuction_NotFound() {
        when(transactionRepository.findByAuctionId(999L))
                .thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionByAuction(999L));
    }

    @Test
    @DisplayName("Should return true when buyer has a SUCCESS transaction for auction")
    void testHasCompletedTransaction_True() {
        when(transactionRepository.findByBuyerIdAndAuctionIdAndPaymentStatus(1L, 101L, "SUCCESS"))
                .thenReturn(Optional.of(successTransaction));

        assertTrue(transactionService.hasCompletedTransaction(1L, 101L));
    }

    @Test
    @DisplayName("Should return false when no SUCCESS transaction exists")
    void testHasCompletedTransaction_False() {
        when(transactionRepository.findByBuyerIdAndAuctionIdAndPaymentStatus(2L, 102L, "SUCCESS"))
                .thenReturn(Optional.empty());

        assertFalse(transactionService.hasCompletedTransaction(2L, 102L));
    }

    @Test
    @DisplayName("Should return false when buyer won a different auction, not this one")
    void testHasCompletedTransaction_WrongAuction() {
        when(transactionRepository.findByBuyerIdAndAuctionIdAndPaymentStatus(1L, 999L, "SUCCESS"))
                .thenReturn(Optional.empty());

        assertFalse(transactionService.hasCompletedTransaction(1L, 999L));
    }
}