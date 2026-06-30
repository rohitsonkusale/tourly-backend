-- =============================================
-- V18: Add tranche column to payouts table
-- Supports the 3-tranche payout model:
-- ADVANCE_1, ADVANCE_2, FINAL
-- =============================================

ALTER TABLE payouts ADD COLUMN tranche VARCHAR(20) NULL AFTER status;
