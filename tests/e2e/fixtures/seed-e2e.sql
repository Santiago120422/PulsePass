-- Fixtures idempotentes de catálogo para pruebas manuales locales.
-- Ejecutar SOLO contra una base PulsePass de prueba. No contiene usuarios, contraseñas ni tokens.
-- Verifica primero el nombre de la base conectada en tu cliente SQL.

INSERT INTO venues (code, name, city, address, capacity, active)
VALUES ('VEN-E2E-01', 'PulsePass E2E Venue', 'Santa Marta', 'Dirección ficticia de prueba', 100, TRUE)
ON CONFLICT (code) DO UPDATE
SET active = TRUE, capacity = 100;

INSERT INTO artists (stage_name, country, genre, active)
VALUES ('PulsePass E2E Artist', 'Colombia', 'Pop', TRUE)
ON CONFLICT (stage_name) DO UPDATE
SET active = TRUE;
