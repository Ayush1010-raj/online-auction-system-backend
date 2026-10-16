package com.cts.oas.review.service;

import com.cts.oas.review.client.TransactionClient;
import com.cts.oas.review.dto.*;
import com.cts.oas.review.model.Review;
import com.cts.oas.review.exception.*;
import com.cts.oas.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TransactionClient transactionClient;

    @Override
    public ReviewResponseDTO createReview(Long userId, ReviewRequestDTO request) {

        if (userId.equals(request.getTargetUserId())) {
            throw new ReviewNotAllowedException("You cannot review yourself.");
        }

        Boolean hasTransaction = transactionClient.hasCompletedTransaction(
                userId, request.getAuctionId());

        if (!Boolean.TRUE.equals(hasTransaction)) {
            throw new ReviewNotAllowedException(
                    "You can only submit a review after a completed transaction for this auction.");
        }

        // One review per buyer per AUCTION — allows reviewing same seller from different auctions
        if (reviewRepository.existsByUserIdAndAuctionId(userId, request.getAuctionId())) {
            throw new ReviewNotAllowedException(
                    "You have already submitted a review for this auction.");
        }

        Review review = Review.builder()
                .userId(userId)
                .reviewerName(request.getReviewerName())
                .targetUserId(request.getTargetUserId())
                .auctionId(request.getAuctionId())
                .rating(request.getRating())
                .comment(request.getComment())
                .date(LocalDateTime.now())
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    public ReviewResponseDTO getReviewById(Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found with ID: " + reviewId));
        return mapToResponse(r);
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByTargetUser(Long targetUserId) {
        return reviewRepository.findByTargetUserId(targetUserId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewResponseDTO> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(
                        "Review not found with ID: " + reviewId));
        reviewRepository.delete(r);
    }

    // ← NEW: Admin — get all reviews
    @Override
    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponseDTO mapToResponse(Review r) {
        return ReviewResponseDTO.builder()
                .reviewId(r.getReviewId())
                .userId(r.getUserId())
                .reviewerName(r.getReviewerName())
                .targetUserId(r.getTargetUserId())
                .auctionId(r.getAuctionId())
                .rating(r.getRating())
                .comment(r.getComment())
                .date(r.getDate())
                .build();
    }
}