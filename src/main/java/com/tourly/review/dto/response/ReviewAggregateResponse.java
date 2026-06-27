package com.tourly.review.dto.response;

import org.springframework.data.domain.Page;

public class ReviewAggregateResponse {

    private Double averageRating;
    private Long reviewCount;
    private Page<ReviewResponse> reviews;

    // ── Getters & Setters ─────────────────────────────────────

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }

    public Page<ReviewResponse> getReviews() { return reviews; }
    public void setReviews(Page<ReviewResponse> reviews) { this.reviews = reviews; }
}
