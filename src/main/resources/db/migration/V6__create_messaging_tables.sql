-- ============================================================================
-- V6__create_messaging_tables.sql
-- Trip Host Chat — Messages + Traveler-Host Link tables
-- ============================================================================

-- ============================================================================
-- DOMAIN 10: MESSAGING (2 tables)
-- ============================================================================

CREATE TABLE messages (
    message_id          BIGINT          AUTO_INCREMENT,
    sender_id           BIGINT          NOT NULL,
    recipient_id        BIGINT          NOT NULL,
    trip_id             BIGINT          NOT NULL,
    content             VARCHAR(1000)   NOT NULL,
    is_read             BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_messages PRIMARY KEY (message_id),
    CONSTRAINT fk_messages_sender_id FOREIGN KEY (sender_id) REFERENCES users(user_id),
    CONSTRAINT fk_messages_recipient_id FOREIGN KEY (recipient_id) REFERENCES users(user_id),
    CONSTRAINT fk_messages_trip_id FOREIGN KEY (trip_id) REFERENCES trips(trip_id),
    CONSTRAINT chk_messages_content_not_empty CHECK (CHAR_LENGTH(TRIM(content)) > 0)
);

CREATE TABLE traveler_host_links (
    link_id             BIGINT          AUTO_INCREMENT,
    traveler_id         BIGINT          NOT NULL,
    host_id             BIGINT          NOT NULL,
    anonymous_id        VARCHAR(20)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ANONYMOUS',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revealed_at         DATETIME,

    CONSTRAINT pk_traveler_host_links PRIMARY KEY (link_id),
    CONSTRAINT fk_traveler_host_links_traveler_id FOREIGN KEY (traveler_id) REFERENCES users(user_id),
    CONSTRAINT fk_traveler_host_links_host_id FOREIGN KEY (host_id) REFERENCES users(user_id),
    CONSTRAINT uq_traveler_host_links_pair UNIQUE (traveler_id, host_id),
    CONSTRAINT uq_traveler_host_links_anonymous_id UNIQUE (anonymous_id)
);

-- ============================================================================
-- INDEXES: Query performance for messaging
-- ============================================================================

-- Messages: traveler fetching conversation with a host
CREATE INDEX idx_messages_sender_id_recipient_id ON messages(sender_id, recipient_id);

-- Messages: host viewing messages grouped by trip
CREATE INDEX idx_messages_recipient_id_trip_id ON messages(recipient_id, trip_id);

-- Messages: filtering/ordering by trip
CREATE INDEX idx_messages_trip_id ON messages(trip_id);

-- Messages: ordering by creation timestamp
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- Messages: filtering unread messages for host dashboard
CREATE INDEX idx_messages_is_read ON messages(is_read);

-- Traveler-Host Links: host looking up all linked travelers
CREATE INDEX idx_traveler_host_links_host_id ON traveler_host_links(host_id);
