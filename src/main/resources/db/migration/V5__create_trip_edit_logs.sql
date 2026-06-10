-- =============================================
-- V5: Trip Edit Logs table
-- Tracks all field-level changes made to trips
-- =============================================

CREATE TABLE trip_edit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    edited_by BIGINT NOT NULL,
    edit_session_id VARCHAR(36) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    admin_message_context VARCHAR(500),
    edit_number INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_trip_edit_logs_trip_id (trip_id),
    INDEX idx_trip_edit_logs_session (edit_session_id),

    CONSTRAINT fk_trip_edit_logs_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_edit_logs_user FOREIGN KEY (edited_by) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
