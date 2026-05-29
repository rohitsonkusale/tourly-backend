-- --------------------------------------------------------------------------------
-- V4__complete_database_redesign.sql
-- Comprehensive database architecture redesign for Roamaya/Tourly platform
-- --------------------------------------------------------------------------------

-- ==============================================================================
-- 1. MODIFY EXISTING TABLES
-- ==============================================================================

-- 1.1 Add columns to trips
ALTER TABLE trips
    ADD COLUMN host_id BIGINT,
    ADD COLUMN approval_status VARCHAR(20) DEFAULT 'PENDING',
    ADD COLUMN rejection_reason VARCHAR(255),
    ADD COLUMN difficulty VARCHAR(50),
    ADD COLUMN group_size_label VARCHAR(50),
    ADD COLUMN trip_type VARCHAR(100),
    ADD COLUMN best_time VARCHAR(100),
    ADD CONSTRAINT fk_trips_host FOREIGN KEY (host_id) REFERENCES users(id);

-- 1.2 Add columns to bookings
ALTER TABLE bookings
    ADD COLUMN booking_ref VARCHAR(50) UNIQUE,
    ADD COLUMN batch_id BIGINT,
    ADD COLUMN base_amount DECIMAL(12, 2),
    ADD COLUMN discount_amount DECIMAL(12, 2) DEFAULT 0,
    ADD COLUMN tax_amount DECIMAL(12, 2) DEFAULT 0,
    ADD COLUMN cancellation_reason VARCHAR(255),
    ADD COLUMN cancelled_at DATETIME,
    ADD COLUMN confirmed_at DATETIME,
    ADD COLUMN completed_at DATETIME;

-- 1.3 Add columns to payments
ALTER TABLE payments
    ADD COLUMN stage_id BIGINT,
    ADD COLUMN razorpay_signature VARCHAR(255),
    ADD COLUMN gateway_fee DECIMAL(10, 2) DEFAULT 0,
    ADD COLUMN currency VARCHAR(10) DEFAULT 'INR',
    ADD COLUMN payment_method VARCHAR(50),
    ADD COLUMN failure_reason VARCHAR(255),
    ADD COLUMN paid_at DATETIME,
    ADD COLUMN updated_at DATETIME;

-- 1.4 Add columns to destinations
ALTER TABLE destinations
    ADD COLUMN image_url VARCHAR(500),
    ADD COLUMN description TEXT,
    ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- ==============================================================================
-- 2. CREATE NEW TABLES - TRIP DOMAIN
-- ==============================================================================

CREATE TABLE trip_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    seats_available INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (trip_id) REFERENCES trips(id)
);

-- Add foreign key to bookings for batch_id now that trip_batches exists
ALTER TABLE bookings
    ADD CONSTRAINT fk_bookings_batch FOREIGN KEY (batch_id) REFERENCES trip_batches(id);

CREATE TABLE trip_itinerary_days (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    day_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    stay VARCHAR(255),
    meals VARCHAR(255),
    sort_order INT,
    FOREIGN KEY (trip_id) REFERENCES trips(id)
);

CREATE TABLE trip_inclusions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL, -- INCLUSION, EXCLUSION
    description VARCHAR(255) NOT NULL,
    sort_order INT,
    FOREIGN KEY (trip_id) REFERENCES trips(id)
);

CREATE TABLE trip_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    media_type VARCHAR(50) NOT NULL, -- IMAGE, VIDEO
    url VARCHAR(500) NOT NULL,
    caption VARCHAR(255),
    is_cover BOOLEAN DEFAULT FALSE,
    sort_order INT,
    FOREIGN KEY (trip_id) REFERENCES trips(id)
);

-- ==============================================================================
-- 3. CREATE NEW TABLES - FINANCIAL ENGINE
-- ==============================================================================

CREATE TABLE payment_stages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    stage_number INT NOT NULL,
    label VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    percentage DECIMAL(5, 2),
    status VARCHAR(50) NOT NULL, -- PENDING, PAID, CANCELLED
    due_date DATE,
    paid_at DATETIME,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Add foreign key to payments for stage_id now that payment_stages exists
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_stage FOREIGN KEY (stage_id) REFERENCES payment_stages(id);

CREATE TABLE refunds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    payment_id BIGINT,
    requested_by BIGINT,
    processed_by BIGINT,
    razorpay_refund_id VARCHAR(255),
    original_amount DECIMAL(12, 2) NOT NULL,
    refund_amount DECIMAL(12, 2) NOT NULL,
    refund_type VARCHAR(50) NOT NULL, -- FULL, PARTIAL, WALLET, MANUAL
    status VARCHAR(50) NOT NULL, -- PENDING, APPROVED, REJECTED, PROCESSED
    reason VARCHAR(255),
    admin_notes VARCHAR(500),
    requested_at DATETIME,
    processed_at DATETIME,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (requested_by) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id)
);

