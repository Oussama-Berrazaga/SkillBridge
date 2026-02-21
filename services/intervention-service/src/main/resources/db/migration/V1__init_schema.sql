CREATE TABLE interventions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    scheduled_time TIMESTAMP NOT NULL,
    location VARCHAR(255),
    technician_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    proposal_id BIGINT NOT NULL UNIQUE, -- Unique ensures idempotency at the DB level
    listing_id BIGINT NOT NULL,
    final_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT chk_intervention_status 
        CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'DISPUTED'))
);

-- Indexing for common queries
CREATE INDEX idx_intervention_technician ON interventions(technician_id);
CREATE INDEX idx_intervention_client ON interventions(client_id);
CREATE INDEX idx_intervention_status ON interventions(status);