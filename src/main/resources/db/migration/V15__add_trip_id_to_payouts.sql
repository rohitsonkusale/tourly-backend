-- V15: Add trip_id column to payouts table for trip-level payout requests
-- Also make booking_id nullable (trip-level payouts don't reference a single booking)

ALTER TABLE payouts ADD COLUMN trip_id BIGINT NULL AFTER payout_id;

ALTER TABLE payouts MODIFY COLUMN booking_id BIGINT NULL;

ALTER TABLE payouts ADD CONSTRAINT fk_payouts_trip
    FOREIGN KEY (trip_id) REFERENCES trips(trip_id);