CREATE TABLE commissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    booking_amount DECIMAL(12, 2) NOT NULL,
    commission_rate DECIMAL(5, 2) NOT NULL,
    commission_amount DECIMAL(12, 2) NOT NULL,
    tax_on_commission DECIMAL(12, 2) DEFAULT 0,
    net_platform_earning DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL, -- CALCULATED, REALIZED, CANCELLED
    calculated_at DATETIME NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_holder_name VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_number VARCHAR(100) NOT NULL,
    ifsc_code VARCHAR(50) NOT NULL,
    upi_id VARCHAR(255),
    is_primary BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE payouts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    payee_id BIGINT NOT NULL,
    bank_account_id BIGINT,
    payee_type VARCHAR(50) NOT NULL, -- HOST, PLANNER
    gross_amount DECIMAL(12, 2) NOT NULL,
    commission_deducted DECIMAL(12, 2) NOT NULL,
    tds_deducted DECIMAL(12, 2) DEFAULT 0,
    net_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, ON_HOLD, FAILED, RELEASED
    razorpay_transfer_id VARCHAR(255),
    utr_number VARCHAR(255),
    escrow_status VARCHAR(50) NOT NULL, -- HELD, RELEASED, EXPIRED
    requested_at DATETIME NOT NULL,
    processed_at DATETIME,
    released_at DATETIME,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (payee_id) REFERENCES users(id),
    FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id)
);

-- ==============================================================================
-- 4. CREATE NEW TABLES - VERIFICATION, MODERATION & SUPPORT
-- ==============================================================================

CREATE TABLE host_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    bio TEXT,
    specialization VARCHAR(255),
    experience_years INT,
    aadhaar_number VARCHAR(12) NOT NULL,
    pan_number VARCHAR(10) NOT NULL,
    aadhaar_document_url VARCHAR(500),
    pan_document_url VARCHAR(500),
    verification_status VARCHAR(50) NOT NULL,
    rejection_reason VARCHAR(500),
    submitted_at DATETIME,
    reviewed_at DATETIME,
    reviewed_by BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    target_type VARCHAR(50) NOT NULL, -- TRIP, HOST, PLANNER
    target_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    status VARCHAR(50) NOT NULL, -- PENDING, APPROVED, REMOVED
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (reviewer_id) REFERENCES users(id),
    FOREIGN KEY (trip_id) REFERENCES trips(id)
);

CREATE TABLE review_media (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    media_type VARCHAR(50) NOT NULL,
    FOREIGN KEY (review_id) REFERENCES reviews(id)
);

CREATE TABLE disputes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT,
    filed_by BIGINT NOT NULL,
    against_user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL, -- OPEN, UNDER_REVIEW, WAITING_RESPONSE, RESOLVED, REJECTED
    priority VARCHAR(50) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    admin_notes TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    resolved_at DATETIME,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (filed_by) REFERENCES users(id),
    FOREIGN KEY (against_user_id) REFERENCES users(id)
);

CREATE TABLE dispute_evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispute_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL, -- SCREENSHOT, CHAT_LOG, DOCUMENT
    label VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    uploaded_at DATETIME NOT NULL,
    FOREIGN KEY (dispute_id) REFERENCES disputes(id)
);

CREATE TABLE support_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by BIGINT NOT NULL,
    assigned_to BIGINT,
    subject VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL, -- PAYMENT, BOOKING, REFUND, VERIFICATION, SAFETY, TECHNICAL
    priority VARCHAR(50) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    status VARCHAR(50) NOT NULL, -- OPEN, IN_PROGRESS, WAITING, RESOLVED, CLOSED
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    resolved_at DATETIME,
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE TABLE ticket_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (ticket_id) REFERENCES support_tickets(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- ==============================================================================
-- 5. CREATE NEW TABLES - MARKETING & SYSTEM
-- ==============================================================================

CREATE TABLE coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(50) NOT NULL, -- PERCENTAGE, FLAT
    discount_value DECIMAL(12, 2) NOT NULL,
    max_uses INT NOT NULL DEFAULT 1,
    used_count INT NOT NULL DEFAULT 0,
    min_order_value DECIMAL(12, 2) DEFAULT 0,
    status VARCHAR(50) NOT NULL, -- ACTIVE, EXPIRED, PAUSED
    valid_from DATETIME NOT NULL,
    valid_to DATETIME NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE coupon_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    discount_applied DECIMAL(12, 2) NOT NULL,
    used_at DATETIME NOT NULL,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- INFO, WARNING, EMERGENCY, MAINTENANCE, PROMOTION
    is_active BOOLEAN DEFAULT TRUE,
    expires_at DATETIME,
    created_at DATETIME NOT NULL
);

