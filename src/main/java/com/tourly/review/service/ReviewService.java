package com.tourly.review.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tourly.review.dto.request.CreateReviewRequest;
import com.tourly.review.dto.request.ModerateReviewRequest;
import com.tourly.review.dto.request.UpdateReviewRequest;
import com.tourly.review.dto.response.MyBookingReviewsResponse;
import com.tourly.review.dto.response.ReviewResponse;

public interface ReviewService {

    /**
     * Create a new review for a host or planner.
     * Performs eligibility check (COMPLETED booking) and duplicate check before persisting.
     */
    ReviewResponse createReview(CreateReviewRequest request);

    /**
     * Update an existing review (only the owner can update).
     * Updates rating, comment, and updatedAt.
     */
    ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request);

    /**
     * Get paginated PUBLISHED reviews for a host, sorted by createdAt descending.
     */
    Page<ReviewResponse> getReviewsForHost(Long hostId, Pageable pageable);

    /**
     * Get paginated PUBLISHED reviews for a planner, sorted by createdAt descending.
     */
    Page<ReviewResponse> getReviewsForPlanner(Long plannerId, Pageable pageable);

    /**
     * Get the current user's reviews (host + planner) for a specific booking.
     */
    MyBookingReviewsResponse getMyReviewsForBooking(Long bookingId);

    /**
     * Admin: toggle review status between PUBLISHED and HIDDEN.
     */
    ReviewResponse moderateReview(Long reviewId, ModerateReviewRequest request);

    /**
     * Get the average rating for a host or planner (PUBLISHED reviews only).
     */
    Double getAverageRating(Long targetId, com.tourly.review.enums.TargetType targetType);

    /**
     * Get the count of PUBLISHED reviews for a host or planner.
     */
    long getReviewCount(Long targetId, com.tourly.review.enums.TargetType targetType);
}
