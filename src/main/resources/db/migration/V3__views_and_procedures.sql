-- ============================================================================
-- V3__views_and_procedures.sql
-- Tourly/Roamaya — Views and Stored Procedures
-- NOTE: Functions and Events removed due to MySQL binary logging restrictions.
--       These will be handled at the application layer (Java schedulers).
-- ============================================================================

-- ============================================================================
-- 1. VIEWS
-- ============================================================================

CREATE VIEW vw_platform_earnings_monthly AS
SELECT
    YEAR(calculated_at)     AS revenue_year,
    MONTH(calculated_at)    AS revenue_month,
    COUNT(commission_id)    AS total_events,
    SUM(payment_amount)     AS total_payment_volume,
    SUM(commission_amount)  AS gross_commission,
    SUM(tax_on_commission)  AS commission_tax,
    SUM(net_platform_earning) AS net_earning
FROM commissions
WHERE status = 'REALIZED'
GROUP BY YEAR(calculated_at), MONTH(calculated_at);

CREATE VIEW vw_planner_earnings AS
SELECT
    p.payee_id          AS planner_id,
    u.full_name         AS planner_name,
    COUNT(p.payout_id)  AS payout_events,
    SUM(p.gross_amount) AS gross_revenue,
    SUM(p.commission_deducted) AS commission_paid,
    SUM(p.net_amount)   AS net_payouts
FROM payouts p
JOIN users u ON p.payee_id = u.user_id
WHERE p.payee_type = 'PLANNER' AND p.status = 'RELEASED'
GROUP BY p.payee_id, u.full_name;

CREATE VIEW vw_host_earnings AS
SELECT
    p.payee_id          AS host_id,
    u.full_name         AS host_name,
    COUNT(p.payout_id)  AS payout_events,
    SUM(p.gross_amount) AS gross_revenue,
    SUM(p.commission_deducted) AS commission_paid,
    SUM(p.net_amount)   AS net_payouts
FROM payouts p
JOIN users u ON p.payee_id = u.user_id
WHERE p.payee_type = 'HOST' AND p.status = 'RELEASED'
GROUP BY p.payee_id, u.full_name;

CREATE VIEW vw_booking_summary AS
SELECT
    status,
    payment_status,
    COUNT(booking_id)   AS total_bookings,
    SUM(total_price)    AS total_revenue
FROM bookings
GROUP BY status, payment_status;

CREATE VIEW vw_overdue_stages AS
SELECT
    ps.payment_stage_id,
    ps.booking_id,
    b.booking_ref,
    b.traveler_id,
    u.full_name         AS traveler_name,
    u.email             AS traveler_email,
    ps.stage_number,
    ps.label,
    ps.amount,
    ps.due_date,
    DATEDIFF(CURDATE(), ps.due_date) AS days_overdue
FROM payment_stages ps
JOIN bookings b ON ps.booking_id = b.booking_id
JOIN users u ON b.traveler_id = u.user_id
WHERE ps.status IN ('PENDING', 'OVERDUE')
  AND ps.due_date < CURDATE()
  AND b.status NOT IN ('CANCELLED', 'EXPIRED')
ORDER BY ps.due_date ASC;

CREATE VIEW vw_trip_availability AS
SELECT
    t.trip_id,
    t.title,
    d.city              AS destination,
    t.total_seats,
    t.booked_seats,
    (t.total_seats - t.booked_seats) AS available_seats,
    t.current_price,
    t.start_date,
    t.status
FROM trips t
JOIN destinations d ON t.destination_id = d.destination_id
WHERE t.is_active = TRUE
  AND t.is_deleted = FALSE
  AND t.status = 'PUBLISHED';

-- ============================================================================
-- 2. STORED PROCEDURES
-- ============================================================================

DELIMITER //

CREATE PROCEDURE sp_expire_pending_bookings()
BEGIN
    UPDATE bookings
    SET status = 'EXPIRED',
        updated_at = NOW()
    WHERE status = 'PENDING'
      AND expires_at < NOW();
END //

CREATE PROCEDURE sp_mark_overdue_stages()
BEGIN
    UPDATE payment_stages ps
    JOIN bookings b ON ps.booking_id = b.booking_id
    SET ps.status = 'OVERDUE'
    WHERE ps.status = 'PENDING'
      AND ps.due_date < CURDATE()
      AND b.status NOT IN ('CANCELLED', 'EXPIRED');
END //

CREATE PROCEDURE sp_cancel_overdue_bookings()
BEGIN
    UPDATE bookings b
    SET b.status = 'CANCELLED',
        b.cancellation_reason = 'Payment stage overdue beyond grace period',
        b.cancelled_at = NOW(),
        b.updated_at = NOW()
    WHERE b.status IN ('PENDING', 'CONFIRMED')
      AND b.booking_id IN (
          SELECT DISTINCT ps.booking_id
          FROM payment_stages ps
          WHERE ps.status = 'OVERDUE'
            AND DATEDIFF(CURDATE(), ps.due_date) > 7
      );
END //

DELIMITER ;
