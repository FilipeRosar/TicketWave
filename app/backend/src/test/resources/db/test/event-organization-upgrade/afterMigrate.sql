INSERT INTO organizations (id, name, type, status, created_at, updated_at, version)
VALUES ('1d17c10a-99fe-45e6-a277-2bb544e88650', 'Organização já vinculada', 'PF', 'ACTIVE', now(), now(), 0);

INSERT INTO organization_members (id, organization_id, user_id, role, status, created_at, updated_at, version)
VALUES (gen_random_uuid(), '1d17c10a-99fe-45e6-a277-2bb544e88650',
        '12f38108-8030-48db-9fd0-6356b39d9e76', 'OWNER', 'ACTIVE', now(), now(), 0);

INSERT INTO categories (id, name, slug, created_at, updated_at, version)
VALUES ('2ed17b83-913f-49db-9ab9-d974580ca44d', 'Categoria anterior à migração', 'categoria-legada', now(), now(), 0);

INSERT INTO addresses (id, street, number, district, city, state, zip_code, country, created_at, updated_at, version)
VALUES ('1ce43a54-1df9-47ee-9c8b-1655e8f39680', 'Rua de teste', '1', 'Centro', 'São Paulo', 'SP',
        '01000-000', 'Brasil', now(), now(), 0);

INSERT INTO venues (id, name, capacity, address_id, slug, created_at, updated_at, version)
VALUES ('99340dc9-12e7-4925-a217-50e85f6581c9', 'Local anterior à migração', 100,
        '1ce43a54-1df9-47ee-9c8b-1655e8f39680', 'local-legado', now(), now(), 0);

INSERT INTO events (id, title, slug, short_description, description, start_date, end_date, capacity, available_tickets,
                    total_views, total_tickets_sold, total_favorites, ticket_price, event_status, event_type, published,
                    featured, ai_assistant_enabled, networking_enabled, gamification_enabled, published_by, category_id,
                    venue_id, organizer_id, created_at, updated_at, version, organization_id)
SELECT fixture.id, fixture.title, fixture.slug, 'Resumo', 'Descrição',
       TIMESTAMP '2026-06-01 10:00:00', TIMESTAMP '2026-06-01 11:00:00',
       50, 47, 17, 3, 2, 25.00, fixture.status, 'PRESENTIAL', fixture.published,
       FALSE, TRUE, TRUE, TRUE, fixture.publisher,
       '2ed17b83-913f-49db-9ab9-d974580ca44d', '99340dc9-12e7-4925-a217-50e85f6581c9',
       '12f38108-8030-48db-9fd0-6356b39d9e76', TIMESTAMP '2026-01-15 13:00:00',
       TIMESTAMP '2026-03-20 16:00:00', 5, fixture.organization
FROM (VALUES
    ('15cbebc4-799a-4379-86b5-3b3505ddef25'::uuid, 'Evento publicado antes da migração', 'evento-legado-publicado',
     'PUBLISHED', TRUE, '3d8a0edf-c46e-4ddb-9daf-29793262d703'::uuid, NULL::uuid),
    ('34db9e7d-5afd-41c1-a3eb-7acc435d24dd'::uuid, 'Evento cancelado antes da migração', 'evento-legado-cancelado',
     'CANCELLED', FALSE, NULL::uuid, NULL::uuid),
    ('4223c4a2-979d-410e-bd43-a8a1c885376a'::uuid, 'Evento já vinculado', 'evento-ja-vinculado',
     'DRAFT', FALSE, NULL::uuid, '1d17c10a-99fe-45e6-a277-2bb544e88650'::uuid)
) AS fixture(id, title, slug, status, published, publisher, organization);

INSERT INTO ticket_batches (id, event_id, name, price, quantity, sales_start, sales_end, created_at, updated_at, version)
VALUES ('2645580a-2da6-405a-90a2-fa1f35b6c369', '15cbebc4-799a-4379-86b5-3b3505ddef25',
        'Lote histórico', 25, 50, '2026-01-15T13:00:00Z', '2026-06-01T09:00:00Z', now(), now(), 0);
