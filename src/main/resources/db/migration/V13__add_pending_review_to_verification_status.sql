-- ============================================================
-- V13: Add PENDING_REVIEW to verification_status columns
--      in host_verifications and planner_verifications tables.
--
-- The ApprovalStatus enum was updated to include PENDING_REVIEW
-- but the DB columns were VARCHAR — MySQL was truncating the value
-- because the column length was too short (length=50 is fine),
-- but the @Enumerated(EnumType.STRING) mapping requires the
-- exact string to fit. The real issue is the column was defined
-- as VARCHAR(50) which is sufficient, but let's ensure it.
-- ============================================================

-- Ensure host_verifications.verification_status can store PENDING_REVIEW (14 chars)
ALTER TABLE host_verifications
    MODIFY COLUMN verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';

-- Ensure planner_verifications.verification_status can store PENDING_REVIEW
ALTER TABLE planner_verifications
    MODIFY COLUMN verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING';
