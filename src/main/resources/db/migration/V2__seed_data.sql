-- ============================================================================
-- V2__seed_data.sql
-- Tourly/Roamaya — Seed data for development/testing
-- ============================================================================

-- ============================================================================
-- 1. ROLES
-- ============================================================================

INSERT INTO roles (role_id, name, description) VALUES
    (1, 'TRAVELER', 'Standard Traveler Role'),
    (2, 'PLANNER',  'Trip Planner Role'),
    (3, 'HOST',     'Trip Host Role'),
    (4, 'ADMIN',    'System Administrator Role');

-- ============================================================================
-- 2. PERMISSIONS
-- ============================================================================

INSERT INTO permissions (permission_id, permission_name, description) VALUES
    (1,  'USER_READ',           'View user profiles'),
    (2,  'USER_WRITE',          'Edit user profiles'),
    (3,  'USER_DELETE',         'Delete/ban users'),
    (4,  'TRIP_CREATE',         'Create trips'),
    (5,  'TRIP_EDIT',           'Edit own trips'),
    (6,  'TRIP_DELETE',         'Delete trips'),
    (7,  'TRIP_APPROVE',        'Approve/reject trips'),
    (8,  'BOOKING_CREATE',      'Create bookings'),
    (9,  'BOOKING_CANCEL',      'Cancel bookings'),
    (10, 'BOOKING_VIEW_ALL',    'View all bookings (admin)'),
    (11, 'PAYMENT_VIEW',        'View payment details'),
    (12, 'PAYMENT_REFUND',      'Process refunds'),
    (13, 'PAYOUT_MANAGE',       'Manage payouts'),
    (14, 'REVIEW_MODERATE',     'Moderate reviews'),
    (15, 'DISPUTE_MANAGE',      'Manage disputes'),
    (16, 'COUPON_MANAGE',       'Create/edit coupons'),
    (17, 'SETTINGS_MANAGE',     'Manage platform settings'),
    (18, 'VERIFICATION_REVIEW', 'Review KYC verifications'),
    (19, 'ANNOUNCEMENT_MANAGE', 'Manage announcements'),
    (20, 'AUDIT_VIEW',          'View audit logs');

-- ============================================================================
-- 3. ROLE-PERMISSION ASSIGNMENTS
-- ============================================================================

-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 6), (4, 7), (4, 8), (4, 9), (4, 10),
    (4, 11), (4, 12), (4, 13), (4, 14), (4, 15), (4, 16), (4, 17), (4, 18), (4, 19), (4, 20);

-- HOST: create/edit trips, view bookings on own trips
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (3, 1), (3, 4), (3, 5), (3, 11);

-- PLANNER: create/edit trips, view bookings on own trips
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (2, 1), (2, 4), (2, 5), (2, 11);

-- TRAVELER: book trips, cancel own bookings, view payments
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (1, 1), (1, 8), (1, 9), (1, 11);

-- ============================================================================
-- 4. USERS
-- ============================================================================

-- Admin (email: admin, password: Admin@123)
-- To change password to SuperAdmin@2026, use the app's password reset or update via API
INSERT INTO users (user_id, role_id, full_name, email, phone, password, account_status,
    email_verified, phone_verified, kyc_verified, admin_approval_flag, created_at)
VALUES (1, 4, 'Super Admin', 'admin', '9999999999',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
    'ACTIVE', TRUE, TRUE, TRUE, 'Y', NOW());

-- Hosts (password: Admin@123)
INSERT INTO users (user_id, role_id, full_name, email, phone, password, account_status,
    email_verified, phone_verified, kyc_verified, admin_approval_flag, created_at)
VALUES
    (10, 3, 'Arjun Mehta',   'arjun.mehta@host.com',   '9876500001',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, TRUE, 'Y', NOW()),
    (11, 3, 'Priya Sharma',  'priya.sharma@host.com',  '9876500002',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, TRUE, 'Y', NOW()),
    (12, 3, 'Vikram Nair',   'vikram.nair@host.com',   '9876500003',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, TRUE, 'Y', NOW());

-- Planners (password: Admin@123)
INSERT INTO users (user_id, role_id, full_name, email, phone, password, account_status,
    email_verified, phone_verified, kyc_verified, admin_approval_flag, created_at)
