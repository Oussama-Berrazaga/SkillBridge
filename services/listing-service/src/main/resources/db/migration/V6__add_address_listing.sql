ALTER TABLE listings 
    ADD COLUMN street VARCHAR(255),
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN state VARCHAR(100),
    ADD COLUMN zip_code VARCHAR(20);

-- Optional: Create an index on city for future filtering tasks
CREATE INDEX idx_listings_city ON listings(city);