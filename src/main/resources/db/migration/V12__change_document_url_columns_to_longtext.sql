-- ============================================================
-- V12: Change document URL columns to LONGTEXT to support
--      base64-encoded image storage (was VARCHAR(500))
-- ============================================================

ALTER TABLE host_verifications
    MODIFY COLUMN aadhaar_document_url LONGTEXT NULL,
    MODIFY COLUMN pan_document_url LONGTEXT NULL,
    MODIFY COLUMN selfie_url LONGTEXT NULL;

ALTER TABLE planner_verifications
    MODIFY COLUMN aadhaar_document_url LONGTEXT NULL,
    MODIFY COLUMN pan_document_url LONGTEXT NULL,
    MODIFY COLUMN selfie_url LONGTEXT NULL;
