CREATE TABLE addresses
(
    id UUID PRIMARY KEY,

    street VARCHAR(255) NOT NULL,

    number VARCHAR(20) NOT NULL,

    complement VARCHAR(150),

    district VARCHAR(150) NOT NULL,

    city VARCHAR(120) NOT NULL,

    state VARCHAR(2) NOT NULL,

    zip_code VARCHAR(20) NOT NULL,

    country VARCHAR(80) NOT NULL,

    latitude DOUBLE PRECISION,

    longitude DOUBLE PRECISION,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP,

    version BIGINT
);

CREATE INDEX idx_address_city
    ON addresses(city);

CREATE INDEX idx_address_state
    ON addresses(state);