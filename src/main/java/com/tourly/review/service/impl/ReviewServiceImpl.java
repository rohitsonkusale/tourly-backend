package com.tourly.review.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tourly.auth.entity.User;
import com.tourly.auth.repository.UserRepository;
import com.tourly.booking.entity.Booking;
import com.tourly.booking.enums.BookingStatus;
import com.tourly.booking.repository.BookingRepository;
import com.tourly.common.exception.ResourceNotFoundException;
import com.tourly.common.exception.UnauthorizedActionException;
import com.tourly.review.dto.request.CreateReviewRequest;
import com.tourly.review.dto.request.ModerateReviewRequest;
import com.tourly.review.dto.request.UpdateReviewRequest;
import com.tourly.review.dto.response.MyBookingReviewsResponse;
import com.tourly.review.dto.response.ReviewResponse;
import com.tourly.review.entity.Review;
import com.tourly.review.enums.ReviewStatus;
import com.tourly.review.enums.TargetType;
import com.tourly.review.exception.DuplicateReviewException;
import com.tourly.review.exception.ReviewEligibilityException;
import com.tourly.review.exception.ReviewNotFoundException;
import com.tourly.review.repository.ReviewRepository;
import com.tourly.review.service.ReviewService;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // =====================================
    // HELPER: GET CURRENT USER
    // =====================================
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new UnauthorizedActionException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // =====================================
    // HELPER: MAP REVIEW TO RESPONSE
    // =====================================
    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setVerified(true); // All reviews come from verified bookings
        response.setStatus(review.getStatus());

        // Reviewer info
        User reviewer = review.getReviewer();
        if (reviewer != null) {
            response.setReviewerName(reviewer.getFullName());
            response.setReviewerAvatarUrl(reviewer.getAvatar());
        }

        // Trip title from booking
        Booking booking = review.getBooking();
        if (booking != null && booking.getTrip() != null) {
            response.setTripTitle(booking.getTrip().getTitle());
        }

        return response;
    }

    // =====================================
    // CREATE REVIEW
    // =====================================
    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        User currentUser = getCurrentUser();

        // 1. Eligibility check: verify the traveler has a COMPLETED booking with the target
        boolean eligible;
        if (request.getTargetType() == TargetType.HOST) {
            eligible = bookingRepository.existsByTravelerIdAndTripHostIdAndStatus(
                    currentUser.getId(), request.getTargetId(), BookingStatus.COMPLETED);
        } else {
            eligible = bookingRepository.existsByTravelerIdAndTripPlannerIdAndStatus(
                    currentUser.getId(), request.getTargetId(), BookingStatus.COMPLETED);
        }

        if (!eligible) {
            throw new ReviewEligibilityException(
                    "You are not eligible to review this " + request.getTargetType().name().toLowerCase()
                            + ". A completed booking is required.");
        }

        // 2. Duplicate check
        boolean exists = reviewRepository.existsByReviewerIdAndTargetTypeAndBookingId(
                currentUser.getId(), request.getTargetType(), request.getBookingId());

        if (exists) {
            throw new DuplicateReviewException(
                    "You have already submitted a review for this " + request.getTargetType().name().toLowerCase()
                            + " on this booking.");
        }

        // 3. Verify the booking exists and belongs to the current user
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        if (!booking.getTraveler().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not authorized to review this booking");
        }

        // 4. Persist with PUBLISHED status
        Review review = new Review();
        review.setBooking(booking);
        review.setReviewer(currentUser);
        review.setTargetType(request.getTargetType());
        review.setTargetId(request.getTargetId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setStatus(ReviewStatus.PUBLISHED);

        Review savedReview = reviewRepository.save(review);

        log.info("Review created: reviewId={}, reviewerId={}, targetType={}, targetId={}, bookingId={}",
                savedReview.getId(), currentUser.getId(), request.getTargetType(),
                request.getTargetId(), request.getBookingId());

        return mapToResponse(savedReview);
    }

    // =====================================
    // UPDATE REVIEW
    // =====================================
    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, UpdateReviewRequest request) {
        User currentUser = getCurrentUser();

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        // Ownership check
        if (!review.getReviewer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You are not authorized to update this review");
        }

        // Update fields if provided
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);

        log.info("Review updated: reviewId={}, reviewerId={}", reviewId, currentUser.getId());

        return mapToResponse(updatedReview);
    }

    // =====================================
    // GET REVIEWS FOR HOST (paginated, PUBLISHED only, descending)
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForHost(Long hostId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByTargetTypeAndTargetIdAndStatus(
                TargetType.HOST, hostId, ReviewStatus.PUBLISHED, pageable);

        return reviews.map(this::mapToResponse);
    }

    // =====================================
    // GET REVIEWS FOR PLANNER (paginated, PUBLISHED only, descending)
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForPlanner(Long plannerId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByTargetTypeAndTargetIdAndStatus(
                TargetType.PLANNER, plannerId, ReviewStatus.PUBLISHED, pageable);

        return reviews.map(this::mapToResponse);
    }

    // =====================================
    // GET MY REVIEWS FOR BOOKING (host + planner reviews)
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public MyBookingReviewsResponse getMyReviewsForBooking(Long bookingId) {
        User currentUser = getCurrentUser();

        List<Review> reviews = reviewRepository.findByReviewerIdAndBookingId(
                currentUser.getId(), bookingId);

        MyBookingReviewsResponse response = new MyBookingReviewsResponse();

        for (Review review : reviews) {
            ReviewResponse reviewResponse = mapToResponse(review);
            if (review.getTargetType() == TargetType.HOST) {
                response.setHostReview(reviewResponse);
            } else if (review.getTargetType() == TargetType.PLANNER) {
                response.setPlannerReview(reviewResponse);
            }
        }

        return response;
    }

    // =====================================
    // MODERATE REVIEW (admin toggle PUBLISHED/HIDDEN)
    // =====================================
    @Override
    @Transactional
    public ReviewResponse moderateReview(Long reviewId, ModerateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        review.setStatus(request.getStatus());

        Review updatedReview = reviewRepository.save(review);

        log.info("Review moderated: reviewId={}, newStatus={}", reviewId, request.getStatus());

        return mapToResponse(updatedReview);
    }

    // =====================================
    // GET AVERAGE RATING (for profile enrichment)
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long targetId, TargetType targetType) {
        return reviewRepository.computeAverageRating(targetType, targetId, ReviewStatus.PUBLISHED)
                .orElse(0.0);
    }

    // =====================================
    // GET REVIEW COUNT (for profile enrichment)
    // =====================================
    @Override
    @Transactional(readOnly = true)
    public long getReviewCount(Long targetId, TargetType targetType) {
        return reviewRepository.countPublishedReviews(targetType, targetId, ReviewStatus.PUBLISHED);
    }
}
