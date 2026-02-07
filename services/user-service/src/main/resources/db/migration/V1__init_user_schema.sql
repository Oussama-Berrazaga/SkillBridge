-- Create the User table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    date_of_birth DATE,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- To store CLIENT, TECHNICIAN, etc.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add an index on email since we will search by it constantly (e.g., during Login)
CREATE INDEX idx_users_email ON users(email);
