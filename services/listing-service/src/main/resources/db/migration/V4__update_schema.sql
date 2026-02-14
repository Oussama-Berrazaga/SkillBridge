
CREATE TABLE proposals (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    proposed_time TIMESTAMP NOT NULL,
    visit_fee DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    version INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT chk_proposal_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT fk_proposal_application FOREIGN KEY (application_id) REFERENCES applications (id) ON DELETE CASCADE
);

ALTER TABLE listings ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE applications ADD COLUMN version INTEGER NOT NULL DEFAULT 0;