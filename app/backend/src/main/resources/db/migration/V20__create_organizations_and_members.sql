CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(2) NOT NULL CHECK (type IN ('PF', 'PJ')),
    status VARCHAR(12) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    document_ciphertext BYTEA,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT ck_organization_document CHECK (type <> 'PJ' OR document_ciphertext IS NOT NULL)
);

CREATE TABLE organization_members (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(12) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'FINANCEIRO', 'OPERADOR')),
    status VARCHAR(12) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT uk_organization_member UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_organization_members_user ON organization_members (user_id);
