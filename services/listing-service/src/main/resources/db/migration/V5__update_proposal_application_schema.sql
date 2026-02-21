-- 1. Update PROPOSAL status constraints
-- We drop the old constraint and add the new one including 'PAID'
ALTER TABLE proposals DROP CONSTRAINT chk_proposal_status;

ALTER TABLE proposals 
    ADD CONSTRAINT chk_proposal_status 
    CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELLED', 'PAID'));

-- 2. Update APPLICATION status constraints
-- We drop the old one and add the expanded list: 
-- PENDING, ACCEPTED, VISIT_PROPOSED, REJECTED, WITHDRAWN, PROPOSAL_ACCEPTED, BOOKED
ALTER TABLE applications DROP CONSTRAINT chk_application_status;

ALTER TABLE applications 
    ADD CONSTRAINT chk_application_status 
    CHECK (status IN (
        'PENDING', 
        'ACCEPTED', 
        'VISIT_PROPOSED', 
        'REJECTED', 
        'WITHDRAWN', 
        'PROPOSAL_ACCEPTED', 
        'BOOKED'
    ));