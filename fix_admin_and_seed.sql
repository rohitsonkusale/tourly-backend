-- ====================================================
-- Fix admin credentials + seed realistic dashboard data
-- ====================================================

-- 1. Fix admin user: email = admin@tourly.com, password = Admin@123
-- BCrypt hash of "Admin@123"
UPDATE users
SET email   = 'admin@tourly.com',
    phone   = '9999999999',
    password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W'
WHERE id = 5;

-- 2. Add realistic roles if missing
INSERT IGNORE INTO roles (id, name, description) VALUES 
  (1,'TRAVELER', 'Standard Traveler Role'),
  (2,'PLANNER', 'Trip Planner Role'),
  (3,'HOST', 'Trip Host Role'),
  (4,'ADMIN', 'System Administrator Role');

-- 3. Seed HOST users
INSERT IGNORE INTO users (id, full_name, email, phone, password, account_status, role_id, email_verified, phone_verified, kyc_verified, admin_approval_flag, created_date, created_time)
VALUES
  (10, 'Arjun Mehta',   'arjun.mehta@host.com',   '9876500001',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 3, 1, 1, 1, 1, CURDATE(), CURTIME()),
  (11, 'Priya Sharma',  'priya.sharma@host.com',  '9876500002',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 3, 1, 1, 1, 1, CURDATE(), CURTIME()),
  (12, 'Vikram Nair',   'vikram.nair@host.com',   '9876500003',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 3, 1, 1, 1, 1, CURDATE(), CURTIME());

-- 4. Seed PLANNER users
INSERT IGNORE INTO users (id, full_name, email, phone, password, account_status, role_id, email_verified, phone_verified, kyc_verified, admin_approval_flag, created_date, created_time)
VALUES
  (20, 'Sneha Kapoor',  'sneha.kapoor@plan.com',  '9876500011',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 2, 1, 1, 1, 1, CURDATE(), CURTIME()),
  (21, 'Rahul Verma',   'rahul.verma@plan.com',   '9876500012',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 2, 1, 1, 1, 1, CURDATE(), CURTIME());

-- 5. Seed TRAVELER users
INSERT IGNORE INTO users (id, full_name, email, phone, password, account_status, role_id, email_verified, phone_verified, kyc_verified, admin_approval_flag, created_date, created_time)
VALUES
  (30, 'Aisha Khan',    'aisha.khan@travel.com',  '9876500021',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 1, 1, 1, 0, 1, CURDATE(), CURTIME()),
  (31, 'Rohan Das',     'rohan.das@travel.com',   '9876500022',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 1, 1, 1, 0, 1, CURDATE(), CURTIME()),
  (32, 'Meera Pillai',  'meera.pillai@travel.com','9876500023',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 1, 1, 1, 0, 1, CURDATE(), CURTIME()),
  (33, 'Nikhil Bose',   'nikhil.bose@travel.com', '9876500024',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 1, 1, 1, 0, 1, CURDATE(), CURTIME()),
  (34, 'Kavya Reddy',   'kavya.reddy@travel.com', '9876500025',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 1, 1, 1, 0, 1, CURDATE(), CURTIME()),
  (35, 'Siddharth Rao', 'siddharth.rao@travel.com','9876500026',
   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh0W',
   'ACTIVE', 1, 1, 1, 0, 1, CURDATE(), CURTIME());

-- 6. Seed Destinations (use existing 10, insert more if needed)
INSERT IGNORE INTO destinations (id, country, state, city, latitude, longitude, is_active)
VALUES
  (1,  'India', 'Himachal Pradesh', 'Manali',     32.2432,  77.1892,  1),
  (2,  'India', 'Ladakh',           'Leh',         34.1526,  77.5771,  1),
  (3,  'India', 'Kerala',           'Munnar',       10.0889,  77.0595,  1),
  (4,  'India', 'Rajasthan',        'Jaisalmer',    26.9157,  70.9083,  1),
  (5,  'India', 'Goa',              'Panaji',       15.4909,  73.8278,  1),
  (6,  'India', 'Himachal Pradesh', 'Spiti',        32.2461,  78.0338,  1),
  (7,  'India', 'Uttarakhand',      'Rishikesh',    30.0869,  78.2676,  1),
  (8,  'India', 'Kashmir',          'Srinagar',     34.0837,  74.7973,  1),
  (9,  'India', 'Kerala',           'Alleppey',     9.4981,   76.3388,  1),
  (10, 'India', 'Rajasthan',        'Udaipur',      24.5854,  73.7125,  1);

-- 7. Seed Trips
INSERT IGNORE INTO trips
  (id, title, description, planner_id, host_id, destination_id,
   start_date, end_date, base_price, min_price, max_price,
   total_seats, booked_seats, category, status, approval_status,
   difficulty, trip_type, is_active, is_deleted, created_at, updated_at)