CREATE TABLE announcement_audiences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL,
    target_role VARCHAR(50) NOT NULL, -- ALL, TRAVELER, HOST, PLANNER, ADMIN
    FOREIGN KEY (announcement_id) REFERENCES announcements(id)
);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    performed_by BIGINT NOT NULL,
    action VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL, -- USER, FINANCIAL, MODERATION, SYSTEM, SECURITY
    target_type VARCHAR(100),
    target_id BIGINT,
    details TEXT,
    ip_address VARCHAR(100),
    device_info VARCHAR(255),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (performed_by) REFERENCES users(id)
);

CREATE TABLE platform_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL,
    description TEXT,
    setting_type VARCHAR(50) NOT NULL, -- TOGGLE, NUMBER, TEXT, SELECT
    value VARCHAR(1000) NOT NULL,
    category VARCHAR(50) NOT NULL, -- PLATFORM, SECURITY, FEATURES
    updated_at DATETIME NOT NULL
);

-- ==============================================================================
-- 6. CREATE INDEXES
-- ==============================================================================

CREATE INDEX idx_bookings_traveler_id ON bookings(traveler_id);
CREATE INDEX idx_bookings_trip_id ON bookings(trip_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_booking_ref ON bookings(booking_ref);
CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_razorpay_order ON payments(razorpay_order_id);
CREATE INDEX idx_trips_planner_id ON trips(planner_id);
CREATE INDEX idx_trips_host_id ON trips(host_id);
CREATE INDEX idx_trips_destination_id ON trips(destination_id);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_start_date ON trips(start_date);
CREATE INDEX idx_payouts_payee_id ON payouts(payee_id);
CREATE INDEX idx_payouts_status ON payouts(status);
CREATE INDEX idx_commissions_booking_id ON commissions(booking_id);
CREATE INDEX idx_refunds_booking_id ON refunds(booking_id);
CREATE INDEX idx_reviews_trip_id ON reviews(trip_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_audit_logs_performed_by ON audit_logs(performed_by);
CREATE INDEX idx_audit_logs_category ON audit_logs(category);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_disputes_status ON disputes(status);
CREATE INDEX idx_support_tickets_status ON support_tickets(status);
CREATE INDEX idx_coupons_code ON coupons(code);

-- ==============================================================================
-- 7. CREATE VIEWS
-- ==============================================================================

CREATE VIEW v_platform_earnings AS
SELECT 
    YEAR(calculated_at) AS year,
    MONTH(calculated_at) AS month,
    COUNT(id) AS total_commission_events,
    SUM(booking_amount) AS total_bookings_revenue,
    SUM(commission_amount) AS gross_commissions,
    SUM(tax_on_commission) AS total_commission_taxes,
    SUM(net_platform_earning) AS net_platform_earning
FROM commissions
WHERE status = 'REALIZED'
GROUP BY YEAR(calculated_at), MONTH(calculated_at);

CREATE VIEW v_planner_earnings AS
SELECT 
    p.payee_id AS planner_id,
    u.full_name AS planner_name,
    COUNT(p.id) AS payout_events,
    SUM(p.gross_amount) AS gross_revenue,
    SUM(p.commission_deducted) AS commission_paid,
    SUM(p.net_amount) AS net_payouts
FROM payouts p
JOIN users u ON p.payee_id = u.id
WHERE p.payee_type = 'PLANNER' AND p.status = 'RELEASED'
GROUP BY p.payee_id, u.full_name;

CREATE VIEW v_host_earnings AS
SELECT 
    p.payee_id AS host_id,
    u.full_name AS host_name,
    COUNT(p.id) AS payout_events,
    SUM(p.gross_amount) AS gross_revenue,
    SUM(p.commission_deducted) AS commission_paid,
    SUM(p.net_amount) AS net_payouts
FROM payouts p
JOIN users u ON p.payee_id = u.id
WHERE p.payee_type = 'HOST' AND p.status = 'RELEASED'
GROUP BY p.payee_id, u.full_name;

CREATE VIEW v_booking_summary AS
SELECT 
    status,
    payment_status,
    COUNT(id) AS total_bookings,
    SUM(total_price) AS total_revenue
FROM bookings
GROUP BY status, payment_status;

-- ==============================================================================
-- 8. CREATE STORED PROCEDURES
-- ==============================================================================

DELIMITER //

CREATE PROCEDURE sp_generate_booking_ref(OUT new_ref VARCHAR(50))
BEGIN
    DECLARE today VARCHAR(8);
    DECLARE rand_num INT;
    SET today = DATE_FORMAT(NOW(), '%Y%m%d');
    SET rand_num = FLOOR(RAND() * 900000) + 100000;
    SET new_ref = CONCAT('BK-', today, '-', rand_num);
END //

CREATE PROCEDURE sp_expire_pending_bookings()
BEGIN
    UPDATE bookings 
    SET status = 'EXPIRED' 
    WHERE status = 'PENDING' AND expires_at < NOW();
END //

DELIMITER ;
