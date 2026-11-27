ALTER TABLE participant_profiles
    ADD COLUMN interests TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN goals TEXT[] NOT NULL DEFAULT '{}';
