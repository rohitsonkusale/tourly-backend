package com.tourly.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tourly.review.entity.Review;
import com.tourly.review.enums.ReviewStatus;
import com.tourly.review.enums.TargetType;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // =====================================
    // DUPLICATE CHECK
    // =====================================

    /**
     * Check if a review already exists for a given reviewer, target type, and booking.
     * Used to prevent duplicate reviews.
     */
    boolean existsByReviewerIdAndTargetTypeAndBookingId(Long reviewerId, TargetType targetType, Long bookingId);

    // =====================================
    // PUBLIC LISTING (paginated)
    // =====================================

    /**
     * Find all reviews for a specific target (by type and id) with a given status.
     * Supports pagination for public review listings.
     */
    Page<Review> findByTargetTypeAndTargetIdAndStatus(TargetType targetType, Long targetId, ReviewStatus status, Pageable pageable);

    // =====================================
    // MY REVIEWS FOR A BOOKING
    // =====================================

    /**
     * Find reviews by a specific reviewer for a specific booking.
     * Used to retrieve the current user's reviews for a particular booking.
     */
    List<Review> findByReviewerIdAndBookingId(Long reviewerId, Long bookingId);

    // =====================================
    // AGGREGATE: AVERAGE RATING
    // =====================================

    /**
     * Compute the average rating for published reviews of a specific target.
     * Returns Optional.empty() if no published reviews exist for the target.
     */
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        WHERE r.targetType = :targetType
          AND r.targetId = :targetId
          AND r.status = :status
    """)
    Optional<Double> computeAverageRating(@Param("targetType") TargetType targetType,
                                          @Param("targetId") Long targetId,
                                          @Param("status") ReviewStatus status);

    // =====================================
    // AGGREGATE: COUNT PUBLISHED REVIEWS
    // =====================================

    /**
     * Count the number of published reviews for a specific target.
     */
    @Query("""
        SELECT COUNT(r) FROM Review r
        WHERE r.targetType = :targetType
          AND r.targetId = :targetId
          AND r.status = :status
    """)
    long countPublishedReviews(@Param("targetType") TargetType targetType,
                               @Param("targetId") Long targetId,
                               @Param("status") ReviewStatus status);
}
