-- V12: Add bank details and manual payment tracking columns to refunds table
-- Bank details: stored for admin to process manual refund transfers
-- Payment proof: admin records UTR/reference after completing manual transfer

ALTER TABLE refunds ADD COLUMN account_holder_name VARCHAR(255) NULL;
ALTER TABLE refunds ADD COLUMN account_number VARCHAR(50) NULL;
ALTER TABLE refunds ADD COLUMN ifsc_code VARCHAR(20) NULL;
ALTER TABLE refunds ADD COLUMN bank_name VARCHAR(255) NULL;
ALTER TABLE refunds ADD COLUMN transaction_reference VARCHAR(100) NULL;
ALTER TABLE refunds ADD COLUMN payment_method VARCHAR(50) NULL;
ALTER TABLE refunds ADD COLUMN paid_on DATETIME NULL;
