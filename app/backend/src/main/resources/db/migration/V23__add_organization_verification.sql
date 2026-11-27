ALTER TABLE organizations
    ADD COLUMN verification_status VARCHAR(12) NOT NULL DEFAULT 'PENDING'
        CHECK (verification_status IN ('PENDING', 'IN_REVIEW', 'APPROVED', 'REJECTED')),
    ADD COLUMN verification_reason VARCHAR(24)
        CHECK (verification_reason IN ('INVALID_DOCUMENT', 'DATA_MISMATCH', 'INSUFFICIENT_EVIDENCE')),
    ADD COLUMN verification_requested_by UUID REFERENCES users(id),
    DROP CONSTRAINT ck_organization_document,
    ADD CONSTRAINT ck_organization_document CHECK (
        type <> 'PJ' OR document_ciphertext IS NOT NULL OR verification_status IN ('APPROVED', 'REJECTED')
    );

CREATE TABLE organization_verification_history (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    actor_id UUID NOT NULL REFERENCES users(id),
    previous_status VARCHAR(12) NOT NULL CHECK (previous_status IN ('PENDING', 'IN_REVIEW', 'APPROVED', 'REJECTED')),
    next_status VARCHAR(12) NOT NULL CHECK (next_status IN ('PENDING', 'IN_REVIEW', 'APPROVED', 'REJECTED')),
    reason VARCHAR(24) CHECK (reason IN ('INVALID_DOCUMENT', 'DATA_MISMATCH', 'INSUFFICIENT_EVIDENCE')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);
CREATE INDEX idx_org_verification_history ON organization_verification_history (organization_id, created_at);
CREATE INDEX idx_org_verification_queue ON organizations (verification_status);
