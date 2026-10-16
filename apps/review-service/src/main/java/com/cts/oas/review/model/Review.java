package com.cts.oas.review.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @Column(nullable = false)
    private Long userId;

    /** Display name of the reviewer (buyer) — stored at review time */
    @Column(length = 200)
    private String reviewerName;

    @Column(nullable = false)
    private Long targetUserId;

    /** The auction this review is for — allows one review per buyer per auction */
    @Column(nullable = true)
    private Long auctionId;

    @Column(nullable = false)
    private Integer rating;       // 1 to 5

    @Column(length = 1000)
    private String comment;

    private LocalDateTime date;
}