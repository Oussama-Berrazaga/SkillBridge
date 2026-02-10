-- 1. Create the Profiles table
CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    birth_date DATE,
    phone_number VARCHAR(20),
    bio TEXT,
    average_rating DOUBLE PRECISION DEFAULT 0.0,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. Create the Technician Skills table
CREATE TABLE technician_skills (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    category_name VARCHAR(100),
    years_experience INTEGER,
    CONSTRAINT fk_skills_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

-- 3. Data Migration: Move existing data from users to profiles
INSERT INTO profiles (user_id, first_name, last_name, birth_date, phone_number)
SELECT id, first_name, last_name, date_of_birth, phone_number FROM users;

-- 4. Cleanup: Remove the columns from the users table that are now in profiles
ALTER TABLE users 
DROP COLUMN first_name,
DROP COLUMN last_name,
DROP COLUMN date_of_birth,
DROP COLUMN phone_number;

-- 5. Add password column (which we noticed was missing from your V1 SQL but present in your Entity)
ALTER TABLE users ADD COLUMN password VARCHAR(255);