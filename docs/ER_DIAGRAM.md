# Tourly/Roamaya — Target ER Diagram

> This represents the **recommended** database structure with all issues addressed.
> Use with any Mermaid renderer (GitHub, VS Code extension, mermaid.live)

```mermaid
erDiagram

    %% ═══════════════════════════════════════════
    %% AUTH DOMAIN
    %% ═══════════════════════════════════════════

    roles {
        BIGINT id PK
        ENUM name "TRAVELER, PLANNER, HOST, ADMIN"
        VARCHAR description
        BOOLEAN is_active
        DATETIME created_at
        DATETIME updated_at
    }

    permissions {
        BIGINT id PK
        ENUM permission_name UK
        VARCHAR description
    }

    role_permissions {
        BIGINT id PK
        BIGINT role_id FK
        BIGINT permission_id FK
    }

    users {
        BIGINT id PK
        VARCHAR full_name
        VARCHAR email UK
        VARCHAR phone UK
        VARCHAR password
        VARCHAR google_id UK
        VARCHAR avatar
        VARCHAR aadhaar_number UK
        VARCHAR pan_number UK
        VARCHAR instagram_username
        VARCHAR website_url
        BIGINT role_id FK
        ENUM account_status "ACTIVE, SUSPENDED, BANNED"
        BOOLEAN email_verified
        BOOLEAN phone_verified
        BOOLEAN kyc_verified
        CHAR admin_approval_flag "Y/N"
        DATETIME last_login_at
        DATETIME created_at
        DATETIME updated_at
        DATETIME deleted_at
    }

    user_profiles {
        BIGINT id PK
        BIGINT user_id FK,UK
        VARCHAR display_name
        TEXT bio
        VARCHAR profile_picture_url
        VARCHAR contact_email
        VARCHAR contact_phone
        JSON social_links
        VARCHAR preferred_language
        VARCHAR timezone
        JSON preferred_destinations
        JSON travel_styles
        BOOLEAN newsletter_subscribed
        DATETIME created_at
        DATETIME updated_at
    }

    roles ||--o{ users : "has"
    roles ||--o{ role_permissions : "grants"
    permissions ||--o{ role_permissions : "assigned via"
    users ||--o| user_profiles : "has profile"

    %% ═══════════════════════════════════════════
    %% TRIP DOMAIN
    %% ═══════════════════════════════════════════

    destinations {
        BIGINT id PK
        VARCHAR country
        VARCHAR state
        VARCHAR city
        DOUBLE latitude
        DOUBLE longitude
        VARCHAR image_url
        TEXT description
        BOOLEAN is_active
    }

    trips {
        BIGINT id PK
        VARCHAR title
        TEXT description
        BIGINT planner_id FK "User who plans the trip"
        BIGINT host_id FK "User who hosts on-ground (nullable)"
        BIGINT destination_id FK
        DATE start_date
        DATE end_date
        DECIMAL base_price
        DECIMAL min_price
        DECIMAL max_price
        DECIMAL current_price "Dynamic pricing"
        DECIMAL max_discount_percent
        DECIMAL max_increase_percent
        INT total_seats
        INT booked_seats
        ENUM category "ADVENTURE, LUXURY, etc."
        ENUM status "DRAFT, PUBLISHED, COMPLETED, CANCELLED"
        ENUM approval_status "PENDING, APPROVED, REJECTED"
        ENUM cancellation_policy
        VARCHAR rejection_reason
        VARCHAR difficulty
        VARCHAR group_size_label
        VARCHAR trip_type
        VARCHAR best_time
        VARCHAR starts_from
        VARCHAR ends_at
        TEXT about_description
        INT min_group_size
        INT duration_days
        INT duration_nights
        BOOLEAN is_active
        BOOLEAN is_deleted
        DATETIME created_at
        DATETIME updated_at
        DATETIME deleted_at
    }

    trip_batches {
        BIGINT id PK
        BIGINT trip_id FK
        DATE start_date
        DATE end_date
        DECIMAL price
        INT seats_available
        VARCHAR status
    }

    trip_itinerary_days {
        BIGINT id PK
        BIGINT trip_id FK
        INT day_number
        VARCHAR title
        TEXT description
        VARCHAR stay
        VARCHAR meals
        INT sort_order
    }

    trip_highlights {
        BIGINT id PK
        BIGINT trip_id FK
        VARCHAR icon
        VARCHAR title
        INT sort_order
    }

    trip_stops {
        BIGINT id PK
        BIGINT trip_id FK
        VARCHAR stop_name
        INT sort_order
    }

    trip_items {
        BIGINT id PK
        BIGINT trip_id FK
        VARCHAR type "INCLUSION or EXCLUSION"
        VARCHAR description
        INT sort_order
    }

    trip_media {
        BIGINT id PK
        BIGINT trip_id FK
        ENUM media_type "IMAGE, VIDEO"
        VARCHAR url "length 1000"
        VARCHAR caption
        BOOLEAN is_cover
        INT sort_order
    }

    trip_price_breakdown {
        BIGINT id PK
        BIGINT trip_id FK
        VARCHAR category
        DECIMAL amount
        VARCHAR description
        INT sort_order
    }

    trip_stays {
        BIGINT id PK
        BIGINT trip_id FK
        VARCHAR name
        VARCHAR location
        TEXT description
        INT sort_order
    }

    trip_stay_amenities {
        BIGINT id PK
        BIGINT stay_id FK
        VARCHAR amenity
    }

    trip_stay_images {
        BIGINT id PK
        BIGINT stay_id FK
        VARCHAR image_url "length 1000"
        INT sort_order
    }

    trip_badges {
        BIGINT id PK
        BIGINT trip_id FK
        VARCHAR badge_name
    }

    users ||--o{ trips : "plans (planner_id)"
    users ||--o{ trips : "hosts (host_id)"
    destinations ||--o{ trips : "located at"
    trips ||--o{ trip_batches : "has batches"
    trips ||--o{ trip_itinerary_days : "has itinerary"
    trips ||--o{ trip_highlights : "has highlights"
    trips ||--o{ trip_stops : "has route stops"
    trips ||--o{ trip_items : "has inclusions/exclusions"
    trips ||--o{ trip_media : "has media"
    trips ||--o{ trip_price_breakdown : "has price breakdown"
    trips ||--o{ trip_stays : "has accommodations"
    trips ||--o{ trip_badges : "tagged with"
    trip_stays ||--o{ trip_stay_amenities : "has amenities"
    trip_stays ||--o{ trip_stay_images : "has images"

    %% ═══════════════════════════════════════════
    %% BOOKING DOMAIN
    %% ═══════════════════════════════════════════

    bookings {
        BIGINT id PK
        VARCHAR booking_ref UK
        BIGINT trip_id FK
        BIGINT traveler_id FK
        BIGINT batch_id FK "nullable"
        INT seats_booked
        DECIMAL base_amount
        DECIMAL discount_amount
        DECIMAL tax_amount
        DECIMAL total_price
        ENUM status "PENDING, CONFIRMED, COMPLETED, CANCELLED, EXPIRED"
        ENUM payment_status "PENDING, PAID, PARTIALLY_PAID, REFUNDED"
        VARCHAR cancellation_reason
        DATETIME expires_at
        DATETIME confirmed_at
        DATETIME cancelled_at
        DATETIME completed_at
        DATETIME created_at
        DATETIME updated_at
    }

    trips ||--o{ bookings : "booked as"
    users ||--o{ bookings : "books (traveler)"
    trip_batches ||--o{ bookings : "for batch"

    %% ═══════════════════════════════════════════
    %% PAYMENT DOMAIN
    %% ═══════════════════════════════════════════

    payment_stages {
        BIGINT id PK
        BIGINT booking_id FK
        INT stage_number
        VARCHAR label
        DECIMAL amount
        DECIMAL percentage
        VARCHAR status "PENDING, PAID, CANCELLED"
        DATE due_date
        DATETIME paid_at
    }

    payments {
        BIGINT id PK
        BIGINT booking_id FK "ManyToOne (NOT OneToOne)"
        BIGINT stage_id FK "nullable"
        VARCHAR razorpay_order_id
        VARCHAR razorpay_payment_id
        VARCHAR razorpay_signature
        DECIMAL amount
        ENUM status "CREATED, PAID, FAILED, REFUNDED"
        DECIMAL gateway_fee
        VARCHAR currency
        VARCHAR payment_method
        VARCHAR failure_reason
        DATETIME paid_at
        DATETIME created_at
        DATETIME updated_at
    }

    refunds {
        BIGINT id PK
        BIGINT booking_id FK
        BIGINT payment_id FK
        BIGINT requested_by FK
        BIGINT processed_by FK
        VARCHAR razorpay_refund_id
        DECIMAL original_amount
        DECIMAL refund_amount
        ENUM refund_type "FULL, PARTIAL, WALLET, MANUAL"
        ENUM status "PENDING, APPROVED, REJECTED, PROCESSED"
        VARCHAR reason
        VARCHAR admin_notes
        DATETIME requested_at
        DATETIME processed_at
        DATETIME created_at
    }

    commissions {
        BIGINT id PK
        BIGINT booking_id FK
        DECIMAL booking_amount
        DECIMAL commission_rate
        DECIMAL commission_amount
        DECIMAL tax_on_commission
        DECIMAL net_platform_earning
        ENUM status "CALCULATED, REALIZED, CANCELLED"
        DATETIME calculated_at
    }

    bank_accounts {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR account_holder_name
        VARCHAR bank_name
        VARCHAR account_number
        VARCHAR ifsc_code
        VARCHAR upi_id
        BOOLEAN is_primary
        BOOLEAN is_verified
        DATETIME created_at
    }

    payouts {
        BIGINT id PK
        BIGINT booking_id FK
        BIGINT payee_id FK
        BIGINT bank_account_id FK
        ENUM payee_type "HOST, PLANNER"
        DECIMAL gross_amount
        DECIMAL commission_deducted
        DECIMAL tds_deducted
        DECIMAL net_amount
        ENUM status "PENDING, ON_HOLD, FAILED, RELEASED"
        VARCHAR razorpay_transfer_id
        VARCHAR utr_number
        DATETIME requested_at
        DATETIME processed_at
        DATETIME released_at
    }

    bookings ||--o{ payments : "paid via (ManyToOne)"
    bookings ||--o{ payment_stages : "staged as"
    payment_stages ||--o{ payments : "fulfilled by"
    bookings ||--o{ refunds : "refunded"
    payments ||--o{ refunds : "source payment"
    users ||--o{ refunds : "requested by"
    users ||--o{ refunds : "processed by"
    bookings ||--o{ commissions : "earns commission"
    users ||--o{ bank_accounts : "owns"
    bookings ||--o{ payouts : "pays out"
    users ||--o{ payouts : "receives (payee)"
    bank_accounts ||--o{ payouts : "deposited to"

    %% ═══════════════════════════════════════════
    %% VERIFICATION DOMAIN
    %% ═══════════════════════════════════════════

    host_verifications {
        BIGINT id PK
        BIGINT user_id FK,UK
        VARCHAR display_name
        TEXT bio
        VARCHAR specialization
        INT experience_years
        TEXT aadhaar_document_url
        TEXT pan_document_url
        TEXT selfie_url
        ENUM verification_status "PENDING, APPROVED, REJECTED"
        VARCHAR rejection_reason
        DATETIME submitted_at
        DATETIME reviewed_at
        BIGINT reviewed_by FK
        DATETIME created_at
        DATETIME updated_at
    }

    planner_verifications {
        BIGINT id PK
        BIGINT user_id FK,UK
        VARCHAR display_name
        TEXT bio
        VARCHAR specialization
        INT experience_years
        TEXT aadhaar_document_url
        TEXT pan_document_url
        TEXT selfie_url
        ENUM verification_status "PENDING, APPROVED, REJECTED"
        VARCHAR rejection_reason
        DATETIME submitted_at
        DATETIME reviewed_at
        BIGINT reviewed_by FK
        DATETIME created_at
        DATETIME updated_at
    }

    users ||--o| host_verifications : "verified as host"
    users ||--o| planner_verifications : "verified as planner"
    users ||--o{ host_verifications : "reviews (admin)"
    users ||--o{ planner_verifications : "reviews (admin)"

    %% ═══════════════════════════════════════════
    %% REVIEWS & MODERATION
    %% ═══════════════════════════════════════════

    reviews {
        BIGINT id PK
        BIGINT booking_id FK
        BIGINT reviewer_id FK
        ENUM target_type "TRIP, HOST, PLANNER"
        BIGINT target_id "polymorphic"
        INT rating "1-5"
        TEXT comment
        ENUM status "PENDING, APPROVED, REMOVED"
        DATETIME created_at
        DATETIME updated_at
    }

    review_media {
        BIGINT id PK
        BIGINT review_id FK
        VARCHAR url
        VARCHAR media_type
    }

    disputes {
        BIGINT id PK
        BIGINT booking_id FK
        BIGINT filed_by FK
        BIGINT against_user_id FK
        VARCHAR title
        TEXT description
        ENUM status "OPEN, UNDER_REVIEW, WAITING_RESPONSE, RESOLVED, REJECTED"
        ENUM priority "LOW, MEDIUM, HIGH, CRITICAL"
        TEXT admin_notes
        DATETIME created_at
        DATETIME updated_at
        DATETIME resolved_at
    }

    dispute_evidence {
        BIGINT id PK
        BIGINT dispute_id FK
        VARCHAR type "SCREENSHOT, CHAT_LOG, DOCUMENT"
        VARCHAR label
        VARCHAR url
        DATETIME uploaded_at
    }

    bookings ||--o{ reviews : "reviewed"
    users ||--o{ reviews : "writes"
    reviews ||--o{ review_media : "has media"
    bookings ||--o{ disputes : "disputed"
    users ||--o{ disputes : "filed by"
    users ||--o{ disputes : "against"
    disputes ||--o{ dispute_evidence : "evidence"

    %% ═══════════════════════════════════════════
    %% SUPPORT
    %% ═══════════════════════════════════════════

    support_tickets {
        BIGINT id PK
        BIGINT created_by FK
        BIGINT assigned_to FK
        VARCHAR subject
        TEXT description
        ENUM category "PAYMENT, BOOKING, REFUND, VERIFICATION, SAFETY, TECHNICAL"
        ENUM priority "LOW, MEDIUM, HIGH, CRITICAL"
        ENUM status "OPEN, IN_PROGRESS, WAITING, RESOLVED, CLOSED"
        DATETIME created_at
        DATETIME updated_at
        DATETIME resolved_at
    }

    ticket_messages {
        BIGINT id PK
        BIGINT ticket_id FK
        BIGINT sender_id FK
        TEXT content
        BOOLEAN is_admin
        DATETIME created_at
    }

    users ||--o{ support_tickets : "creates"
    users ||--o{ support_tickets : "assigned to"
    support_tickets ||--o{ ticket_messages : "has messages"
    users ||--o{ ticket_messages : "sends"

    %% ═══════════════════════════════════════════
    %% MARKETING & COUPONS
    %% ═══════════════════════════════════════════

    coupons {
        BIGINT id PK
        VARCHAR code UK
        BIGINT trip_id FK "nullable — scoped to trip"
        BIGINT destination_id FK "nullable — scoped to destination"
        ENUM discount_type "PERCENTAGE, FLAT"
        DECIMAL discount_value
        INT max_uses
        INT used_count
        DECIMAL min_order_value
        ENUM status "ACTIVE, EXPIRED, PAUSED"
        DATETIME valid_from
        DATETIME valid_to
        DATETIME created_at
    }

    coupon_usages {
        BIGINT id PK
        BIGINT coupon_id FK
        BIGINT booking_id FK
        BIGINT user_id FK
        DECIMAL discount_applied
        DATETIME used_at
    }

    announcements {
        BIGINT id PK
        VARCHAR title
        TEXT content
        ENUM type "INFO, WARNING, EMERGENCY, MAINTENANCE, PROMOTION"
        BOOLEAN is_active
        DATETIME expires_at
        DATETIME created_at
    }

    announcement_audiences {
        BIGINT id PK
        BIGINT announcement_id FK
        VARCHAR target_role "ALL, TRAVELER, HOST, PLANNER, ADMIN"
    }

    trips ||--o{ coupons : "scoped to trip"
    destinations ||--o{ coupons : "scoped to destination"
    coupons ||--o{ coupon_usages : "used"
    bookings ||--o{ coupon_usages : "applied to"
    users ||--o{ coupon_usages : "used by"
    announcements ||--o{ announcement_audiences : "targets"

    %% ═══════════════════════════════════════════
    %% SYSTEM & AUDIT
    %% ═══════════════════════════════════════════

    notifications {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR title
        TEXT message
        ENUM type "BOOKING, PAYMENT, SYSTEM, DISPUTE"
        VARCHAR target_type
        BIGINT target_id
        DATETIME read_at
        DATETIME created_at
    }

    audit_logs {
        BIGINT id PK
        BIGINT performed_by FK
        VARCHAR action
        ENUM category "USER, FINANCIAL, MODERATION, SYSTEM, SECURITY"
        VARCHAR target_type
        BIGINT target_id
        TEXT details
        VARCHAR ip_address
        VARCHAR device_info
        DATETIME created_at
    }

    webhook_logs {
        BIGINT id PK
        ENUM provider "RAZORPAY"
        VARCHAR event_type
        JSON payload
        ENUM status "PENDING, PROCESSED, FAILED"
        TEXT error_message
        DATETIME received_at
        DATETIME processed_at
    }

    email_logs {
        BIGINT id PK
        VARCHAR recipient_email
        VARCHAR subject
        VARCHAR template_id
        ENUM status "SENT, DELIVERED, BOUNCED, FAILED"
        VARCHAR provider_message_id
        TEXT error_message
        DATETIME sent_at
    }

    media_assets {
        BIGINT id PK
        BIGINT uploaded_by FK
        VARCHAR file_name
        VARCHAR file_url
        VARCHAR file_type "IMAGE, VIDEO, DOCUMENT"
        BIGINT file_size
        VARCHAR provider "CLOUDINARY, S3"
        VARCHAR provider_public_id
        DATETIME created_at
    }

    platform_settings {
        BIGINT id PK
        VARCHAR setting_key UK
        VARCHAR label
        TEXT description
        VARCHAR setting_type "TOGGLE, NUMBER, TEXT, SELECT"
        VARCHAR value
        VARCHAR category "PLATFORM, SECURITY, FEATURES"
        BIGINT updated_by FK
        DATETIME updated_at
    }

    users ||--o{ notifications : "receives"
    users ||--o{ audit_logs : "performed by"
    users ||--o{ media_assets : "uploaded by"
    users ||--o{ platform_settings : "updated by"
```

