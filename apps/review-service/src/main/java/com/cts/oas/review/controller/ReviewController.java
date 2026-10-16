package com.cts.oas.review.controller;

import com.cts.oas.review.dto.*;
import com.cts.oas.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO request,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(Long.parseLong(userId), request));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> getById(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewById(reviewId));
    }

    @GetMapping("/user/{targetUserId}")
    public ResponseEntity<List<ReviewResponseDTO>> getByUser(@PathVariable Long targetUserId) {
        return ResponseEntity.ok(reviewService.getReviewsByTargetUser(targetUserId));
    }

    /** Reviews given BY the current user (buyer history) */
    @GetMapping("/given")
    public ResponseEntity<List<ReviewResponseDTO>> getGiven(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(Long.parseLong(userId)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Role") String role) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok("Review deleted successfully.");
    }

    // ← NEW: Admin — get all reviews
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllReviews(
            @RequestHeader("X-User-Role") String role) {

        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can view all reviews.");
        }
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
}