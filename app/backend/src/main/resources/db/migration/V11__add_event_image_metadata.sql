ALTER TABLE event_image
    ADD COLUMN width INTEGER,
    ADD COLUMN height INTEGER,
    ADD COLUMN size BIGINT,
    ADD COLUMN file_name VARCHAR(255),
    ADD COLUMN content_type VARCHAR(255);
