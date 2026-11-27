ALTER TABLE organizations
    ADD COLUMN commercial_name VARCHAR(150),
    ADD COLUMN contact_email VARCHAR(255),
    ADD COLUMN commercial_description VARCHAR(1000),
    ADD COLUMN financial_status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
    ADD CONSTRAINT ck_organization_financial_status CHECK (financial_status IN ('PENDING', 'APPROVED'));

ALTER TABLE events ADD COLUMN organization_id UUID REFERENCES organizations(id);

CREATE INDEX idx_events_organization ON events (organization_id);
