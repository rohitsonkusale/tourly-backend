-- ============================================================
-- V11: Add selfie_url column to host_verifications and
--      planner_verifications tables
-- ============================================================

ALTER TABLE host_verifications
    ADD COLUMN selfie_url VARCHAR(500) NULL;

ALTER TABLE planner_verifications
    ADD COLUMN selfie_url VARCHAR(500) NULL;
