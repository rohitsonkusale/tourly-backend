# Implementation Plan: Review & Rating System

## Overview

Implement reviews for hosts and planners (not trips). Travelers submit reviews from My Bookings after trip completion. Reviews display on host/planner profile pages with aggregate ratings.

## Tasks

- [x] 1. Backend: Set up review module foundation
  - [x] 1.1 Create enums, entity, and exceptions
    - Create `com.tourly.review.enums.ReviewStatus` (PUBLISHED, HIDDEN)
    - Create `com.tourly.review.enums.TargetType` (HOST, PLANNER)
    - Create `com.tourly.review.entity.Review` JPA entity mapped to `reviews` table
    - Add @PrePersist/@PreUpdate for timestamps
    - Create exceptions: ReviewNotFoundException, DuplicateReviewException, ReviewEligibilityException
    - _Requirements: 1.3, 1.4, 2.3, 2.4, 8.1_

  - [x] 1.2 Create DTOs
    - CreateReviewRequest (targetType, targetId, bookingId, rating @Min(1)@Max(5), comment @Size(max=2000))
    - UpdateReviewRequest (rating, comment)
    - ModerateReviewRequest (status)
    - ReviewResponse (id, reviewerName, reviewerAvatarUrl, rating, comment, tripTitle, createdAt, verified, status)
    - ReviewAggregateResponse (averageRating, reviewCount, Page<ReviewResponse>)
    - MyBookingReviewsResponse (hostReview, plannerReview — nullable)
    - _Requirements: 4.2, 5.2, 8.1, 8.2, 9.4_

- [x] 2. Backend: Repository and service layer
  - [x] 2.1 Create ReviewRepository
    - existsByReviewerIdAndTargetTypeAndBookingId (duplicate check)
    - findByTargetTypeAndTargetIdAndStatus with Pageable (public listing)
    - findByReviewerIdAndBookingId (get my reviews for a booking)
    - @Query computeAverageRating (AVG for published reviews by target)
    - @Query countPublishedReviews (COUNT for published reviews by target)
    - _Requirements: 1.4, 2.4, 4.1, 5.1, 6.1_

  - [x] 2.2 Add eligibility methods to BookingRepository
    - existsByTravelerIdAndTripHostIdAndStatus(travelerId, hostId, COMPLETED)
    - existsByTravelerIdAndTripPlannerIdAndStatus(travelerId, plannerId, COMPLETED)
    - _Requirements: 1.1, 2.1_

  - [x] 2.3 Implement ReviewService and ReviewServiceImpl
    - createReview: eligibility check → duplicate check → persist with PUBLISHED status
    - updateReview: ownership check → update rating/comment/updatedAt
    - getReviewsForHost: paginated, PUBLISHED only, descending
    - getReviewsForPlanner: paginated, PUBLISHED only, descending
    - getMyReviewsForBooking: return host + planner reviews for a specific booking
    - moderateReview: admin toggle PUBLISHED/HIDDEN
    - getAverageRating + getReviewCount (for profile enrichment)
    - _Requirements: 1.1-1.5, 2.1-2.5, 3.1-3.4, 4.1-4.3, 5.1-5.3, 6.1-6.2, 7.1-7.3_

- [x] 3. Backend: REST Controller
  - [x] 3.1 Create ReviewController with all endpoints
    - POST /api/reviews (TRAVELER) — create review
    - PUT /api/reviews/{reviewId} (TRAVELER) — edit own review
    - GET /api/reviews/host/{hostId} (Public) — list host reviews
    - GET /api/reviews/planner/{plannerId} (Public) — list planner reviews
    - GET /api/reviews/my/booking/{bookingId} (TRAVELER) — get my reviews for booking
    - PATCH /api/reviews/{reviewId}/status (ADMIN) — moderate
    - Add /api/reviews endpoints to SecurityConfig permitAll for GET routes
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 7.3, 9.1_

- [x] 4. Backend: Integrate with host/planner profiles
  - [x] 4.1 Enrich public profile responses with review data
    - Add averageRating and reviewCount to the host/planner public profile endpoint response
    - Call ReviewService.getAverageRating and getReviewCount when building profile responses
    - _Requirements: 6.1, 6.2_

- [x] 5. Frontend: Review submission from My Bookings
  - [x] 5.1 Create review API functions
    - Create lib/api/reviews.ts with: submitReview, updateReview, getMyReviewsForBooking, getHostReviews, getPlannerReviews
    - _Requirements: 1.3, 2.3, 3.3, 4.1, 5.1_

  - [x] 5.2 Add review actions to completed booking cards
    - On bookings with status COMPLETED, show "Rate Host" and "Rate Planner" buttons
    - Call getMyReviewsForBooking to check if already reviewed → show "Edit Review" if exists
    - Build review modal/inline form with star rating picker (1-5) and optional comment textarea
    - On submit, call submitReview API; on edit, call updateReview API
    - Show success feedback and update button state
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [x] 6. Frontend: Display reviews on host/planner profiles
  - [x] 6.1 Add reviews section to host profile page
    - Fetch reviews via getHostReviews(hostId) with pagination
    - Display average rating, review count, and paginated review cards
    - Each card shows: reviewer name, avatar, star rating, comment, trip title, date
    - _Requirements: 4.1, 4.2, 4.3, 6.1_

  - [x] 6.2 Add reviews section to planner profile page (if exists)
    - Same pattern as host reviews
    - _Requirements: 5.1, 5.2, 5.3, 6.1_

## Notes

- No trip reviews — only host/planner reviews
- Reviews table already exists in DB (Flyway V1) — no migration needed
- Entry point: My Bookings page (completed bookings only)
- Display point: Host/Planner public profile pages
- Frontend ReviewsSection on trip detail page remains unchanged (shows empty until future iteration)
- Uniqueness: one host review + one planner review per booking

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "2.1", "2.2"] },
    { "id": 2, "tasks": ["2.3"] },
    { "id": 3, "tasks": ["3.1"] },
    { "id": 4, "tasks": ["4.1", "5.1"] },
    { "id": 5, "tasks": ["5.2", "6.1", "6.2"] }
  ]
}
```