VALUES
    (20, 2, 'Sneha Kapoor',  'sneha.kapoor@plan.com',  '9876500011',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, TRUE, 'Y', NOW()),
    (21, 2, 'Rahul Verma',   'rahul.verma@plan.com',   '9876500012',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, TRUE, 'Y', NOW());

-- Travelers (password: Admin@123)
INSERT INTO users (user_id, role_id, full_name, email, phone, password, account_status,
    email_verified, phone_verified, kyc_verified, admin_approval_flag, created_at)
VALUES
    (30, 1, 'Aisha Khan',    'aisha.khan@travel.com',  '9876500021',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, FALSE, 'Y', NOW()),
    (31, 1, 'Rohan Das',     'rohan.das@travel.com',   '9876500022',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, FALSE, 'Y', NOW()),
    (32, 1, 'Meera Pillai',  'meera.pillai@travel.com','9876500023',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, FALSE, 'Y', NOW()),
    (33, 1, 'Nikhil Bose',   'nikhil.bose@travel.com', '9876500024',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, FALSE, 'Y', NOW()),
    (34, 1, 'Kavya Reddy',   'kavya.reddy@travel.com', '9876500025',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, FALSE, 'Y', NOW()),
    (35, 1, 'Siddharth Rao', 'siddharth.rao@travel.com','9876500026',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
     'ACTIVE', TRUE, TRUE, FALSE, 'Y', NOW());

-- ============================================================================
-- 5. DESTINATIONS
-- ============================================================================

INSERT INTO destinations (destination_id, country, state, city, latitude, longitude, is_active) VALUES
    (1,  'India', 'Himachal Pradesh', 'Manali',     32.2432,  77.1892,  TRUE),
    (2,  'India', 'Ladakh',           'Leh',        34.1526,  77.5771,  TRUE),
    (3,  'India', 'Kerala',           'Munnar',     10.0889,  77.0595,  TRUE),
    (4,  'India', 'Rajasthan',        'Jaisalmer',  26.9157,  70.9083,  TRUE),
    (5,  'India', 'Goa',              'Panaji',     15.4909,  73.8278,  TRUE),
    (6,  'India', 'Himachal Pradesh', 'Spiti',      32.2461,  78.0338,  TRUE),
    (7,  'India', 'Uttarakhand',      'Rishikesh',  30.0869,  78.2676,  TRUE),
    (8,  'India', 'Kashmir',          'Srinagar',   34.0837,  74.7973,  TRUE),
    (9,  'India', 'Kerala',           'Alleppey',   9.4981,   76.3388,  TRUE),
    (10, 'India', 'Rajasthan',        'Udaipur',    24.5854,  73.7125,  TRUE);

-- ============================================================================
-- 6. TRIPS
-- ============================================================================

INSERT INTO trips (trip_id, planner_id, host_id, destination_id, title, description,
    base_price, min_price, max_price, current_price, total_seats, booked_seats,
    start_date, end_date, category, status, approval_status,
    difficulty, trip_type, is_active, is_deleted, created_at, updated_at)
