-- =============================================
-- V8: Add contact_masked column to messages table
-- Tracks whether a message had contact details masked
-- =============================================

ALTER TABLE messages
ADD COLUMN contact_masked BOOLEAN NOT NULL DEFAULT FALSE;
