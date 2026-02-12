-- 1. Create Categories Table (Self-referencing)
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    icon_code VARCHAR(50),
    parent_id BIGINT,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories (id)
);

-- 2. Create Listings Table
CREATE TABLE listings (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    CONSTRAINT chk_listing_status CHECK (status IN ('DRAFT', 'ACTIVE', 'FILLED', 'ARCHIVED')),
    customer_id BIGINT NOT NULL
);

-- 3. Create Applications Table
CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    technician_id BIGINT NOT NULL,
    message TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_application_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN')),
    listing_id BIGINT,
    CONSTRAINT fk_application_listing FOREIGN KEY (listing_id) REFERENCES listings (id)
);

-- 4. Create Join Table for many-to-many (Listing <-> Category)
CREATE TABLE listings_categories (
    listing_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (listing_id, category_id),
    CONSTRAINT fk_lc_listing FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE,
    CONSTRAINT fk_lc_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE CASCADE
);