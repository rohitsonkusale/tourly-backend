-- V13: Add optimistic locking version column to bookings table.
-- Prevents race conditions between concurrent cancel/payment operations.

ALTER TABLE bookings ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
