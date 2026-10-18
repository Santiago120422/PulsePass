-- Fase 4: seguridad. Tablas nuevas; no se altera ninguna tabla existente.
-- Los usuarios legados (creados antes de esta fase) quedan SIN credenciales y por tanto sin login
-- hasta una activacion segura. Nunca se inserta una contrasena por defecto.

CREATE TABLE user_credentials (
    user_id             BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    password_hash       VARCHAR(100) NOT NULL,
    password_changed_at TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(20) NOT NULL CHECK (role IN ('USER','STAFF','ADMIN')),
    PRIMARY KEY (user_id, role)
);
