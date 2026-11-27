ALTER TABLE users
    ADD COLUMN birth_date DATE,
    ADD COLUMN cpf VARCHAR(11),
    ADD CONSTRAINT users_cpf_key UNIQUE (cpf);
