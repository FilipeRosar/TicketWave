CREATE TABLE categories(
        id UUID PRIMARY KEY,

        name VARCHAR(80) NOT NULL UNIQUE,

        slug VARCHAR(100) NOT NULL UNIQUE,

        description VARCHAR(255),

        active BOOLEAN NOT NULL DEFAULT TRUE,

        created_at TIMESTAMP NOT NULL,

        updated_at TIMESTAMP,

        version BIGINT
);

CREATE INDEX idx_categories_slug
    ON categories(slug);

CREATE INDEX idx_categories_name
    ON categories(name);