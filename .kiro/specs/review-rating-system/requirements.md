# Requirements Document

## Introduction

The Review & Rating System enables travelers to submit star ratings (1–5) and text reviews for hosts and planners after completing a trip. Reviews are accessible from the "My Bookings" section once a booking reaches COMPLETED status. Reviews are auto-published and displayed on host/planner public profile pages. Each traveler may submit one host review and one planner review per completed booking. Admins can moderate (hide/unhide) reviews.

## Glossary

- **Review_System**: The backend service responsible for creating, storing, retrieving, updating, and moderating reviews
- **Traveler**: An authenticated user with role TRAVELER who has completed at least one booking
- **Host**: An authenticated user with role HOST who organized the trip
- **Planner**: An authenticated user with role PLANNER who planned the trip
- **Admin**: An authenticated user with role ADMIN who moderates reviews
- **Review**: A record containing a rating (1–5), optional comment text, target_type (HOST or PLANNER), target_id, and status
- **Booking**: A reservation record linking a traveler to a trip; must have status COMPLETED for review eligibility
- **Target_Type**: A discriminator indicating whether the review targets a HOST or a PLANNER
- **Rating**: An integer value between 1 and 5 inclusive
- **Review_Status**: The publication state: PUBLISHED or HIDDEN
- **My_Bookings_Page**: The frontend page showing a traveler's booking history, where review actions are accessible

## Requirements

### Requirement 1: Submit a Host Review

**User Story:** As a traveler who completed a trip, I want to rate and review the host from my completed booking, so other travelers can evaluate host quality.

#### Acceptance Criteria

1. WHEN a traveler submits a review with target_type HOST, THE Review_System SHALL verify that the traveler has a booking with status COMPLETED for a trip managed by the specified host.
2. IF the traveler does not have a completed booking associated with the target host, THEN THE Review_System SHALL reject the submission with a 403 error.
3. WHEN a valid host review is submitted, THE Review_System SHALL persist the review with a rating between 1 and 5, an optional comment up to 2000 characters, and status PUBLISHED.
4. THE Review_System SHALL enforce one host review per traveler per booking (reviewer_id + target_type HOST + booking_id).
5. IF a traveler attempts a duplicate host review for the same booking, THEN THE Review_System SHALL reject with a 409 conflict error.

### Requirement 2: Submit a Planner Review

**User Story:** As a traveler who completed a trip, I want to rate and review the planner from my completed booking, so other travelers can evaluate planner quality.

#### Acceptance Criteria

1. WHEN a traveler submits a review with target_type PLANNER, THE Review_System SHALL verify that the traveler has a booking with status COMPLETED for a trip managed by the specified planner.
2. IF the traveler does not have a completed booking associated with the target planner, THEN THE Review_System SHALL reject the submission with a 403 error.
3. WHEN a valid planner review is submitted, THE Review_System SHALL persist the review with a rating between 1 and 5, an optional comment up to 2000 characters, and status PUBLISHED.
4. THE Review_System SHALL enforce one planner review per traveler per booking (reviewer_id + target_type PLANNER + booking_id).
5. IF a traveler attempts a duplicate planner review for the same booking, THEN THE Review_System SHALL reject with a 409 conflict error.

### Requirement 3: Edit an Existing Review

**User Story:** As a traveler, I want to edit my previously submitted review so I can update my rating or comment.

#### Acceptance Criteria

1. WHEN a traveler submits an update to an existing review, THE Review_System SHALL verify the review belongs to the requesting traveler.
2. IF the review does not belong to the requesting traveler, THEN THE Review_System SHALL reject with a 403 error.
3. WHEN a valid update is submitted, THE Review_System SHALL update the rating and comment fields and set updated_at to current time.
4. THE Review_System SHALL retain the original created_at timestamp and review status when edited.

### Requirement 4: Retrieve Reviews for a Host

**User Story:** As a visitor viewing a host profile, I want to see all reviews for that host so I can evaluate their reputation.

#### Acceptance Criteria

1. WHEN reviews are requested for a host, THE Review_System SHALL return a paginated list of reviews with target_type HOST, status PUBLISHED, for that host, ordered by created_at descending.
2. THE Review_System SHALL include reviewer display name, avatar URL, rating, comment, created_at, and trip title for context.
3. THE Review_System SHALL return total review count and average rating alongside the list.

### Requirement 5: Retrieve Reviews for a Planner

**User Story:** As a visitor viewing a planner profile, I want to see all reviews for that planner.

#### Acceptance Criteria

1. WHEN reviews are requested for a planner, THE Review_System SHALL return a paginated list with target_type PLANNER, status PUBLISHED, for that planner, ordered by created_at descending.
2. THE Review_System SHALL include reviewer display name, avatar URL, rating, comment, created_at, and trip title for context.
3. THE Review_System SHALL return total review count and average rating alongside the list.

### Requirement 6: Display Aggregate Ratings on Host/Planner Profiles

**User Story:** As a visitor, I want to see average ratings and review counts on host/planner profile pages.

#### Acceptance Criteria

1. THE Review_System SHALL compute and return average rating (rounded to 1 decimal) and total review count for each host/planner.
2. WHEN a host/planner has zero reviews, THE Review_System SHALL return average 0 and count 0.

### Requirement 7: Admin Review Moderation

**User Story:** As an admin, I want to hide inappropriate reviews to maintain platform quality.

#### Acceptance Criteria

1. WHEN an admin sets a review status to HIDDEN, it SHALL be excluded from public queries.
2. WHEN an admin sets a review status to PUBLISHED, it SHALL be restored to public visibility.
3. THE Review_System SHALL restrict moderation to ADMIN role only.

### Requirement 8: Validation Rules

#### Acceptance Criteria

1. Rating must be integer between 1 and 5 inclusive.
2. Comment must not exceed 2000 characters.
3. Invalid submissions SHALL return 400 with specific validation error messages.

### Requirement 9: Review Entry Point from My Bookings

**User Story:** As a traveler, I want to see a "Write Review" option on my completed bookings so I can easily leave feedback.

#### Acceptance Criteria

1. THE My_Bookings_Page SHALL show a "Rate Host" and "Rate Planner" action on bookings with status COMPLETED.
2. IF the traveler has already reviewed the host for that booking, THE action SHALL show "Edit Review" instead.
3. IF the traveler has already reviewed the planner for that booking, THE action SHALL show "Edit Review" instead.
4. THE review form SHALL display the host/planner name, trip title, and allow rating (1-5 stars) and optional comment input.
