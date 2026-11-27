ALTER TABLE events
    ADD COLUMN classification VARCHAR(20) NOT NULL DEFAULT 'SOCIAL',
    ADD CONSTRAINT ck_event_classification CHECK (classification IN ('PROFESSIONAL', 'SOCIAL'));