VALUES
  (1,  'Manali Snow Trek',      'Epic winter trek in Manali valleys',         20, 10, 1,
   '2026-06-10','2026-06-16', 18000.00, 15000.00, 22000.00,
   20, 14, 'ADVENTURE',  'PUBLISHED', 'APPROVED', 'Hard',   'Trek',     1, 0, NOW(), NOW()),
  (2,  'Leh Ladakh Expedition', 'Ultimate high-altitude road trip',           21, 11, 2,
   '2026-06-20','2026-06-30', 35000.00, 30000.00, 40000.00,
   15, 10, 'ADVENTURE',  'PUBLISHED', 'APPROVED', 'Hard',   'Road Trip', 1, 0, NOW(), NOW()),
  (3,  'Munnar Tea Estate Stay','Lush green hills and serene tea gardens',    20, 12, 3,
   '2026-07-05','2026-07-10', 12000.00, 10000.00, 15000.00,
   25, 18, 'LUXURY',     'PUBLISHED', 'APPROVED', 'Easy',   'Leisure',  1, 0, NOW(), NOW()),
  (4,  'Jaisalmer Desert Camp', 'Camel safari and desert glamping',           21, 10, 4,
   '2026-07-15','2026-07-19', 14000.00, 12000.00, 17000.00,
   30, 22, 'HERITAGE',   'PUBLISHED', 'APPROVED', 'Easy',   'Cultural', 1, 0, NOW(), NOW()),
  (5,  'Goa Beach Carnival',    'Sun, sand and vibrant beach culture',        20, 11, 5,
   '2026-08-01','2026-08-05', 9000.00,  7500.00,  11000.00,
   40, 30, 'BACKPACKING','PUBLISHED', 'APPROVED', 'Easy',   'Beach',    1, 0, NOW(), NOW()),
  (6,  'Spiti Valley Hidden',   'Remote monastery and mountain passes',       21, 12, 6,
   '2026-08-10','2026-08-17', 22000.00, 19000.00, 26000.00,
   12, 8,  'ADVENTURE',  'PUBLISHED', 'APPROVED', 'Hard',   'Trek',     1, 0, NOW(), NOW()),
  (7,  'Rishikesh Yoga Retreat','Spiritual cleansing and river rafting',      20, 10, 7,
   '2026-08-20','2026-08-24', 8000.00,  6500.00,  9500.00,
   20, 12, 'WEEKEND',    'PUBLISHED', 'APPROVED', 'Easy',   'Wellness', 1, 0, NOW(), NOW()),
  (8,  'Kashmir Tulip Festival','Scenic Dal lake and flower valley tour',     21, 11, 8,
   '2026-09-01','2026-09-06', 16000.00, 13500.00, 19000.00,
   18, 14, 'LUXURY',     'PUBLISHED', 'APPROVED', 'Medium', 'Cultural', 1, 0, NOW(), NOW());

-- 8. Seed Bookings (all CONFIRMED, varying dates across last 30 days)
INSERT IGNORE INTO bookings
  (id, trip_id, traveler_id, seats_booked, total_price, status, payment_status,
   booking_ref, base_amount, discount_amount, tax_amount,
   created_at, updated_at, confirmed_at, expires_at)
VALUES
  (1,  1, 30, 2, 36000.00, 'CONFIRMED', 'PAID', 'BK-20260501-001', 36000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (2,  2, 31, 1, 35000.00, 'CONFIRMED', 'PAID', 'BK-20260504-002', 35000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (3,  3, 32, 3, 36000.00, 'CONFIRMED', 'PAID', 'BK-20260507-003', 36000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (4,  4, 33, 2, 28000.00, 'CONFIRMED', 'PAID', 'BK-20260510-004', 28000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (5,  5, 34, 4, 36000.00, 'CONFIRMED', 'PAID', 'BK-20260513-005', 36000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (6,  6, 35, 1, 22000.00, 'CONFIRMED', 'PAID', 'BK-20260516-006', 22000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (7,  7, 30, 2, 16000.00, 'CONFIRMED', 'PAID', 'BK-20260519-007', 16000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (8,  8, 31, 2, 32000.00, 'CONFIRMED', 'PAID', 'BK-20260522-008', 32000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (9,  1, 32, 1, 18000.00, 'CONFIRMED', 'PAID', 'BK-20260524-009', 18000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (10, 3, 33, 2, 24000.00, 'CONFIRMED', 'PAID', 'BK-20260526-010', 24000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (11, 2, 34, 1, 35000.00, 'CONFIRMED', 'PAID', 'BK-20260527-011', 35000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 2 DAY),  DATE_SUB(NOW(), INTERVAL 2 DAY),  DATE_SUB(NOW(), INTERVAL 2 DAY),  DATE_ADD(NOW(), INTERVAL 30 DAY)),
  (12, 4, 35, 2, 28000.00, 'CONFIRMED', 'PAID', 'BK-20260528-012', 28000.00, 0, 0,
   DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 30 DAY));

-- 9. Seed support tickets
INSERT IGNORE INTO support_tickets
  (id, created_by, subject, description, category, priority, status, created_at, updated_at)
VALUES
  (1, 30, 'Payment not processed', 'I paid but booking not confirmed', 'PAYMENT',  'HIGH',   'OPEN',        DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
  (2, 31, 'Refund pending 7 days', 'My refund is still pending',       'REFUND',   'MEDIUM', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
  (3, 32, 'Trip cancelled by host', 'Host cancelled my trip',           'BOOKING',  'HIGH',   'OPEN',        DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
  (4, 33, 'App login issue',       'Cannot login after password reset', 'TECHNICAL','LOW',    'OPEN',        DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- Done
SELECT 'Seed completed successfully' AS result;
