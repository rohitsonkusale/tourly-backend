# Tourly/Roamaya — Database Naming Standards

> All database objects follow these conventions. No exceptions.

---

## 1. General Rules

- All names: **lowercase**, **snake_case**
- No reserved words as names (avoid `status`, `order`, `type` alone — prefix them: `booking_status`, `sort_order`, `media_type`)
- No generic `id` columns — always `<table_singular>_id`
- Maximum name length: 64 characters (MySQL limit)
- Use singular nouns for entities in PK/FK names (`user_id` not `users_id`)

---

## 2. Tables

| Rule | Convention | Example |
|------|-----------|---------|
| Name | Plural, snake_case | `users`, `trips`, `trip_batches` |
| Join tables | Both entity names, alphabetical | `role_permissions` (role + permission) |
| Child tables | Parent prefix + child noun | `trip_stays`, `trip_stay_images` |

---

## 3. Primary Keys

| Rule | Convention | Example |
|------|-----------|---------|
| Column name | `<singular_table>_id` | `user_id`, `trip_id`, `booking_id` |
| Always first column | — | — |
| Constraint name | `pk_<table>` | `pk_users`, `pk_trips`, `pk_bookings` |
| Type | `BIGINT AUTO_INCREMENT` | — |

### Examples

| Table | PK Column | Constraint |
|-------|-----------|------------|
| `users` | `user_id` | `pk_users` |
| `trips` | `trip_id` | `pk_trips` |
| `bookings` | `booking_id` | `pk_bookings` |
| `payment_stages` | `payment_stage_id` | `pk_payment_stages` |
| `trip_stay_images` | `trip_stay_image_id` | `pk_trip_stay_images` |
| `trip_itinerary_days` | `trip_itinerary_day_id` | `pk_trip_itinerary_days` |

---

## 4. Foreign Keys

### Column Naming

| Scenario | Rule | Example |
|----------|------|---------|
| Single FK to parent | Match parent PK name exactly | `bookings.trip_id` → `trips.trip_id` |
| Multiple FKs to same parent | Semantic prefix/suffix | `trips.planner_id`, `trips.host_id` (both → `users.user_id`) |
| Self-referencing | `parent_<entity>_id` | `categories.parent_category_id` |

### Constraint Naming

Format: `fk_<table>_<column>`

| Table | Column | Constraint |
|-------|--------|------------|
| `trips` | `planner_id` | `fk_trips_planner_id` |
| `trips` | `host_id` | `fk_trips_host_id` |
| `trips` | `destination_id` | `fk_trips_destination_id` |
| `bookings` | `trip_id` | `fk_bookings_trip_id` |
| `bookings` | `traveler_id` | `fk_bookings_traveler_id` |
| `bookings` | `batch_id` | `fk_bookings_batch_id` |
| `payments` | `booking_id` | `fk_payments_booking_id` |
| `payments` | `payment_stage_id` | `fk_payments_payment_stage_id` |
| `refunds` | `payment_id` | `fk_refunds_payment_id` |
| `refunds` | `requested_by` | `fk_refunds_requested_by` |
| `refunds` | `processed_by` | `fk_refunds_processed_by` |

### Multiple FKs to `users` — Full Reference

| Table | Column | Meaning | Constraint |
|-------|--------|---------|------------|
| `trips` | `planner_id` | Who designed | `fk_trips_planner_id` |
| `trips` | `host_id` | Who hosts on-ground | `fk_trips_host_id` |
| `bookings` | `traveler_id` | Who booked | `fk_bookings_traveler_id` |
| `refunds` | `requested_by` | Who asked for refund | `fk_refunds_requested_by` |
| `refunds` | `processed_by` | Admin who processed | `fk_refunds_processed_by` |
| `disputes` | `filed_by` | Who filed dispute | `fk_disputes_filed_by` |
| `disputes` | `against_user_id` | Who it's against | `fk_disputes_against_user_id` |
| `support_tickets` | `created_by` | Who created ticket | `fk_support_tickets_created_by` |
| `support_tickets` | `assigned_to` | Admin assigned | `fk_support_tickets_assigned_to` |
| `host_verifications` | `reviewed_by` | Admin reviewer | `fk_host_verifications_reviewed_by` |
| `audit_logs` | `performed_by` | Who did the action | `fk_audit_logs_performed_by` |

