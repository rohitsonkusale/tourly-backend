-- =============================================
-- V7: Create wishlists table for saved trips
-- =============================================

CREATE TABLE wishlists (
    wishlist_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    trip_id     BIGINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_wishlist_trip FOREIGN KEY (trip_id) REFERENCES trips(trip_id) ON DELETE CASCADE,
    CONSTRAINT uk_wishlist_user_trip UNIQUE (user_id, trip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index for fast lookup by user
CREATE INDEX idx_wishlist_user_id ON wishlists(user_id);
