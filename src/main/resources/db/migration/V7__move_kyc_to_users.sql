-- V7__move_kyc_to_users.sql

-- 1. Add columns to users table
ALTER TABLE users ADD COLUMN aadhaar_number VARCHAR(12) UNIQUE;
ALTER TABLE users ADD COLUMN pan_number VARCHAR(10) UNIQUE;

-- 2. Migrate existing data from planner_verifications (if any)
UPDATE users u
JOIN planner_verifications pv ON u.id = pv.user_id
SET u.aadhaar_number = pv.aadhaar_number, u.pan_number = pv.pan_number
WHERE pv.aadhaar_number IS NOT NULL OR pv.pan_number IS NOT NULL;

-- 3. Migrate existing data from host_verifications (if any)
UPDATE users u
JOIN host_verifications hv ON u.id = hv.user_id
SET u.aadhaar_number = hv.aadhaar_number, u.pan_number = hv.pan_number
WHERE hv.aadhaar_number IS NOT NULL OR hv.pan_number IS NOT NULL;

-- 4. Drop columns from planner_verifications
ALTER TABLE planner_verifications DROP COLUMN aadhaar_number;
ALTER TABLE planner_verifications DROP COLUMN pan_number;

-- 5. Drop columns from host_verifications
ALTER TABLE host_verifications DROP COLUMN aadhaar_number;
ALTER TABLE host_verifications DROP COLUMN pan_number;
