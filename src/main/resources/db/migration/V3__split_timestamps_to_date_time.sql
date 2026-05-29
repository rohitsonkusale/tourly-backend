-- =============================================
-- V3: Split timestamp columns into separate date and time
-- =============================================

-- 1. Add new date/time columns
ALTER TABLE users ADD COLUMN last_login_time TIME NULL;
ALTER TABLE users ADD COLUMN created_date DATE NULL;
ALTER TABLE users ADD COLUMN created_time TIME NULL;
ALTER TABLE users ADD COLUMN updated_date DATE NULL;
ALTER TABLE users ADD COLUMN updated_time TIME NULL;
ALTER TABLE users ADD COLUMN deleted_date DATE NULL;
ALTER TABLE users ADD COLUMN deleted_time TIME NULL;

-- 2. Migrate existing data from old columns to new columns
UPDATE users SET created_date = DATE(created_at), created_time = TIME(created_at) WHERE created_at IS NOT NULL;
UPDATE users SET updated_date = DATE(updated_at), updated_time = TIME(updated_at) WHERE updated_at IS NOT NULL;
UPDATE users SET deleted_date = DATE(deleted_at), deleted_time = TIME(deleted_at) WHERE deleted_at IS NOT NULL;
UPDATE users SET last_login_time = TIME(last_login) WHERE last_login IS NOT NULL;

-- 3. Rename last_login to last_login_date (keep date only)
ALTER TABLE users CHANGE COLUMN last_login last_login_date DATE NULL;

-- 4. Drop old timestamp columns
ALTER TABLE users DROP COLUMN created_at;
ALTER TABLE users DROP COLUMN updated_at;
ALTER TABLE users DROP COLUMN deleted_at;
