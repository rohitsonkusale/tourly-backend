-- ============================================================================
-- V1__initial_schema.sql
-- Tourly/Roamaya — Complete Database Schema (Clean Build)
-- 42 tables, fully normalized, naming-standards compliant
-- ============================================================================

-- ============================================================================
-- DOMAIN 1: AUTH (4 tables)
-- ============================================================================

CREATE TABLE roles (
    role_id             BIGINT          AUTO_INCREMENT,
    name                VARCHAR(50)     NOT NULL,
    description         VARCHAR(255),
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_roles PRIMARY KEY (role_id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

CREATE TABLE permissions (
    permission_id       BIGINT          AUTO_INCREMENT,
    permission_name     VARCHAR(100)    NOT NULL,
    description         VARCHAR(255),

    CONSTRAINT pk_permissions PRIMARY KEY (permission_id),
    CONSTRAINT uq_permissions_name UNIQUE (permission_name)
);

CREATE TABLE role_permissions (
    role_permission_id  BIGINT          AUTO_INCREMENT,
    role_id             BIGINT          NOT NULL,
    permission_id       BIGINT          NOT NULL,

    CONSTRAINT pk_role_permissions PRIMARY KEY (role_permission_id),
    CONSTRAINT fk_role_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT fk_role_permissions_permission_id FOREIGN KEY (permission_id) REFERENCES permissions(permission_id),
    CONSTRAINT uq_role_permissions_role_perm UNIQUE (role_id, permission_id)
);

CREATE TABLE users (
    user_id             BIGINT          AUTO_INCREMENT,
    role_id             BIGINT,
    full_name           VARCHAR(120)    NOT NULL,
    email               VARCHAR(150)    NOT NULL,
    phone               VARCHAR(20),
    password            VARCHAR(255),
    google_id           VARCHAR(255),
    avatar              VARCHAR(500),
    aadhaar_number      VARCHAR(12),
    pan_number          VARCHAR(10),
    instagram_username  VARCHAR(255),
    website_url         VARCHAR(500),
    account_status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    email_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    phone_verified      BOOLEAN         NOT NULL DEFAULT FALSE,
    kyc_verified        BOOLEAN         NOT NULL DEFAULT FALSE,
    admin_approval_flag CHAR(1)         NOT NULL DEFAULT 'N',
    last_login_at       DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          DATETIME,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles(role_id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_phone UNIQUE (phone),
    CONSTRAINT uq_users_google_id UNIQUE (google_id),
    CONSTRAINT uq_users_aadhaar UNIQUE (aadhaar_number),
    CONSTRAINT uq_users_pan UNIQUE (pan_number)
);

-- ============================================================================
-- DOMAIN 2: USER PROFILES (1 table)
-- ============================================================================

CREATE TABLE user_profiles (
    user_profile_id     BIGINT          AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    display_name        VARCHAR(150),
    bio                 VARCHAR(500),
    profile_picture_url VARCHAR(255),
    contact_email       VARCHAR(255),
    contact_phone       VARCHAR(50),
    social_links        JSON,
    preferred_language  VARCHAR(100),
    timezone            VARCHAR(100),
    preferred_destinations JSON,
    travel_styles       JSON,
    newsletter_subscribed BOOLEAN       DEFAULT TRUE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_profiles PRIMARY KEY (user_profile_id),
    CONSTRAINT fk_user_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_user_profiles_user_id UNIQUE (user_id)
);

-- ============================================================================
-- DOMAIN 3: TRIP (12 tables)
-- ============================================================================

CREATE TABLE destinations (
    destination_id      BIGINT          AUTO_INCREMENT,
    country             VARCHAR(100),
    state               VARCHAR(100),
    city                VARCHAR(100),
    latitude            DOUBLE,
    longitude           DOUBLE,
    image_url           VARCHAR(500),
    description         TEXT,
    is_active           BOOLEAN         DEFAULT TRUE,

    CONSTRAINT pk_destinations PRIMARY KEY (destination_id)
);

CREATE TABLE trips (
    trip_id             BIGINT          AUTO_INCREMENT,
    planner_id          BIGINT,
    host_id             BIGINT,
    destination_id      BIGINT,
    title               VARCHAR(255),
    description         TEXT,
    base_price          DECIMAL(12,2),
    min_price           DECIMAL(12,2),
    max_price           DECIMAL(12,2),
    current_price       DECIMAL(12,2),
    max_discount_percent DECIMAL(5,2)   NOT NULL DEFAULT 0,
    max_increase_percent DECIMAL(5,2)   NOT NULL DEFAULT 0,
    start_date          DATE,
    end_date            DATE,
    total_seats         INT,
    booked_seats        INT             DEFAULT 0,
    category            VARCHAR(50),
    status              VARCHAR(50)     NOT NULL DEFAULT 'DRAFT',
    approval_status     VARCHAR(20)     DEFAULT 'PENDING',
    cancellation_policy VARCHAR(50),
    rejection_reason    VARCHAR(255),
    difficulty          VARCHAR(50),
    group_size_label    VARCHAR(50),
    trip_type           VARCHAR(100),
    best_time           VARCHAR(100),
    starts_from         VARCHAR(255),
    ends_at             VARCHAR(255),
    about_description   TEXT,
    min_group_size      INT,
    duration_days       INT,
    duration_nights     INT,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    is_deleted          BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          DATETIME,

    CONSTRAINT pk_trips PRIMARY KEY (trip_id),
    CONSTRAINT fk_trips_planner_id FOREIGN KEY (planner_id) REFERENCES users(user_id),
    CONSTRAINT fk_trips_host_id FOREIGN KEY (host_id) REFERENCES users(user_id),
    CONSTRAINT fk_trips_destination_id FOREIGN KEY (destination_id) REFERENCES destinations(destination_id)
);

CREATE TABLE trip_batches (
    trip_batch_id       BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    start_date          DATE            NOT NULL,
    end_date            DATE            NOT NULL,
    price               DECIMAL(12,2)   NOT NULL,
    seats_available     INT             NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'OPEN',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_trip_batches PRIMARY KEY (trip_batch_id),
    CONSTRAINT fk_trip_batches_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT uq_trip_batches_trip_dates UNIQUE (trip_id, start_date, end_date)
);

CREATE TABLE trip_itinerary_days (
    trip_itinerary_day_id BIGINT        AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    day_number          INT             NOT NULL,
    title               VARCHAR(255)    NOT NULL,
    description         TEXT,
    stay                VARCHAR(255),
    meals               VARCHAR(255),
    sort_order          INT,

    CONSTRAINT pk_trip_itinerary_days PRIMARY KEY (trip_itinerary_day_id),
    CONSTRAINT fk_trip_itinerary_days_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
);

CREATE TABLE trip_highlights (
    trip_highlight_id   BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    icon                VARCHAR(50)     NOT NULL,
    title               VARCHAR(255)    NOT NULL,
    sort_order          INT             NOT NULL DEFAULT 0,

    CONSTRAINT pk_trip_highlights PRIMARY KEY (trip_highlight_id),
    CONSTRAINT fk_trip_highlights_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
);

CREATE TABLE trip_stops (
    trip_stop_id        BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    stop_name           VARCHAR(255)    NOT NULL,
    sort_order          INT             NOT NULL DEFAULT 0,

    CONSTRAINT pk_trip_stops PRIMARY KEY (trip_stop_id),
    CONSTRAINT fk_trip_stops_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
);

CREATE TABLE trip_items (
    trip_item_id        BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    type                VARCHAR(20)     NOT NULL,
    description         VARCHAR(500)    NOT NULL,
    sort_order          INT             NOT NULL DEFAULT 0,

    CONSTRAINT pk_trip_items PRIMARY KEY (trip_item_id),
    CONSTRAINT fk_trip_items_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
);

CREATE TABLE trip_media (
    trip_media_id       BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    media_type          VARCHAR(50)     NOT NULL,
    url                 VARCHAR(1000)   NOT NULL,
    caption             VARCHAR(255),
    is_cover            BOOLEAN         DEFAULT FALSE,
    sort_order          INT,

    CONSTRAINT pk_trip_media PRIMARY KEY (trip_media_id),
    CONSTRAINT fk_trip_media_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
);

CREATE TABLE trip_price_breakdown (
    trip_price_breakdown_id BIGINT      AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    category            VARCHAR(255)    NOT NULL,
    amount              DECIMAL(12,2)   NOT NULL,
    description         VARCHAR(500),
    sort_order          INT             NOT NULL DEFAULT 0,

    CONSTRAINT pk_trip_price_breakdown PRIMARY KEY (trip_price_breakdown_id),
    CONSTRAINT fk_trip_price_breakdown_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
);

CREATE TABLE trip_stays (
    trip_stay_id        BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    location            VARCHAR(255),
    description         TEXT,
    sort_order          INT             NOT NULL DEFAULT 0,

    CONSTRAINT pk_trip_stays PRIMARY KEY (trip_stay_id),
    CONSTRAINT fk_trip_stays_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE
);

CREATE TABLE trip_stay_amenities (
    trip_stay_amenity_id BIGINT         AUTO_INCREMENT,
    trip_stay_id        BIGINT          NOT NULL,
    amenity             VARCHAR(255)    NOT NULL,

    CONSTRAINT pk_trip_stay_amenities PRIMARY KEY (trip_stay_amenity_id),
    CONSTRAINT fk_trip_stay_amenities_trip_stay_id FOREIGN KEY (trip_stay_id) REFERENCES trip_stays(trip_stay_id) ON DELETE CASCADE
);

CREATE TABLE trip_stay_images (
    trip_stay_image_id  BIGINT          AUTO_INCREMENT,
    trip_stay_id        BIGINT          NOT NULL,
    image_url           VARCHAR(1000)   NOT NULL,
    sort_order          INT             NOT NULL DEFAULT 0,

    CONSTRAINT pk_trip_stay_images PRIMARY KEY (trip_stay_image_id),
    CONSTRAINT fk_trip_stay_images_trip_stay_id FOREIGN KEY (trip_stay_id) REFERENCES trip_stays(trip_stay_id) ON DELETE CASCADE
);

CREATE TABLE trip_badges (
    trip_badge_id       BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    badge_name          VARCHAR(100)    NOT NULL,

    CONSTRAINT pk_trip_badges PRIMARY KEY (trip_badge_id),
    CONSTRAINT fk_trip_badges_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT uq_trip_badges_trip_badge UNIQUE (trip_id, badge_name)
);

-- ============================================================================
-- DOMAIN 4: BOOKING (1 table)
-- ============================================================================

CREATE TABLE bookings (
    booking_id          BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT          NOT NULL,
    traveler_id         BIGINT          NOT NULL,
    batch_id            BIGINT,
    coupon_id           BIGINT,
    booking_ref         VARCHAR(50)     NOT NULL,
    seats_booked        INT             NOT NULL,
    base_amount         DECIMAL(12,2),
    discount_amount     DECIMAL(12,2)   DEFAULT 0,
    tax_amount          DECIMAL(12,2)   DEFAULT 0,
    total_price         DECIMAL(12,2)   NOT NULL,
    amount_paid         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    amount_pending      DECIMAL(12,2)   NOT NULL DEFAULT 0,
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    payment_status      VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    cancellation_reason VARCHAR(255),
    expires_at          DATETIME,
    confirmed_at        DATETIME,
    completed_at        DATETIME,
    cancelled_at        DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_bookings PRIMARY KEY (booking_id),
    CONSTRAINT fk_bookings_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id),
    CONSTRAINT fk_bookings_traveler_id FOREIGN KEY (traveler_id) REFERENCES users(user_id),
    CONSTRAINT fk_bookings_batch_id FOREIGN KEY (batch_id) REFERENCES trip_batches(trip_batch_id),
    CONSTRAINT uq_bookings_booking_ref UNIQUE (booking_ref),
    CONSTRAINT chk_bookings_positive_seats CHECK (seats_booked > 0),
    CONSTRAINT chk_bookings_non_negative_price CHECK (total_price >= 0)
);

-- ============================================================================
-- DOMAIN 5: PAYMENT (6 tables)
-- ============================================================================

CREATE TABLE payment_stages (
    payment_stage_id    BIGINT          AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    stage_number        INT             NOT NULL,
    label               VARCHAR(255)    NOT NULL,
    amount              DECIMAL(12,2)   NOT NULL,
    percentage          DECIMAL(5,2),
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    due_date            DATE,
    paid_at             DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_payment_stages PRIMARY KEY (payment_stage_id),
    CONSTRAINT fk_payment_stages_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    CONSTRAINT uq_payment_stages_booking_stage UNIQUE (booking_id, stage_number),
    CONSTRAINT chk_payment_stages_positive_amount CHECK (amount > 0),
    CONSTRAINT chk_payment_stages_percentage CHECK (percentage IS NULL OR (percentage >= 0 AND percentage <= 100))
);

CREATE TABLE payments (
    payment_id          BIGINT          AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    payment_stage_id    BIGINT,
    razorpay_order_id   VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    razorpay_signature  VARCHAR(255),
    amount              DECIMAL(12,2)   NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'CREATED',
    currency            VARCHAR(10)     NOT NULL DEFAULT 'INR',
    payment_method      VARCHAR(50),
    gateway_fee         DECIMAL(10,2)   DEFAULT 0,
    failure_reason      VARCHAR(255),
    attempt_number      INT             NOT NULL DEFAULT 1,
    paid_at             DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payments PRIMARY KEY (payment_id),
    CONSTRAINT fk_payments_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_payments_payment_stage_id FOREIGN KEY (payment_stage_id) REFERENCES payment_stages(payment_stage_id),
    CONSTRAINT uidx_payments_razorpay_order_id UNIQUE (razorpay_order_id),
    CONSTRAINT uidx_payments_razorpay_payment_id UNIQUE (razorpay_payment_id)
);

CREATE TABLE refunds (
    refund_id           BIGINT          AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    payment_id          BIGINT,
    requested_by        BIGINT,
    processed_by        BIGINT,
    razorpay_refund_id  VARCHAR(255),
    original_amount     DECIMAL(12,2)   NOT NULL,
    refund_amount       DECIMAL(12,2)   NOT NULL,
    refund_type         VARCHAR(50)     NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    reason              VARCHAR(255),
    admin_notes         VARCHAR(500),
    requested_at        DATETIME,
    processed_at        DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_refunds PRIMARY KEY (refund_id),
    CONSTRAINT fk_refunds_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_refunds_payment_id FOREIGN KEY (payment_id) REFERENCES payments(payment_id),
    CONSTRAINT fk_refunds_requested_by FOREIGN KEY (requested_by) REFERENCES users(user_id),
    CONSTRAINT fk_refunds_processed_by FOREIGN KEY (processed_by) REFERENCES users(user_id)
);

CREATE TABLE commissions (
    commission_id       BIGINT          AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    payment_id          BIGINT,
    payment_amount      DECIMAL(12,2)   NOT NULL,
    commission_rate     DECIMAL(5,2)    NOT NULL,
    commission_amount   DECIMAL(12,2)   NOT NULL,
    tax_on_commission   DECIMAL(12,2)   DEFAULT 0,
    net_platform_earning DECIMAL(12,2)  NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'CALCULATED',
    calculated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_commissions PRIMARY KEY (commission_id),
    CONSTRAINT fk_commissions_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_commissions_payment_id FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
);

CREATE TABLE bank_accounts (
    bank_account_id     BIGINT          AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    account_holder_name VARCHAR(255)    NOT NULL,
    bank_name           VARCHAR(255)    NOT NULL,
    account_number      VARCHAR(100)    NOT NULL,
    ifsc_code           VARCHAR(50)     NOT NULL,
    upi_id              VARCHAR(255),
    is_primary          BOOLEAN         DEFAULT FALSE,
    is_verified         BOOLEAN         DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_bank_accounts PRIMARY KEY (bank_account_id),
    CONSTRAINT fk_bank_accounts_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE payouts (
    payout_id           BIGINT          AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    payee_id            BIGINT          NOT NULL,
    bank_account_id     BIGINT,
    payee_type          VARCHAR(50)     NOT NULL,
    gross_amount        DECIMAL(12,2)   NOT NULL,
    commission_deducted DECIMAL(12,2)   NOT NULL,
    tds_deducted        DECIMAL(12,2)   DEFAULT 0,
    net_amount          DECIMAL(12,2)   NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    razorpay_transfer_id VARCHAR(255),
    utr_number          VARCHAR(255),
    requested_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at        DATETIME,
    released_at         DATETIME,

    CONSTRAINT pk_payouts PRIMARY KEY (payout_id),
    CONSTRAINT fk_payouts_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_payouts_payee_id FOREIGN KEY (payee_id) REFERENCES users(user_id),
    CONSTRAINT fk_payouts_bank_account_id FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(bank_account_id)
);

-- ============================================================================
-- DOMAIN 6: VERIFICATION (2 tables)
-- ============================================================================

CREATE TABLE host_verifications (
    host_verification_id BIGINT         AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    display_name        VARCHAR(100)    NOT NULL,
    bio                 TEXT,
    specialization      VARCHAR(255),
    experience_years    INT,
    aadhaar_document_url LONGTEXT,
    pan_document_url    LONGTEXT,
    selfie_url          LONGTEXT,
    verification_status VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    rejection_reason    VARCHAR(500),
    submitted_at        DATETIME,
    reviewed_at         DATETIME,
    reviewed_by         BIGINT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_host_verifications PRIMARY KEY (host_verification_id),
    CONSTRAINT fk_host_verifications_user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_host_verifications_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(user_id),
    CONSTRAINT uq_host_verifications_user_id UNIQUE (user_id)
);

CREATE TABLE planner_verifications (
    planner_verification_id BIGINT      AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    display_name        VARCHAR(100)    NOT NULL,
    bio                 TEXT,
    specialization      VARCHAR(255),
    experience_years    INT,
    aadhaar_document_url LONGTEXT,
    pan_document_url    LONGTEXT,
    selfie_url          LONGTEXT,
    verification_status VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    rejection_reason    VARCHAR(500),
    submitted_at        DATETIME,
    reviewed_at         DATETIME,
    reviewed_by         BIGINT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_planner_verifications PRIMARY KEY (planner_verification_id),
    CONSTRAINT fk_planner_verifications_user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_planner_verifications_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users(user_id),
    CONSTRAINT uq_planner_verifications_user_id UNIQUE (user_id)
);

-- ============================================================================
-- DOMAIN 7: REVIEWS & MODERATION (4 tables)
-- ============================================================================

CREATE TABLE reviews (
    review_id           BIGINT          AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    reviewer_id         BIGINT          NOT NULL,
    target_type         VARCHAR(50)     NOT NULL,
    target_id           BIGINT          NOT NULL,
    rating              INT             NOT NULL,
    comment             TEXT,
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_reviews PRIMARY KEY (review_id),
    CONSTRAINT fk_reviews_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_reviews_reviewer_id FOREIGN KEY (reviewer_id) REFERENCES users(user_id),
    CONSTRAINT chk_reviews_rating_range CHECK (rating BETWEEN 1 AND 5)
);

CREATE TABLE review_media (
    review_media_id     BIGINT          AUTO_INCREMENT,
    review_id           BIGINT          NOT NULL,
    url                 VARCHAR(500)    NOT NULL,
    media_type          VARCHAR(50)     NOT NULL,

    CONSTRAINT pk_review_media PRIMARY KEY (review_media_id),
    CONSTRAINT fk_review_media_review_id FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE
);

CREATE TABLE disputes (
    dispute_id          BIGINT          AUTO_INCREMENT,
    booking_id          BIGINT,
    filed_by            BIGINT          NOT NULL,
    against_user_id     BIGINT          NOT NULL,
    title               VARCHAR(255)    NOT NULL,
    description         TEXT            NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'OPEN',
    priority            VARCHAR(50)     NOT NULL DEFAULT 'MEDIUM',
    admin_notes         TEXT,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at         DATETIME,

    CONSTRAINT pk_disputes PRIMARY KEY (dispute_id),
    CONSTRAINT fk_disputes_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_disputes_filed_by FOREIGN KEY (filed_by) REFERENCES users(user_id),
    CONSTRAINT fk_disputes_against_user_id FOREIGN KEY (against_user_id) REFERENCES users(user_id)
);

CREATE TABLE dispute_evidence (
    dispute_evidence_id BIGINT          AUTO_INCREMENT,
    dispute_id          BIGINT          NOT NULL,
    type                VARCHAR(50)     NOT NULL,
    label               VARCHAR(255)    NOT NULL,
    url                 VARCHAR(500)    NOT NULL,
    uploaded_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_dispute_evidence PRIMARY KEY (dispute_evidence_id),
    CONSTRAINT fk_dispute_evidence_dispute_id FOREIGN KEY (dispute_id) REFERENCES disputes(dispute_id) ON DELETE CASCADE
);

-- ============================================================================
-- DOMAIN 8: SUPPORT (2 tables)
-- ============================================================================

CREATE TABLE support_tickets (
    support_ticket_id   BIGINT          AUTO_INCREMENT,
    created_by          BIGINT          NOT NULL,
    assigned_to         BIGINT,
    subject             VARCHAR(255)    NOT NULL,
    description         TEXT            NOT NULL,
    category            VARCHAR(50)     NOT NULL,
    priority            VARCHAR(50)     NOT NULL DEFAULT 'MEDIUM',
    status              VARCHAR(50)     NOT NULL DEFAULT 'OPEN',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at         DATETIME,

    CONSTRAINT pk_support_tickets PRIMARY KEY (support_ticket_id),
    CONSTRAINT fk_support_tickets_created_by FOREIGN KEY (created_by) REFERENCES users(user_id),
    CONSTRAINT fk_support_tickets_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(user_id)
);

CREATE TABLE ticket_messages (
    ticket_message_id   BIGINT          AUTO_INCREMENT,
    support_ticket_id   BIGINT          NOT NULL,
    sender_id           BIGINT          NOT NULL,
    content             TEXT            NOT NULL,
    is_admin            BOOLEAN         DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ticket_messages PRIMARY KEY (ticket_message_id),
    CONSTRAINT fk_ticket_messages_support_ticket_id FOREIGN KEY (support_ticket_id) REFERENCES support_tickets(support_ticket_id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_messages_sender_id FOREIGN KEY (sender_id) REFERENCES users(user_id)
);

-- ============================================================================
-- DOMAIN 9: MARKETING (4 tables)
-- ============================================================================

CREATE TABLE coupons (
    coupon_id           BIGINT          AUTO_INCREMENT,
    trip_id             BIGINT,
    destination_id      BIGINT,
    code                VARCHAR(50)     NOT NULL,
    discount_type       VARCHAR(50)     NOT NULL,
    discount_value      DECIMAL(12,2)   NOT NULL,
    max_uses            INT             NOT NULL DEFAULT 1,
    used_count          INT             NOT NULL DEFAULT 0,
    min_order_value     DECIMAL(12,2)   DEFAULT 0,
    status              VARCHAR(50)     NOT NULL DEFAULT 'ACTIVE',
    valid_from          DATETIME        NOT NULL,
    valid_to            DATETIME        NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_coupons PRIMARY KEY (coupon_id),
    CONSTRAINT fk_coupons_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id),
    CONSTRAINT fk_coupons_destination_id FOREIGN KEY (destination_id) REFERENCES destinations(destination_id),
    CONSTRAINT uq_coupons_code UNIQUE (code),
    CONSTRAINT chk_coupons_positive_discount CHECK (discount_value > 0),
    CONSTRAINT chk_coupons_valid_date_range CHECK (valid_from < valid_to)
);

-- Add coupon FK to bookings now that coupons table exists
ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_coupon_id FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id);

CREATE TABLE coupon_usages (
    coupon_usage_id     BIGINT          AUTO_INCREMENT,
    coupon_id           BIGINT          NOT NULL,
    booking_id          BIGINT          NOT NULL,
    user_id             BIGINT          NOT NULL,
    discount_applied    DECIMAL(12,2)   NOT NULL,
    used_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_coupon_usages PRIMARY KEY (coupon_usage_id),
    CONSTRAINT fk_coupon_usages_coupon_id FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id),
    CONSTRAINT fk_coupon_usages_booking_id FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_coupon_usages_user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uq_coupon_usages_booking_coupon UNIQUE (booking_id, coupon_id)
);

CREATE TABLE announcements (
    announcement_id     BIGINT          AUTO_INCREMENT,
    title               VARCHAR(255)    NOT NULL,
    content             TEXT            NOT NULL,
    type                VARCHAR(50)     NOT NULL,
    is_active           BOOLEAN         DEFAULT TRUE,
    expires_at          DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_announcements PRIMARY KEY (announcement_id)
);

CREATE TABLE announcement_audiences (
    announcement_audience_id BIGINT     AUTO_INCREMENT,
    announcement_id     BIGINT          NOT NULL,
    target_role         VARCHAR(50)     NOT NULL,

    CONSTRAINT pk_announcement_audiences PRIMARY KEY (announcement_audience_id),
    CONSTRAINT fk_announcement_audiences_announcement_id FOREIGN KEY (announcement_id) REFERENCES announcements(announcement_id) ON DELETE CASCADE
);

-- ============================================================================
-- DOMAIN 10: SYSTEM & AUDIT (5 tables + 1 config)
-- ============================================================================

CREATE TABLE notifications (
    notification_id     BIGINT          AUTO_INCREMENT,
    user_id             BIGINT          NOT NULL,
    title               VARCHAR(255)    NOT NULL,
    message             TEXT            NOT NULL,
    type                VARCHAR(50)     NOT NULL,
    target_type         VARCHAR(50),
    target_id           BIGINT,
    read_at             DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE audit_logs (
    audit_log_id        BIGINT          AUTO_INCREMENT,
    performed_by        BIGINT          NOT NULL,
    action              VARCHAR(255)    NOT NULL,
    category            VARCHAR(50)     NOT NULL,
    target_type         VARCHAR(100),
    target_id           BIGINT,
    details             TEXT,
    ip_address          VARCHAR(100),
    device_info         VARCHAR(255),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_audit_logs PRIMARY KEY (audit_log_id),
    CONSTRAINT fk_audit_logs_performed_by FOREIGN KEY (performed_by) REFERENCES users(user_id)
);

CREATE TABLE webhook_logs (
    webhook_log_id      BIGINT          AUTO_INCREMENT,
    provider            VARCHAR(50)     NOT NULL,
    event_type          VARCHAR(100)    NOT NULL,
    payload             JSON            NOT NULL,
    status              VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    error_message       TEXT,
    received_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at        DATETIME,

    CONSTRAINT pk_webhook_logs PRIMARY KEY (webhook_log_id)
);

CREATE TABLE email_logs (
    email_log_id        BIGINT          AUTO_INCREMENT,
    recipient_email     VARCHAR(255)    NOT NULL,
    subject             VARCHAR(255)    NOT NULL,
    template_id         VARCHAR(100),
    status              VARCHAR(50)     NOT NULL,
    provider_message_id VARCHAR(255),
    error_message       TEXT,
    sent_at             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_email_logs PRIMARY KEY (email_log_id)
);

CREATE TABLE media_assets (
    media_asset_id      BIGINT          AUTO_INCREMENT,
    uploaded_by         BIGINT,
    file_name           VARCHAR(255)    NOT NULL,
    file_url            VARCHAR(500)    NOT NULL,
    file_type           VARCHAR(50)     NOT NULL,
    file_size           BIGINT          NOT NULL,
    provider            VARCHAR(50)     NOT NULL,
    provider_public_id  VARCHAR(255),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_media_assets PRIMARY KEY (media_asset_id),
    CONSTRAINT fk_media_assets_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(user_id)
);

CREATE TABLE platform_settings (
    platform_setting_id BIGINT          AUTO_INCREMENT,
    setting_key         VARCHAR(100)    NOT NULL,
    label               VARCHAR(255)    NOT NULL,
    description         TEXT,
    setting_type        VARCHAR(50)     NOT NULL,
    value               VARCHAR(1000)   NOT NULL,
    category            VARCHAR(50)     NOT NULL,
    updated_by          BIGINT,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_platform_settings PRIMARY KEY (platform_setting_id),
    CONSTRAINT fk_platform_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users(user_id),
    CONSTRAINT uq_platform_settings_key UNIQUE (setting_key)
);

-- ============================================================================
-- INDEXES
-- ============================================================================

-- AUTH
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_users_account_status ON users(account_status);
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- TRIPS
CREATE INDEX idx_trips_planner_id ON trips(planner_id);
CREATE INDEX idx_trips_host_id ON trips(host_id);
CREATE INDEX idx_trips_destination_id ON trips(destination_id);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_approval_status ON trips(approval_status);
CREATE INDEX idx_trips_start_date ON trips(start_date);
CREATE INDEX idx_trips_is_active_is_deleted ON trips(is_active, is_deleted);

-- TRIP CHILDREN
CREATE INDEX idx_trip_batches_trip_id ON trip_batches(trip_id);
CREATE INDEX idx_trip_itinerary_days_trip_id ON trip_itinerary_days(trip_id);
CREATE INDEX idx_trip_highlights_trip_id ON trip_highlights(trip_id);
CREATE INDEX idx_trip_stops_trip_id ON trip_stops(trip_id);
CREATE INDEX idx_trip_items_trip_id ON trip_items(trip_id);
CREATE INDEX idx_trip_media_trip_id ON trip_media(trip_id);
CREATE INDEX idx_trip_price_breakdown_trip_id ON trip_price_breakdown(trip_id);
CREATE INDEX idx_trip_stays_trip_id ON trip_stays(trip_id);
CREATE INDEX idx_trip_stay_amenities_trip_stay_id ON trip_stay_amenities(trip_stay_id);
CREATE INDEX idx_trip_stay_images_trip_stay_id ON trip_stay_images(trip_stay_id);
CREATE INDEX idx_trip_badges_trip_id ON trip_badges(trip_id);

-- BOOKINGS
CREATE INDEX idx_bookings_trip_id ON bookings(trip_id);
CREATE INDEX idx_bookings_traveler_id ON bookings(traveler_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_payment_status ON bookings(payment_status);
CREATE INDEX idx_bookings_created_at ON bookings(created_at);

-- PAYMENT STAGES
CREATE INDEX idx_payment_stages_booking_id ON payment_stages(booking_id);
CREATE INDEX idx_payment_stages_status ON payment_stages(status);
CREATE INDEX idx_payment_stages_due_date ON payment_stages(due_date);

-- PAYMENTS
CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_payment_stage_id ON payments(payment_stage_id);
CREATE INDEX idx_payments_status ON payments(status);

-- REFUNDS
CREATE INDEX idx_refunds_booking_id ON refunds(booking_id);
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_status ON refunds(status);

-- COMMISSIONS
CREATE INDEX idx_commissions_booking_id ON commissions(booking_id);
CREATE INDEX idx_commissions_payment_id ON commissions(payment_id);
CREATE INDEX idx_commissions_status ON commissions(status);

-- PAYOUTS
CREATE INDEX idx_payouts_booking_id ON payouts(booking_id);
CREATE INDEX idx_payouts_payee_id ON payouts(payee_id);
CREATE INDEX idx_payouts_status ON payouts(status);

-- BANK ACCOUNTS
CREATE INDEX idx_bank_accounts_user_id ON bank_accounts(user_id);

-- REVIEWS
CREATE INDEX idx_reviews_booking_id ON reviews(booking_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_reviews_target_type_target_id ON reviews(target_type, target_id);

-- DISPUTES
CREATE INDEX idx_disputes_booking_id ON disputes(booking_id);
CREATE INDEX idx_disputes_filed_by ON disputes(filed_by);
CREATE INDEX idx_disputes_status ON disputes(status);

-- SUPPORT
CREATE INDEX idx_support_tickets_created_by ON support_tickets(created_by);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);

-- NOTIFICATIONS
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read_at ON notifications(read_at);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- AUDIT LOGS
CREATE INDEX idx_audit_logs_performed_by ON audit_logs(performed_by);
CREATE INDEX idx_audit_logs_category ON audit_logs(category);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- COUPONS
CREATE INDEX idx_coupons_status ON coupons(status);
CREATE INDEX idx_coupon_usages_user_id ON coupon_usages(user_id);

-- WEBHOOK / EMAIL LOGS
CREATE INDEX idx_webhook_logs_status ON webhook_logs(status);
CREATE INDEX idx_webhook_logs_received_at ON webhook_logs(received_at);
CREATE INDEX idx_email_logs_status ON email_logs(status);
