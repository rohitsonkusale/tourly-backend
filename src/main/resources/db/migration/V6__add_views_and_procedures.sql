-- --------------------------------------------------------------------------------
-- V6__add_views_and_procedures.sql
-- Re-adding SQL views and stored procedures after JPA-based schema baseline
-- --------------------------------------------------------------------------------

-- ==============================================================================
-- 1. CREATE VIEWS
-- ==============================================================================

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
-- 2. CREATE STORED PROCEDURES
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
