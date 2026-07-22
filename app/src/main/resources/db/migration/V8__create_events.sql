CREATE TABLE events
(
    id UUID PRIMARY KEY,

    title VARCHAR(150) NOT NULL,

    slug VARCHAR(180) NOT NULL UNIQUE,

    short_description VARCHAR(500) NOT NULL,

    description TEXT NOT NULL,

    start_date TIMESTAMP NOT NULL,

    end_date TIMESTAMP NOT NULL,

    capacity INTEGER NOT NULL,

    available_tickets INTEGER NOT NULL,

    total_views INTEGER NOT NULL ,

    total_tickets_sold INTEGER NOT NULL ,

    total_favorites INTEGER NOT NULL,

    ticket_price NUMERIC(10,2) NOT NULL,

    event_status VARCHAR(30) NOT NULL,

    event_type VARCHAR(30) NOT NULL,

    published BOOLEAN NOT NULL,

    featured BOOLEAN NOT NULL,

    ai_assistant_enabled BOOLEAN NOT NULL,

    networking_enabled BOOLEAN NOT NULL,

    gamification_enabled BOOLEAN NOT NULL,

    published_by UUID,

    streaming_url VARCHAR(255),

    category_id UUID NOT NULL,

    venue_id UUID NOT NULL,

    organizer_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    version BIGINT,

    CONSTRAINT fk_event_category
        FOREIGN KEY(category_id)
            REFERENCES categories(id),

    CONSTRAINT fk_event_venue
        FOREIGN KEY(venue_id)
            REFERENCES venues(id),

    CONSTRAINT fk_event_user
        FOREIGN KEY(organizer_id)
            REFERENCES users(id),

    CONSTRAINT fk_event_published_by
        FOREIGN KEY(published_by)
            REFERENCES users(id)
);
CREATE INDEX idx_event_slug
    ON events(slug);

CREATE INDEX idx_event_status
    ON events(event_status);

CREATE INDEX idx_event_category
    ON events(category_id);

CREATE INDEX idx_event_venue
    ON events(venue_id);

CREATE INDEX idx_event_start
    ON events(start_date);

CREATE INDEX idx_event_end
    ON events(end_date);

CREATE INDEX idx_event_organizer
    ON events(organizer_id);