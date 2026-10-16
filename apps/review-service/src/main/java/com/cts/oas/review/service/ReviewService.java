package com.cts.oas.review.service;

import com.cts.oas.review.dto.*;
import java.util.List;

public interface ReviewService {
    ReviewResponseDTO createReview(Long userId, ReviewRequestDTO request);
    ReviewResponseDTO getReviewById(Long reviewId);
    List<ReviewResponseDTO> getReviewsByTargetUser(Long targetUserId);
    List<ReviewResponseDTO> getReviewsByUser(Long userId);   // reviews given BY a user
    void deleteReview(Long reviewId);
    List<ReviewResponseDTO> getAllReviews();
}