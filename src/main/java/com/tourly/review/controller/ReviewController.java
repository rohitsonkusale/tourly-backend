package com.tourly.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.tourly.common.dto.ApiResponse;
import com.tourly.review.dto.request.CreateReviewRequest;
import com.tourly.review.dto.request.ModerateReviewRequest;
import com.tourly.review.dto.request.UpdateReviewRequest;
import com.tourly.review.dto.response.MyBookingReviewsResponse;
import com.tourly.review.dto.response.ReviewResponse;
import com.tourly.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/reviews")
@Validated
@Tag(name = "Review", description = "Review and rating management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // =========================================
    // TRAVELER — Create a review
    // =========================================
    @PostMapping
    @PreAuthorize("hasAnyRole('TRAVELER','ADMIN')")
    @Operation(summary = "Create a review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", response));
    }

    // =========================================
    // TRAVELER — Edit own review
    // =========================================
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('TRAVELER','ADMIN')")
    @Operation(summary = "Update a review", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable @Positive(message = "Review ID must be greater than 0") Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(reviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", response));
    }

    // =========================================
    // PUBLIC — List reviews for a host
    // =========================================
    @GetMapping("/host/{hostId}")
    @Operation(summary = "Get reviews for a host (public)")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getHostReviews(
            @PathVariable @Positive(message = "Host ID must be greater than 0") Long hostId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewResponse> response = reviewService.getReviewsForHost(hostId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Host reviews fetched successfully", response));
    }

    // =========================================
    // PUBLIC — List reviews for a planner
    // =========================================
    @GetMapping("/planner/{plannerId}")
    @Operation(summary = "Get reviews for a planner (public)")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getPlannerReviews(
            @PathVariable @Positive(message = "Planner ID must be greater than 0") Long plannerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewResponse> response = reviewService.getReviewsForPlanner(plannerId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Planner reviews fetched successfully", response));
    }

    // =========================================
    // TRAVELER — Get my reviews for a booking
    // =========================================
    @GetMapping("/my/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('TRAVELER','ADMIN')")
    @Operation(summary = "Get my reviews for a booking", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MyBookingReviewsResponse>> getMyReviewsForBooking(
            @PathVariable @Positive(message = "Booking ID must be greater than 0") Long bookingId) {
        MyBookingReviewsResponse response = reviewService.getMyReviewsForBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking reviews fetched successfully", response));
    }

    // =========================================
    // ADMIN — Moderate a review
    // =========================================
    @PatchMapping("/{reviewId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Moderate a review (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReviewResponse>> moderateReview(
            @PathVariable @Positive(message = "Review ID must be greater than 0") Long reviewId,
            @Valid @RequestBody ModerateReviewRequest request) {
        ReviewResponse response = reviewService.moderateReview(reviewId, request);
        return ResponseEntity.ok(ApiResponse.success("Review status updated successfully", response));
    }
}
