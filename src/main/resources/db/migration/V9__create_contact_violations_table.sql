-- =============================================
-- V9: Create contact_violations table
-- Tracks contact-sharing attempts for the 3-strike system and audit logging
-- =============================================

CREATE TABLE contact_violations (
    violation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message_id BIGINT,
    original_content VARCHAR(1000) NOT NULL,
    masked_content VARCHAR(1000) NOT NULL,
    detected_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_contact_violations_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_contact_violations_message
        FOREIGN KEY (message_id) REFERENCES messages(message_id)
        ON DELETE SET NULL
);

-- Index for fast lookups by user + time window (3-strike check)
CREATE INDEX idx_contact_violations_user_created
    ON contact_violations (user_id, created_at DESC);
