-- Add aadhaar_number and pan_number columns to host_verifications table
-- These store the numbers submitted during KYC for cross-verification with users table
ALTER TABLE host_verifications ADD COLUMN aadhaar_number VARCHAR(12) AFTER experience_years;
ALTER TABLE host_verifications ADD COLUMN pan_number VARCHAR(10) AFTER aadhaar_number;

-- Backfill existing records from users table
UPDATE host_verifications hv
JOIN users u ON hv.user_id = u.user_id
SET hv.aadhaar_number = u.aadhaar_number,
    hv.pan_number = u.pan_number
WHERE u.aadhaar_number IS NOT NULL OR u.pan_number IS NOT NULL;