---

## 5. Indexes

### Naming Convention

Format: `idx_<table>_<column(s)>`

For composite indexes: `idx_<table>_<col1>_<col2>`

| Type | Format | Example |
|------|--------|---------|
| Single column | `idx_<table>_<column>` | `idx_users_email` |
| Composite | `idx_<table>_<col1>_<col2>` | `idx_bookings_trip_id_status` |
| Unique index | `uidx_<table>_<column>` | `uidx_users_email` |
| Full-text | `ftidx_<table>_<column>` | `ftidx_trips_title` |

### Standard Indexes for This Project

```sql
-- AUTH
idx_users_email
idx_users_phone
idx_users_role_id
idx_users_account_status
uidx_users_email
uidx_users_phone
uidx_users_google_id

-- TRIPS
idx_trips_planner_id
idx_trips_host_id
idx_trips_destination_id
idx_trips_status
idx_trips_approval_status
idx_trips_start_date
idx_trips_is_active_is_deleted

-- BOOKINGS
idx_bookings_trip_id
idx_bookings_traveler_id
idx_bookings_status
idx_bookings_payment_status
idx_bookings_created_at
uidx_bookings_booking_ref

-- PAYMENTS
idx_payments_booking_id
idx_payments_payment_stage_id
idx_payments_status
uidx_payments_razorpay_order_id
uidx_payments_razorpay_payment_id

-- PAYMENT STAGES
idx_payment_stages_booking_id
idx_payment_stages_status
idx_payment_stages_due_date

-- PAYOUTS
idx_payouts_booking_id
idx_payouts_payee_id
idx_payouts_status

-- COMMISSIONS
idx_commissions_booking_id
idx_commissions_payment_id
idx_commissions_status

-- REFUNDS
idx_refunds_booking_id
idx_refunds_payment_id
idx_refunds_status

-- REVIEWS
idx_reviews_booking_id
idx_reviews_reviewer_id
idx_reviews_target_type_target_id

-- NOTIFICATIONS
idx_notifications_user_id
idx_notifications_read_at
idx_notifications_created_at

-- AUDIT LOGS
idx_audit_logs_performed_by
idx_audit_logs_category
idx_audit_logs_created_at

-- SUPPORT
idx_support_tickets_created_by
idx_support_tickets_status

-- COUPONS
uidx_coupons_code
idx_coupons_status
```

---

## 6. Unique Constraints

Format: `uq_<table>_<column(s)>`

| Table | Columns | Constraint |
|-------|---------|------------|
| `users` | `email` | `uq_users_email` |
| `users` | `phone` | `uq_users_phone` |
| `bookings` | `booking_ref` | `uq_bookings_booking_ref` |
| `trip_batches` | `trip_id, start_date, end_date` | `uq_trip_batches_trip_dates` |
| `payment_stages` | `booking_id, stage_number` | `uq_payment_stages_booking_stage` |
| `trip_badges` | `trip_id, badge_name` | `uq_trip_badges_trip_badge` |
| `coupon_usages` | `booking_id, coupon_id` | `uq_coupon_usages_booking_coupon` |
| `role_permissions` | `role_id, permission_id` | `uq_role_permissions_role_perm` |
| `coupons` | `code` | `uq_coupons_code` |

---

## 7. Check Constraints

Format: `chk_<table>_<rule_description>`

| Table | Rule | Constraint |
|-------|------|------------|
| `reviews` | `rating BETWEEN 1 AND 5` | `chk_reviews_rating_range` |
| `payment_stages` | `percentage BETWEEN 0 AND 100` | `chk_payment_stages_percentage` |
| `payment_stages` | `amount > 0` | `chk_payment_stages_positive_amount` |
| `bookings` | `seats_booked > 0` | `chk_bookings_positive_seats` |
| `bookings` | `total_price >= 0` | `chk_bookings_non_negative_price` |
| `coupons` | `discount_value > 0` | `chk_coupons_positive_discount` |
| `coupons` | `valid_from < valid_to` | `chk_coupons_valid_date_range` |

