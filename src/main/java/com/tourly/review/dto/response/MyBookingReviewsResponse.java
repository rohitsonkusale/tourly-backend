package com.tourly.review.dto.response;

public class MyBookingReviewsResponse {

    private ReviewResponse hostReview;
    private ReviewResponse plannerReview;

    // ── Getters & Setters ─────────────────────────────────────

    public ReviewResponse getHostReview() { return hostReview; }
    public void setHostReview(ReviewResponse hostReview) { this.hostReview = hostReview; }

    public ReviewResponse getPlannerReview() { return plannerReview; }
    public void setPlannerReview(ReviewResponse plannerReview) { this.plannerReview = plannerReview; }
}
