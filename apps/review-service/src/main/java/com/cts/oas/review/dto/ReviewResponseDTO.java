package com.cts.oas.review.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Long reviewId;
    private Long userId;
    private String reviewerName;   // buyer's display name
    private Long targetUserId;
    private Long auctionId;
    private Integer rating;
    private String comment;
    private LocalDateTime date;
}