---

## 8. Views

Format: `vw_<descriptive_name>`

| View | Purpose |
|------|---------|
| `vw_platform_earnings_monthly` | Monthly commission/revenue summary |
| `vw_planner_earnings` | Per-planner net payouts |
| `vw_host_earnings` | Per-host net payouts |
| `vw_booking_summary` | Booking count/revenue by status |
| `vw_overdue_stages` | Stages past due date and unpaid |
| `vw_trip_availability` | Trips with remaining seats |

### Example

```sql
CREATE VIEW vw_platform_earnings_monthly AS
SELECT
    YEAR(c.calculated_at) AS revenue_year,
    MONTH(c.calculated_at) AS revenue_month,
    COUNT(c.commission_id) AS total_events,
    SUM(c.payment_amount) AS total_payment_volume,
    SUM(c.commission_amount) AS gross_commission,
    SUM(c.tax_on_commission) AS commission_tax,
    SUM(c.net_platform_earning) AS net_earning
FROM commissions c
WHERE c.status = 'REALIZED'
GROUP BY YEAR(c.calculated_at), MONTH(c.calculated_at);
```

---

## 9. Stored Procedures

Format: `sp_<action>_<entity>`

| Procedure | Purpose |
|-----------|---------|
| `sp_generate_booking_ref` | Create unique booking reference |
| `sp_expire_pending_bookings` | Mark stale PENDING bookings as EXPIRED |
| `sp_mark_overdue_stages` | Update PENDING stages past due_date to OVERDUE |
| `sp_cancel_overdue_bookings` | Cancel bookings with stages overdue beyond grace period |
| `sp_calculate_commission` | Calculate platform commission for a payment |
| `sp_recalculate_dynamic_price` | Update trip current_price based on demand |

### Example

```sql
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
    UPDATE payment_stages
    SET status = 'OVERDUE'
    WHERE status = 'PENDING'
      AND due_date < CURDATE();
END //

DELIMITER ;
```

---

## 10. Functions

Format: `fn_<what_it_returns>`

| Function | Purpose | Returns |
|----------|---------|---------|
| `fn_booking_ref()` | Generate unique booking reference | VARCHAR |
| `fn_seats_available(trip_id)` | Calculate remaining seats | INT |
| `fn_total_paid(booking_id)` | Sum of paid payments for a booking | DECIMAL |
| `fn_commission_rate(booking_amount)` | Get commission tier rate | DECIMAL |
| `fn_is_stage_overdue(stage_id)` | Check if stage is past due | BOOLEAN |

### Example

```sql
DELIMITER //

CREATE FUNCTION fn_booking_ref()
RETURNS VARCHAR(50) DETERMINISTIC
BEGIN
    DECLARE new_ref VARCHAR(50);
    DECLARE today_str VARCHAR(8);
    DECLARE rand_num INT;

    SET today_str = DATE_FORMAT(NOW(), '%Y%m%d');
    SET rand_num = FLOOR(RAND() * 900000) + 100000;
    SET new_ref = CONCAT('BK-', today_str, '-', rand_num);

    RETURN new_ref;
END //

CREATE FUNCTION fn_seats_available(p_trip_id BIGINT)
RETURNS INT DETERMINISTIC READS SQL DATA
BEGIN
    DECLARE total INT;
    DECLARE booked INT;

    SELECT total_seats, booked_seats
    INTO total, booked
    FROM trips WHERE trip_id = p_trip_id;

    RETURN COALESCE(total - booked, 0);
END //

DELIMITER ;
```

---

## 11. Triggers

Format: `trg_<table>_<timing>_<action>`

Where timing = `before` or `after`, action = `insert`, `update`, `delete`

| Trigger | Purpose |
|---------|---------|
| `trg_bookings_before_insert` | Auto-set booking_ref, created_at |
| `trg_payments_after_update` | Sync booking.amount_paid when payment status changes |
| `trg_payment_stages_after_update` | Send notification when stage becomes OVERDUE |

### Example

