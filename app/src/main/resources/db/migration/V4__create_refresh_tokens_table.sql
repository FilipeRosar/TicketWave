CREATE TABLE refresh_token(
    id UUID PRIMARY KEY NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL ,
    user_id UUID NOT NULL ,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT,
    CONSTRAINT fk_refresh_user
                          FOREIGN KEY (user_id)
                          REFERENCES users(id)
);