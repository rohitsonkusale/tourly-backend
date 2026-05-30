-- ============================================================
-- V10: Change admin_approval_flag from BOOLEAN (0/1) to CHAR(1) (Y/N)
-- ============================================================

-- Step 1: Add a temporary column to hold the new Y/N values
ALTER TABLE users ADD COLUMN admin_approval_flag_new CHAR(1) NOT NULL DEFAULT 'N';

-- Step 2: Migrate existing data  (1 → 'Y', 0 → 'N')
UPDATE users SET admin_approval_flag_new = CASE
    WHEN admin_approval_flag = 1 THEN 'Y'
    ELSE 'N'
END;

-- Step 3: Drop the old BOOLEAN column
ALTER TABLE users DROP COLUMN admin_approval_flag;

-- Step 4: Rename the new column to the original name
ALTER TABLE users RENAME COLUMN admin_approval_flag_new TO admin_approval_flag;
