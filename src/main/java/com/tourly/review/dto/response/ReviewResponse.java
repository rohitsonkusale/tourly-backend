package com.tourly.review.dto.response;

import java.time.LocalDateTime;

import com.tourly.review.enums.ReviewStatus;

public class ReviewResponse {

    private Long id;
    private String reviewerName;
    private String reviewerAvatarUrl;
    private Integer rating;
    private String comment;
    private String tripTitle;
    private LocalDateTime createdAt;
    private Boolean verified;
    private ReviewStatus status;

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewerAvatarUrl() { return reviewerAvatarUrl; }
    public void setReviewerAvatarUrl(String reviewerAvatarUrl) { this.reviewerAvatarUrl = reviewerAvatarUrl; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getTripTitle() { return tripTitle; }
    public void setTripTitle(String tripTitle) { this.tripTitle = tripTitle; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
}
