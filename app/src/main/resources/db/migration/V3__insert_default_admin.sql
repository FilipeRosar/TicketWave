INSERT INTO users (
    id,
    name,
    email,
    password,
    role,
    phone,
    enabled,
    created_at,
    updated_at,
    version
) VALUES (
             '3d8a0edf-c46e-4ddb-9daf-29793262d703',
             'Administrador',
             'admin@ticketwave.com',
             '123456',
             'ROLE_ADMIN',
             '00000000000',
             true,
             CURRENT_TIMESTAMP,
             CURRENT_TIMESTAMP,
             0
         ) ON CONFLICT(email) DO NOTHING;