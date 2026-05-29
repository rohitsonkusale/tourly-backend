-- V8__add_social_links_to_users.sql

ALTER TABLE users ADD COLUMN instagram_username VARCHAR(255);
ALTER TABLE users ADD COLUMN website_url VARCHAR(500);