VALUES
    (1, 20, 10, 1, 'Manali Snow Trek',
     'Epic winter trek in Manali valleys',
     18000.00, 15000.00, 22000.00, 18000.00, 20, 14,
     '2026-07-10', '2026-07-16', 'ADVENTURE', 'PUBLISHED', 'APPROVED',
     'Hard', 'Trek', TRUE, FALSE, NOW(), NOW()),

    (2, 21, 11, 2, 'Leh Ladakh Expedition',
     'Ultimate high-altitude road trip',
     35000.00, 30000.00, 40000.00, 35000.00, 15, 10,
     '2026-07-20', '2026-07-30', 'ADVENTURE', 'PUBLISHED', 'APPROVED',
     'Hard', 'Road Trip', TRUE, FALSE, NOW(), NOW()),

    (3, 20, 12, 3, 'Munnar Tea Estate Stay',
     'Lush green hills and serene tea gardens',
     12000.00, 10000.00, 15000.00, 12000.00, 25, 18,
     '2026-08-05', '2026-08-10', 'LUXURY', 'PUBLISHED', 'APPROVED',
     'Easy', 'Leisure', TRUE, FALSE, NOW(), NOW()),

    (4, 21, 10, 4, 'Jaisalmer Desert Camp',
     'Camel safari and desert glamping',
     14000.00, 12000.00, 17000.00, 14000.00, 30, 22,
     '2026-08-15', '2026-08-19', 'HERITAGE', 'PUBLISHED', 'APPROVED',
     'Easy', 'Cultural', TRUE, FALSE, NOW(), NOW()),

    (5, 20, 11, 5, 'Goa Beach Carnival',
     'Sun, sand and vibrant beach culture',
     9000.00, 7500.00, 11000.00, 9000.00, 40, 30,
     '2026-09-01', '2026-09-05', 'BACKPACKING', 'PUBLISHED', 'APPROVED',
     'Easy', 'Beach', TRUE, FALSE, NOW(), NOW()),

    (6, 21, 12, 6, 'Spiti Valley Hidden',
     'Remote monastery and mountain passes',
     22000.00, 19000.00, 26000.00, 22000.00, 12, 8,
     '2026-09-10', '2026-09-17', 'ADVENTURE', 'PUBLISHED', 'APPROVED',
     'Hard', 'Trek', TRUE, FALSE, NOW(), NOW()),

    (7, 20, 10, 7, 'Rishikesh Yoga Retreat',
     'Spiritual cleansing and river rafting',
     8000.00, 6500.00, 9500.00, 8000.00, 20, 12,
     '2026-09-20', '2026-09-24', 'WEEKEND', 'PUBLISHED', 'APPROVED',
     'Easy', 'Wellness', TRUE, FALSE, NOW(), NOW()),

    (8, 21, 11, 8, 'Kashmir Tulip Festival',
     'Scenic Dal lake and flower valley tour',
     16000.00, 13500.00, 19000.00, 16000.00, 18, 14,
     '2026-10-01', '2026-10-06', 'LUXURY', 'PUBLISHED', 'APPROVED',
     'Medium', 'Cultural', TRUE, FALSE, NOW(), NOW());

-- ============================================================================
-- 7. SAMPLE BOOKINGS (all CONFIRMED with staged payments)
-- ============================================================================

INSERT INTO bookings (booking_id, trip_id, traveler_id, booking_ref, seats_booked,
    base_amount, discount_amount, tax_amount, total_price, amount_paid, amount_pending,
    status, payment_status, expires_at, confirmed_at, created_at, updated_at)
VALUES
    (1, 1, 30, 'BK-20260601-100001', 2, 36000.00, 0, 0, 36000.00, 36000.00, 0,
     'CONFIRMED', 'FULLY_PAID', DATE_ADD(NOW(), INTERVAL 30 DAY),
     DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY), NOW()),

    (2, 2, 31, 'BK-20260604-100002', 1, 35000.00, 0, 0, 35000.00, 35000.00, 0,
     'CONFIRMED', 'FULLY_PAID', DATE_ADD(NOW(), INTERVAL 30 DAY),
     DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), NOW()),

    (3, 3, 32, 'BK-20260607-100003', 3, 36000.00, 0, 0, 36000.00, 36000.00, 0,
     'CONFIRMED', 'FULLY_PAID', DATE_ADD(NOW(), INTERVAL 30 DAY),
     DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY), NOW()),

    (4, 4, 33, 'BK-20260610-100004', 2, 28000.00, 0, 0, 28000.00, 10800.00, 17200.00,
     'CONFIRMED', 'PARTIALLY_PAID', DATE_ADD(NOW(), INTERVAL 30 DAY),
     DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), NOW()),

    (5, 5, 34, 'BK-20260613-100005', 4, 36000.00, 0, 0, 36000.00, 36000.00, 0,
     'CONFIRMED', 'FULLY_PAID', DATE_ADD(NOW(), INTERVAL 30 DAY),
     DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), NOW()),

    (6, 6, 35, 'BK-20260616-100006', 1, 22000.00, 0, 0, 22000.00, 6600.00, 15400.00,
     'CONFIRMED', 'PARTIALLY_PAID', DATE_ADD(NOW(), INTERVAL 30 DAY),
     DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), NOW());

