-- ============================================================
-- V14: Trip rich data tables + dynamic pricing columns
-- ============================================================

-- ── New columns on trips table ────────────────────────────────
ALTER TABLE trips
    ADD COLUMN starts_from           VARCHAR(255)    NULL,
    ADD COLUMN ends_at               VARCHAR(255)    NULL,
    ADD COLUMN about_description     TEXT            NULL,
    ADD COLUMN min_group_size        INT             NULL,
    ADD COLUMN duration_days         INT             NULL,
    ADD COLUMN duration_nights       INT             NULL,
    ADD COLUMN badges                TEXT            NULL,  -- JSON array string
    ADD COLUMN max_discount_percent  DECIMAL(5,2)    NOT NULL DEFAULT 0,
    ADD COLUMN max_increase_percent  DECIMAL(5,2)    NOT NULL DEFAULT 0,
    ADD COLUMN current_price         DECIMAL(12,2)   NULL;

-- ── Trip Highlights ───────────────────────────────────────────
CREATE TABLE trip_highlights (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trip_id     BIGINT          NOT NULL,
    icon        VARCHAR(50)     NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

-- ── Trip Stops (route) ────────────────────────────────────────
CREATE TABLE trip_stops (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trip_id     BIGINT          NOT NULL,
    stop_name   VARCHAR(255)    NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

-- ── Trip Price Breakdown ──────────────────────────────────────
CREATE TABLE trip_price_breakdown (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trip_id     BIGINT          NOT NULL,
    category    VARCHAR(255)    NOT NULL,
    amount      DECIMAL(12,2)   NOT NULL,
    description VARCHAR(500)    NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

-- ── Trip Stays ────────────────────────────────────────────────
CREATE TABLE trip_stays (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    trip_id     BIGINT          NOT NULL,
    name        VARCHAR(255)    NOT NULL,
    location    VARCHAR(255)    NULL,
    description TEXT            NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
);

-- ── Trip Stay Amenities ───────────────────────────────────────
CREATE TABLE trip_stay_amenities (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    stay_id     BIGINT          NOT NULL,
    amenity     VARCHAR(255)    NOT NULL,
    FOREIGN KEY (stay_id) REFERENCES trip_stays(id) ON DELETE CASCADE
);

-- ── Trip Stay Images (Cloudinary URLs) ───────────────────────
CREATE TABLE trip_stay_images (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    stay_id     BIGINT          NOT NULL,
    image_url   VARCHAR(1000)   NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    FOREIGN KEY (stay_id) REFERENCES trip_stays(id) ON DELETE CASCADE
);

-- ── Indexes ───────────────────────────────────────────────────
CREATE INDEX idx_trip_highlights_trip_id    ON trip_highlights(trip_id);
CREATE INDEX idx_trip_stops_trip_id         ON trip_stops(trip_id);
CREATE INDEX idx_trip_price_breakdown_trip  ON trip_price_breakdown(trip_id);
CREATE INDEX idx_trip_stays_trip_id         ON trip_stays(trip_id);
CREATE INDEX idx_trip_stay_amenities_stay   ON trip_stay_amenities(stay_id);
CREATE INDEX idx_trip_stay_images_stay      ON trip_stay_images(stay_id);
