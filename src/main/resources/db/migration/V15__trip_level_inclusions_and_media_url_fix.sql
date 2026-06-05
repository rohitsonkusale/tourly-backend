-- ============================================================
-- V15: Add trip-level inclusions/exclusions table and fix
--      trip_media url column length for Cloudinary URLs
-- ============================================================

-- Trip-level inclusions and exclusions (direct FK to trip)
CREATE TABLE trip_items (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trip_id     BIGINT          NOT NULL,
    type        VARCHAR(20)     NOT NULL,  -- INCLUSION or EXCLUSION
    description VARCHAR(500)    NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

CREATE INDEX idx_trip_items_trip_id ON trip_items(trip_id);

-- Extend trip_media.url to hold Cloudinary URLs (was VARCHAR 500)
ALTER TABLE trip_media
    MODIFY COLUMN url VARCHAR(1000) NOT NULL;
