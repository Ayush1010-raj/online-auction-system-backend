package com.cts.oas.review.service;

import com.cts.oas.review.client.TransactionClient;
import com.cts.oas.review.dto.*;
import com.cts.oas.review.model.Review;
import com.cts.oas.review.exception.*;
import com.cts.oas.review.repository.ReviewRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TransactionClient transactionClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review sampleReview;
    private ReviewRequestDTO sampleRequest;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        sampleReview = Review.builder()
                .reviewId(1L)
                .userId(1L)
                .targetUserId(10L)
                .rating(5)
                .comment("Great seller!")
                .date(LocalDateTime.now())
                .build();

        sampleRequest = new ReviewRequestDTO();
        sampleRequest.setTargetUserId(10L);
        sampleRequest.setAuctionId(101L);
        sampleRequest.setRating(5);
        sampleRequest.setComment("Great seller!");
    }

    @Test
    @DisplayName("Should create review successfully when transaction exists")
    void testCreateReview_Success() {
        when(transactionClient.hasCompletedTransaction(1L, 101L)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndAuctionId(1L, 10L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(sampleReview);

        ReviewResponseDTO result = reviewService.createReview(USER_ID, sampleRequest);

        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals("Great seller!", result.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw ReviewNotAllowedException when no completed transaction")
    void testCreateReview_NoTransaction() {
        when(transactionClient.hasCompletedTransaction(1L, 101L)).thenReturn(false);

        assertThrows(ReviewNotAllowedException.class,
                () -> reviewService.createReview(USER_ID, sampleRequest));
    }

    @Test
    @DisplayName("Should throw ReviewNotAllowedException when buyer won different auction")
    void testCreateReview_WrongAuction() {
        ReviewRequestDTO wrongAuctionRequest = new ReviewRequestDTO();
        wrongAuctionRequest.setTargetUserId(10L);
        wrongAuctionRequest.setAuctionId(999L);
        wrongAuctionRequest.setRating(5);
        wrongAuctionRequest.setComment("Wrong auction");

        when(transactionClient.hasCompletedTransaction(1L, 999L)).thenReturn(false);

        assertThrows(ReviewNotAllowedException.class,
                () -> reviewService.createReview(USER_ID, wrongAuctionRequest));
    }

    @Test
    @DisplayName("Should throw ReviewNotAllowedException when user reviews themselves")
    void testCreateReview_SelfReview() {
        sampleRequest.setTargetUserId(1L); // same as USER_ID

        assertThrows(ReviewNotAllowedException.class,
                () -> reviewService.createReview(USER_ID, sampleRequest));
    }

    @Test
    @DisplayName("Should throw ReviewNotAllowedException on duplicate review")
    void testCreateReview_Duplicate() {
        when(transactionClient.hasCompletedTransaction(1L, 101L)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndAuctionId(1L, 10L)).thenReturn(true);

        assertThrows(ReviewNotAllowedException.class,
                () -> reviewService.createReview(USER_ID, sampleRequest));
    }

    @Test
    @DisplayName("Should return review when found by ID")
    void testGetReviewById_Found() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(sampleReview));

        ReviewResponseDTO result = reviewService.getReviewById(1L);
        assertEquals(1L, result.getReviewId());
    }

    @Test
    @DisplayName("Should throw ReviewNotFoundException when review ID not found")
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.getReviewById(99L));
    }

    @Test
    @DisplayName("Should delete review when it exists")
    void testDeleteReview_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(sampleReview));

        assertDoesNotThrow(() -> reviewService.deleteReview(1L));
        verify(reviewRepository, times(1)).delete(sampleReview);
    }

    @Test
    @DisplayName("Should throw ReviewNotFoundException when deleting non-existent review")
    void testDeleteReview_NotFound() {
        when(reviewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.deleteReview(99L));
    }
}