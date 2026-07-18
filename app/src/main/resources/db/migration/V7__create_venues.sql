CREATE TABLE venues
(
    id UUID PRIMARY KEY,

    name VARCHAR(150) NOT NULL,

    description TEXT,

    capacity INTEGER NOT NULL,

    phone VARCHAR(30),

    email VARCHAR(120),

    website VARCHAR(255),

    address_id UUID NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    version BIGINT,

    CONSTRAINT fk_venue_address
        FOREIGN KEY(address_id)
            REFERENCES addresses(id)
);