-- ============================================================================
-- drop_and_rebuild.sql
-- Run this ONCE manually before starting the app with new migrations
-- This drops the entire database and recreates it fresh
-- ============================================================================

-- WARNING: This destroys ALL data. Only use in development.

DROP DATABASE IF EXISTS tourly_db;
CREATE DATABASE tourly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges to tourly_user (adjust if your user is different)
GRANT ALL PRIVILEGES ON tourly_db.* TO 'tourly_user'@'localhost';
FLUSH PRIVILEGES;

USE tourly_db;

-- Now start your Spring Boot app — Flyway will run V1, V2, V3 automatically.
SELECT 'Database dropped and recreated. Start the app to run migrations.' AS status;
