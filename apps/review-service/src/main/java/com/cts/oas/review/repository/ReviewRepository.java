package com.cts.oas.review.repository;

import com.cts.oas.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // view all reviews received by a specific user (seller dashboard)
    List<Review> findByTargetUserId(Long targetUserId);

    // view all reviews given BY a specific user (buyer history)
    List<Review> findByUserId(Long userId);

    // prevent duplicate: one review per buyer per AUCTION (not per seller)
    boolean existsByUserIdAndAuctionId(Long userId, Long auctionId);
}