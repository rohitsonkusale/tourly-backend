-- =============================================
-- V16: Add admin_message column to payouts table
-- =============================================

ALTER TABLE payouts ADD COLUMN admin_message TEXT NULL;
