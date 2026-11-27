CREATE TABLE legacy_event_organization_map (
    organizer_id UUID PRIMARY KEY REFERENCES users(id),
    organization_id UUID NOT NULL UNIQUE
);

INSERT INTO legacy_event_organization_map (organizer_id, organization_id)
SELECT organizer_id, gen_random_uuid()
FROM events
WHERE organization_id IS NULL
GROUP BY organizer_id;

INSERT INTO organizations (id, name, type, status, financial_status, created_at, updated_at, version)
SELECT mapping.organization_id, LEFT(owner.name, 141) || ' (legado)', 'PF', 'ACTIVE', 'PENDING',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM legacy_event_organization_map mapping
JOIN users owner ON owner.id = mapping.organizer_id;

ALTER TABLE legacy_event_organization_map
    ADD CONSTRAINT fk_legacy_event_organization FOREIGN KEY (organization_id) REFERENCES organizations(id);

INSERT INTO organization_members (id, organization_id, user_id, role, status, created_at, updated_at, version)
SELECT gen_random_uuid(), organization_id, organizer_id, 'OWNER', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0
FROM legacy_event_organization_map;

UPDATE events event
SET organization_id = mapping.organization_id
FROM legacy_event_organization_map mapping
WHERE event.organization_id IS NULL AND event.organizer_id = mapping.organizer_id;

ALTER TABLE events ALTER COLUMN organization_id SET NOT NULL;