---

## Key Changes from Current Schema (reflected above)

| # | Change | Reason |
|---|--------|--------|
| 1 | `payments.booking_id` is **ManyToOne** (not OneToOne) | Supports staged/multiple payments per booking |
| 2 | `trip_media.url` → **VARCHAR(1000)** | Match DDL, support Cloudinary URLs |
| 3 | **Removed `trip_inclusions`** table | Replaced by `trip_items` (trip-level). No batch-level inclusions. |
| 4 | **Added `trip_badges`** table | Replaces JSON string column. Queryable, indexable. |
| 5 | `users` timestamps → **single DATETIME** columns | Replaced date+time split with `created_at`, `updated_at`, `deleted_at` |
| 6 | `user_profiles.social_links` → **JSON type** | Replaces comma-separated string |
| 7 | All child tables use **ON DELETE CASCADE** | Consistent deletion behavior |
| 8 | `trip_batches` has **UNIQUE(trip_id, start_date, end_date)** | Prevents duplicate batches |
| 9 | Keep **planner_id + host_id** on trips | Two-actor model is correct for marketplace |
| 10 | Unified verification enum → **shared ENUM** in both tables | Removes cross-domain dependency |

---

## Table Count Summary

| Domain | Tables | Purpose |
|--------|--------|---------|
| Auth | 4 | Users, roles, permissions, profiles |
| Trip | 12 | Trip + all child content tables |
| Booking | 1 | Booking lifecycle |
| Payment | 6 | Payments, stages, refunds, commissions, payouts, bank accounts |
| Verification | 2 | Host + Planner KYC |
| Reviews & Moderation | 4 | Reviews, disputes + evidence |
| Support | 2 | Tickets + messages |
| Marketing | 4 | Coupons, usages, announcements, audiences |
| System | 5 | Notifications, audit, webhooks, emails, media, settings |
| **Total** | **40** | |

