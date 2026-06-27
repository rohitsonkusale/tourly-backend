# Design Document: Review & Rating System

## Overview

The Review & Rating System enables travelers to rate hosts and planners after trip completion. Reviews are submitted from the My Bookings page, auto-published, and displayed on host/planner public profiles. Admins can moderate reviews. No trip reviews exist — only person reviews (HOST, PLANNER).

## Architecture

New `com.tourly.review` module following existing package structure. Integrates with `booking` module for eligibility and `auth` module for authentication.

### Flow

```
My Bookings Page (completed) → "Rate Host" / "Rate Planner" button
       ↓
Review Form (stars + comment)
       ↓
POST /api/reviews → ReviewController → ReviewService → ReviewRepository
       ↓
Review appears on Host/Planner profile page
```

### Integration Points

1. **BookingRepository** — Eligibility check (COMPLETED status)
2. **User entity** — Reviewer name/avatar for response DTOs
3. **Host/Planner profile pages** — Display reviews and aggregate ratings
4. **My Bookings page** — Review entry point with state-aware buttons

---

## REST API Endpoints

### ReviewController (`/api/reviews`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/reviews` | TRAVELER | Submit review for host or planner |
| PUT | `/api/reviews/{reviewId}` | TRAVELER | Edit own review |
| GET | `/api/reviews/host/{hostId}` | Public | Get reviews for a host |
| GET | `/api/reviews/planner/{plannerId}` | Public | Get reviews for a planner |
| GET | `/api/reviews/my/booking/{bookingId}` | TRAVELER | Get traveler's reviews for a booking |
| PATCH | `/api/reviews/{reviewId}/status` | ADMIN | Moderate (hide/unhide) |

### Request/Response DTOs

**CreateReviewRequest:**
```java
{
  targetType: "HOST" | "PLANNER",
  targetId: Long,        // host or planner user ID
  bookingId: Long,       // the completed booking
  rating: Integer (1-5),
  comment: String (max 2000, optional)
}
```

**ReviewResponse:**
```java
{
  id: Long,
  reviewerName: String,
  reviewerAvatarUrl: String,
  rating: Integer,
  comment: String,
  tripTitle: String,      // context: which trip this was for
  createdAt: LocalDateTime,
  verified: boolean,
  status: ReviewStatus
}
```

**ReviewAggregateResponse:**
```java
{
  averageRating: Double,  // rounded 1 decimal
  reviewCount: Long,
  reviews: Page<ReviewResponse>
}
```

**MyBookingReviewsResponse:**
```java
{
  hostReview: ReviewResponse | null,
  plannerReview: ReviewResponse | null
}
```

---

## Data Model

### Entity: Review (maps to existing `reviews` table)

| Field | Type | Constraint |
|-------|------|-----------|
| id | Long | PK, auto-increment |
| booking | Booking | FK, not null |
| reviewer | User | FK, not null |
| targetType | Enum (HOST, PLANNER) | not null |
| targetId | Long | not null (host/planner user ID) |
| rating | Integer | 1-5, not null |
| comment | String (TEXT) | max 5000, nullable |
| status | Enum (PUBLISHED, HIDDEN) | default PUBLISHED |
| createdAt | LocalDateTime | auto-set |
| updatedAt | LocalDateTime | auto-set |

**Uniqueness:** Application-enforced per (reviewer_id, target_type, booking_id) — one host review + one planner review per booking.

---

## Frontend Changes

### My Bookings Page — Review Actions

On completed booking cards, add:
- "Rate Host" button → opens review modal/inline form
- "Rate Planner" button → opens review modal/inline form (only if trip had a planner)
- If already reviewed → show "Edit Review" with filled stars

### Host/Planner Profile Pages

Display reviews section with:
- Average rating + review count header
- Paginated review cards (name, avatar, stars, comment, trip title, date)

---

## Error Handling

| Scenario | HTTP | Message |
|----------|------|---------|
| No completed booking | 403 | "You must complete a booking to review this host/planner" |
| Duplicate review | 409 | "You have already reviewed this host/planner for this booking" |
| Not own review | 403 | "You can only edit your own reviews" |
| Review not found | 404 | "Review not found" |
| Rating out of range | 400 | "Rating must be between 1 and 5" |
| Comment too long | 400 | "Comment must not exceed 2000 characters" |
| Non-admin moderate | 403 | "Access denied" |
