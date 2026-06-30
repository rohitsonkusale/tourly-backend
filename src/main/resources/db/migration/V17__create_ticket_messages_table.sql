-- =============================================
-- V17: Create ticket_messages table
-- =============================================

CREATE TABLE ticket_messages (
    ticket_message_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    support_ticket_id   BIGINT NOT NULL,
    sender_id           BIGINT NOT NULL,
    content             TEXT NOT NULL,
    is_admin            BOOLEAN DEFAULT FALSE,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ticket_messages_support_ticket_id FOREIGN KEY (support_ticket_id) REFERENCES support_tickets(ticket_id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_messages_sender_id FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_ticket_messages_ticket_id ON ticket_messages(support_ticket_id);
CREATE INDEX idx_ticket_messages_sender_id ON ticket_messages(sender_id);