```sql
DELIMITER //

CREATE TRIGGER trg_bookings_before_insert
BEFORE INSERT ON bookings
FOR EACH ROW
BEGIN
    IF NEW.booking_ref IS NULL THEN
        SET NEW.booking_ref = fn_booking_ref();
    END IF;
    SET NEW.created_at = NOW();
    SET NEW.updated_at = NOW();
END //

DELIMITER ;
```

---

## 12. Events (Scheduled Jobs)

Format: `evt_<action>_<frequency>`

| Event | Schedule | Purpose |
|-------|----------|---------|
| `evt_expire_bookings_5min` | Every 5 minutes | Expire stale pending bookings |
| `evt_mark_overdue_daily` | Daily at 00:30 | Mark unpaid stages as OVERDUE |
| `evt_cancel_overdue_weekly` | Weekly on Monday | Cancel bookings with stages overdue > 7 days |
| `evt_cleanup_webhook_logs_monthly` | Monthly | Delete processed webhook logs > 90 days |

### Example

```sql
CREATE EVENT evt_expire_bookings_5min
ON SCHEDULE EVERY 5 MINUTE
DO
    CALL sp_expire_pending_bookings();

CREATE EVENT evt_mark_overdue_daily
ON SCHEDULE EVERY 1 DAY
STARTS TIMESTAMP(CURRENT_DATE, '00:30:00')
DO
    CALL sp_mark_overdue_stages();
```

---

## 13. Sequences (MySQL AUTO_INCREMENT)

MySQL uses `AUTO_INCREMENT` natively, not explicit sequences. No naming needed.

If you ever move to PostgreSQL:
Format: `seq_<table>_<column>`

Example: `seq_users_user_id`, `seq_trips_trip_id`

---

## 14. Column Ordering Template

Every table follows this exact order:

```
1. Primary Key           → trip_id
2. Foreign Keys          → planner_id, host_id, destination_id
3. Mandatory business    → title, base_price, total_seats
4. Optional business     → description, difficulty, badges
5. Status/Enum fields    → status, approval_status, is_active
6. Computed/Derived      → current_price, booked_seats
7. Audit timestamps      → created_at, updated_at, deleted_at
8. Audit actors          → created_by, updated_by
```

---

## 15. Naming Quick Reference Card

| Object | Format | Example |
|--------|--------|---------|
| Table | `plural_snake_case` | `payment_stages` |
| Primary Key | `<singular>_id` | `payment_stage_id` |
| PK Constraint | `pk_<table>` | `pk_payment_stages` |
| Foreign Key Col | `<parent_singular>_id` or semantic | `booking_id`, `planner_id` |
| FK Constraint | `fk_<table>_<column>` | `fk_payments_booking_id` |
| Unique Constraint | `uq_<table>_<columns>` | `uq_trip_batches_trip_dates` |
| Check Constraint | `chk_<table>_<rule>` | `chk_reviews_rating_range` |
| Index | `idx_<table>_<column(s)>` | `idx_bookings_trip_id_status` |
| Unique Index | `uidx_<table>_<column>` | `uidx_users_email` |
| View | `vw_<description>` | `vw_platform_earnings_monthly` |
| Stored Procedure | `sp_<action>_<entity>` | `sp_expire_pending_bookings` |
| Function | `fn_<what_it_returns>` | `fn_seats_available` |
| Trigger | `trg_<table>_<timing>_<action>` | `trg_bookings_before_insert` |
| Event | `evt_<action>_<frequency>` | `evt_expire_bookings_5min` |

---

## 16. Anti-Patterns (Never Do This)

| ❌ Bad | ✅ Good | Why |
|--------|---------|-----|
| `id` | `user_id` | Generic, ambiguous in joins |
| `trip_user_id` | `planner_id` | Redundant prefix when context is clear |
| `FK_Trips_UserId` | `fk_trips_planner_id` | Not snake_case, mixed case |
| `index1` | `idx_bookings_status` | Meaningless name |
| `proc_1` | `sp_expire_pending_bookings` | Meaningless name |
| `v1` | `vw_platform_earnings_monthly` | Meaningless name |
| `status` (alone) | `booking_status` or keep `status` with table context | Reserved word risk |
| `type` (alone) | `media_type`, `refund_type` | Reserved word risk |
