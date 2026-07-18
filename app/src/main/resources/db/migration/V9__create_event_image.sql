CREATE TABLE event_images
(
    id UUID PRIMARY KEY,

    image_url VARCHAR(500) NOT NULL,

    image_key VARCHAR(300) NOT NULL,

    cover BOOLEAN NOT NULL,

    display_order INTEGER NOT NULL,

    event_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    version BIGINT,

    CONSTRAINT fk_event_image
        FOREIGN KEY(event_id)
            REFERENCES events(id)
            ON DELETE CASCADE
);

CREATE INDEX idx_event_images_event
    ON event_images(event_id);