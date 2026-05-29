-- --------------------------------------------------------------------------------
-- V5__address_architecture_feedback.sql
-- Fix relationships, constraints, and add missing infrastructure tables
-- --------------------------------------------------------------------------------

-- ==============================================================================
-- 1. MODIFY EXISTING TABLES (Constraints & Foreign Keys)
-- ==============================================================================

-- 1.1 trip_inclusions: Shift FK from trips to trip_batches
ALTER TABLE trip_inclusions DROP FOREIGN KEY trip_inclusions_ibfk_1;
ALTER TABLE trip_inclusions DROP COLUMN trip_id;
ALTER TABLE trip_inclusions ADD COLUMN batch_id BIGINT NOT NULL;
ALTER TABLE trip_inclusions ADD CONSTRAINT fk_trip_inclusions_batch FOREIGN KEY (batch_id) REFERENCES trip_batches(id);

-- 1.2 reviews: Drop redundant trip_id (use polymorphic target_type + target_id)
ALTER TABLE reviews DROP FOREIGN KEY reviews_ibfk_3;
ALTER TABLE reviews DROP COLUMN trip_id;

-- 1.3 payouts: Drop redundant escrow_status
ALTER TABLE payouts DROP COLUMN escrow_status;

-- 1.4 coupons: Add targeted scope columns
ALTER TABLE coupons ADD COLUMN trip_id BIGINT;
ALTER TABLE coupons ADD COLUMN destination_id BIGINT;
ALTER TABLE coupons ADD CONSTRAINT fk_coupons_trip FOREIGN KEY (trip_id) REFERENCES trips(id);
ALTER TABLE coupons ADD CONSTRAINT fk_coupons_destination FOREIGN KEY (destination_id) REFERENCES destinations(id);

-- 1.5 platform_settings: Add updated_by audit trail
ALTER TABLE platform_settings ADD COLUMN updated_by BIGINT;
ALTER TABLE platform_settings ADD CONSTRAINT fk_platform_settings_user FOREIGN KEY (updated_by) REFERENCES users(id);

-- 1.6 coupon_usages: Prevent double-dipping
ALTER TABLE coupon_usages ADD CONSTRAINT uq_booking_coupon UNIQUE (booking_id, coupon_id);

-- ==============================================================================
-- 2. CREATE NEW INFRASTRUCTURE TABLES
-- ==============================================================================

-- 2.1 notifications
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- BOOKING, PAYMENT, SYSTEM, DISPUTE
    target_type VARCHAR(50), -- BOOKING, TRIP, DISPUTE
    target_id BIGINT,
    read_at DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 2.2 webhook_logs
CREATE TABLE webhook_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(50) NOT NULL, -- RAZORPAY
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, PROCESSED, FAILED
    error_message TEXT,
    received_at DATETIME NOT NULL,
    processed_at DATETIME
);

-- 2.3 email_logs
CREATE TABLE email_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_id VARCHAR(100),
    status VARCHAR(50) NOT NULL, -- SENT, DELIVERED, BOUNCED, FAILED
    provider_message_id VARCHAR(255),
    error_message TEXT,
    sent_at DATETIME NOT NULL
);

-- 2.4 media_assets
CREATE TABLE media_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uploaded_by BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(50) NOT NULL, -- IMAGE, VIDEO, DOCUMENT
    file_size BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL, -- S3, CLOUDINARY
    provider_public_id VARCHAR(255),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);
