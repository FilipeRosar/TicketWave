CREATE TABLE participant_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    occupation VARCHAR(120),
    city VARCHAR(120),
    bio VARCHAR(1000),
    photo_type VARCHAR(20),
    photo BYTEA
);