-- ============================================================================
-- 8. PAYMENT STAGES (3 stages per booking)
-- ============================================================================

-- Booking 1: Fully paid (all 3 stages)
INSERT INTO payment_stages (payment_stage_id, booking_id, stage_number, label, amount, percentage, status, due_date, paid_at) VALUES
    (1,  1, 1, 'Token Advance', 10800.00, 30.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY)),
    (2,  1, 2, 'Mid Payment',   14400.00, 40.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
    (3,  1, 3, 'Final Payment', 10800.00, 30.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Booking 2: Fully paid
INSERT INTO payment_stages (payment_stage_id, booking_id, stage_number, label, amount, percentage, status, due_date, paid_at) VALUES
    (4,  2, 1, 'Token Advance', 10500.00, 30.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
    (5,  2, 2, 'Mid Payment',   14000.00, 40.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
    (6,  2, 3, 'Final Payment', 10500.00, 30.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 4 DAY));

-- Booking 3: Fully paid
INSERT INTO payment_stages (payment_stage_id, booking_id, stage_number, label, amount, percentage, status, due_date, paid_at) VALUES
    (7,  3, 1, 'Token Advance', 10800.00, 30.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY)),
    (8,  3, 2, 'Mid Payment',   14400.00, 40.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)),
    (9,  3, 3, 'Final Payment', 10800.00, 30.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 3 DAY),  DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Booking 4: Partially paid (stage 1 done, stage 2 & 3 pending)
INSERT INTO payment_stages (payment_stage_id, booking_id, stage_number, label, amount, percentage, status, due_date, paid_at) VALUES
    (10, 4, 1, 'Token Advance', 8400.00,  30.00, 'PAID',    DATE_SUB(CURDATE(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
    (11, 4, 2, 'Mid Payment',   11200.00, 40.00, 'PENDING', DATE_ADD(CURDATE(), INTERVAL 10 DAY), NULL),
    (12, 4, 3, 'Final Payment', 8400.00,  30.00, 'PENDING', DATE_ADD(CURDATE(), INTERVAL 20 DAY), NULL);

-- Booking 5: Fully paid
INSERT INTO payment_stages (payment_stage_id, booking_id, stage_number, label, amount, percentage, status, due_date, paid_at) VALUES
    (13, 5, 1, 'Token Advance', 10800.00, 30.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),
    (14, 5, 2, 'Mid Payment',   14400.00, 40.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 8 DAY),  DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (15, 5, 3, 'Final Payment', 10800.00, 30.00, 'PAID', DATE_SUB(CURDATE(), INTERVAL 2 DAY),  DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Booking 6: Partially paid (stage 1 done)
INSERT INTO payment_stages (payment_stage_id, booking_id, stage_number, label, amount, percentage, status, due_date, paid_at) VALUES
    (16, 6, 1, 'Token Advance', 6600.00,  30.00, 'PAID',    DATE_SUB(CURDATE(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),
    (17, 6, 2, 'Mid Payment',   8800.00,  40.00, 'PENDING', DATE_ADD(CURDATE(), INTERVAL 15 DAY), NULL),
    (18, 6, 3, 'Final Payment', 6600.00,  30.00, 'PENDING', DATE_ADD(CURDATE(), INTERVAL 25 DAY), NULL);

-- ============================================================================
-- 9. PAYMENTS (one per paid stage)
-- ============================================================================

-- Booking 1 payments
INSERT INTO payments (payment_id, booking_id, payment_stage_id, razorpay_order_id, razorpay_payment_id, amount, status, currency, payment_method, attempt_number, paid_at) VALUES
    (1,  1, 1,  'order_bk1_stg1', 'pay_bk1_stg1', 10800.00, 'PAID', 'INR', 'UPI',  1, DATE_SUB(NOW(), INTERVAL 28 DAY)),
    (2,  1, 2,  'order_bk1_stg2', 'pay_bk1_stg2', 14400.00, 'PAID', 'INR', 'CARD', 1, DATE_SUB(NOW(), INTERVAL 18 DAY)),
    (3,  1, 3,  'order_bk1_stg3', 'pay_bk1_stg3', 10800.00, 'PAID', 'INR', 'UPI',  1, DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Booking 2 payments
INSERT INTO payments (payment_id, booking_id, payment_stage_id, razorpay_order_id, razorpay_payment_id, amount, status, currency, payment_method, attempt_number, paid_at) VALUES
    (4,  2, 4,  'order_bk2_stg1', 'pay_bk2_stg1', 10500.00, 'PAID', 'INR', 'UPI',       1, DATE_SUB(NOW(), INTERVAL 25 DAY)),
    (5,  2, 5,  'order_bk2_stg2', 'pay_bk2_stg2', 14000.00, 'PAID', 'INR', 'NETBANKING',1, DATE_SUB(NOW(), INTERVAL 14 DAY)),
    (6,  2, 6,  'order_bk2_stg3', 'pay_bk2_stg3', 10500.00, 'PAID', 'INR', 'CARD',      1, DATE_SUB(NOW(), INTERVAL 4 DAY));

-- Booking 3 payments
INSERT INTO payments (payment_id, booking_id, payment_stage_id, razorpay_order_id, razorpay_payment_id, amount, status, currency, payment_method, attempt_number, paid_at) VALUES
    (7,  3, 7,  'order_bk3_stg1', 'pay_bk3_stg1', 10800.00, 'PAID', 'INR', 'CARD', 1, DATE_SUB(NOW(), INTERVAL 22 DAY)),
    (8,  3, 8,  'order_bk3_stg2', 'pay_bk3_stg2', 14400.00, 'PAID', 'INR', 'UPI',  1, DATE_SUB(NOW(), INTERVAL 11 DAY)),
    (9,  3, 9,  'order_bk3_stg3', 'pay_bk3_stg3', 10800.00, 'PAID', 'INR', 'UPI',  1, DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Booking 4 payments (only stage 1 paid)
INSERT INTO payments (payment_id, booking_id, payment_stage_id, razorpay_order_id, razorpay_payment_id, amount, status, currency, payment_method, attempt_number, paid_at) VALUES
    (10, 4, 10, 'order_bk4_stg1', 'pay_bk4_stg1', 8400.00,  'PAID', 'INR', 'UPI', 1, DATE_SUB(NOW(), INTERVAL 19 DAY));

-- Booking 5 payments
INSERT INTO payments (payment_id, booking_id, payment_stage_id, razorpay_order_id, razorpay_payment_id, amount, status, currency, payment_method, attempt_number, paid_at) VALUES
    (11, 5, 13, 'order_bk5_stg1', 'pay_bk5_stg1', 10800.00, 'PAID', 'INR', 'UPI',  1, DATE_SUB(NOW(), INTERVAL 16 DAY)),
    (12, 5, 14, 'order_bk5_stg2', 'pay_bk5_stg2', 14400.00, 'PAID', 'INR', 'CARD', 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
    (13, 5, 15, 'order_bk5_stg3', 'pay_bk5_stg3', 10800.00, 'PAID', 'INR', 'UPI',  1, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Booking 6 payments (only stage 1 paid)
INSERT INTO payments (payment_id, booking_id, payment_stage_id, razorpay_order_id, razorpay_payment_id, amount, status, currency, payment_method, attempt_number, paid_at) VALUES
    (14, 6, 16, 'order_bk6_stg1', 'pay_bk6_stg1', 6600.00,  'PAID', 'INR', 'UPI', 1, DATE_SUB(NOW(), INTERVAL 13 DAY));

-- ============================================================================
-- 10. SUPPORT TICKETS
-- ============================================================================

INSERT INTO support_tickets (support_ticket_id, created_by, subject, description, category, priority, status) VALUES
    (1, 30, 'Payment not processed',  'I paid but booking not confirmed', 'PAYMENT',   'HIGH',   'OPEN'),
    (2, 31, 'Refund pending 7 days',  'My refund is still pending',       'REFUND',    'MEDIUM', 'IN_PROGRESS'),
    (3, 32, 'Trip cancelled by host', 'Host cancelled my trip',           'BOOKING',   'HIGH',   'OPEN'),
    (4, 33, 'App login issue',        'Cannot login after password reset', 'TECHNICAL', 'LOW',    'OPEN');

-- ============================================================================
-- DONE
-- ============================================================================
SELECT 'V2 Seed data loaded successfully' AS result;
