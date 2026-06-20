-- ============================================================================
-- V10: Enhance payment_stages & bookings for dynamic payment schedule policy
-- ============================================================================
-- Adds invoice window tracking, deadline precision, and schedule type
-- to support the Roamaya 3-stage / 2-stage / 1-stage dynamic payment model.
-- ============================================================================

-- 1. Add schedule_type to bookings (THREE_STAGE, TWO_STAGE, FULL_PAYMENT)
ALTER TABLE bookings
    ADD COLUMN schedule_type VARCHAR(20) NULL AFTER payment_status;

-- 2. Add new columns to payment_stages for invoice window management
ALTER TABLE payment_stages
    ADD COLUMN invoice_open_date DATE NULL AFTER due_date,
    ADD COLUMN deadline_at DATETIME NULL AFTER invoice_open_date,
    ADD COLUMN invoice_sent_at DATETIME NULL AFTER deadline_at,
    ADD COLUMN is_immediate BOOLEAN NOT NULL DEFAULT FALSE AFTER invoice_sent_at;

-- 3. Add index on payment_stages for scheduler queries (find stages to invoice)
CREATE INDEX idx_payment_stages_invoice_open_date
    ON payment_stages (invoice_open_date);

CREATE INDEX idx_payment_stages_deadline_at
    ON payment_stages (deadline_at);

-- 4. Add index on bookings for schedule_type filtering
CREATE INDEX idx_bookings_schedule_type
    ON bookings (schedule_type);

-- 5. Update existing stored procedure to use deadline_at for overdue detection
DROP PROCEDURE IF EXISTS sp_mark_overdue_stages;

DELIMITER //

CREATE PROCEDURE sp_mark_overdue_stages()
BEGIN
    -- Mark stages as OVERDUE if deadline_at has passed (precise datetime check)
    UPDATE payment_stages
    SET status = 'OVERDUE'
    WHERE status IN ('PENDING', 'INVOICE_SENT')
      AND deadline_at IS NOT NULL
      AND deadline_at < NOW();

    -- Fallback: also check due_date for stages without deadline_at
    UPDATE payment_stages
    SET status = 'OVERDUE'
    WHERE status IN ('PENDING', 'INVOICE_SENT')
      AND deadline_at IS NULL
      AND due_date IS NOT NULL
      AND due_date < CURDATE();
END //

DELIMITER ;
