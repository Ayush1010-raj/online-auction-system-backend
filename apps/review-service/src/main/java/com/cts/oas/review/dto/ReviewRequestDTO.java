package com.cts.oas.review.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    //  userId removed — extracted from JWT token instead

    @NotNull(message = "Target user ID is required")
    private Long targetUserId;

    @NotNull(message = "Auction ID is required")
    private Long auctionId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

    /** Reviewer's display name — sent by the frontend from the logged-in user's profile */
    @Size(max = 200)
    private String reviewerName;

}