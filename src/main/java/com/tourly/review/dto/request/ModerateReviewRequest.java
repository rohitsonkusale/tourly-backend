package com.tourly.review.dto.request;

import com.tourly.review.enums.ReviewStatus;

import jakarta.validation.constraints.NotNull;

public class ModerateReviewRequest {

    @NotNull(message = "Status is required")
    private ReviewStatus status;

    // ── Getters & Setters ─────────────────────────────────────

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
}
