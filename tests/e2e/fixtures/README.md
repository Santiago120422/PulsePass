# Fixtures locales de E2E

Esta carpeta contiene datos de catálogo no sensibles para ejecutar la colección manual. Usa exclusivamente una base local descartable/de prueba, después de que Flyway haya aplicado V1–V4. Confirma la base y las credenciales antes de ejecutar SQL. El script `seed-e2e.sql` no elimina ni trunca datos.

## Fixtures incluidos

- Venue activo: `VEN-E2E-01` (`PulsePass E2E Venue`), capacidad 100.
- Artista activo: `PulsePass E2E Artist`.

Ejecuta `seed-e2e.sql` una sola vez contra la base local de pruebas. Después consulta `/api/venues/active` y `/api/artists/active` y copia los valores reales a variables de Postman.

## Crear actores de prueba

1. Registra `USER_A` y `USER_B` mediante `POST /api/users` usando credenciales ficticias únicas. El registro público debe asignar solo `USER`.
2. Para `STAFF` y `ADMIN`, registra cuentas normales primero y asígnales el rol únicamente fuera de HTTP, en una base local de prueba aislada. Nunca agregues un campo `role` al registro público para elevar privilegios.
3. Ejemplo de SQL local para provisionar un rol privilegiado (sustituye el username por el actor que acabas de registrar):

```sql
-- EJECUTAR SOLO EN UNA BASE LOCAL/DESCARTABLE DE E2E.
-- Verifica current_database() antes de continuar.
SELECT current_database();

DELETE FROM user_roles
WHERE user_id = (SELECT id FROM users WHERE username = 'e2e_admin_cambiar')
  AND role = 'USER';
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'e2e_admin_cambiar'
ON CONFLICT DO NOTHING;

DELETE FROM user_roles
WHERE user_id = (SELECT id FROM users WHERE username = 'e2e_staff_cambiar')
  AND role = 'USER';
INSERT INTO user_roles (user_id, role)
SELECT id, 'STAFF' FROM users WHERE username = 'e2e_staff_cambiar'
ON CONFLICT DO NOTHING;
```

4. Para preparar una cuenta inactiva, hazlo en la base de prueba y solo para el usuario ficticio de esa corrida: `UPDATE users SET active = false WHERE username = 'e2e_inactive_cambiar';`.
5. Tras cambiar roles o activar/desactivar cuentas, vuelve a hacer login y comprueba `/api/auth/me`. Las peticiones JWT recargan el estado y roles vigentes desde la base.

Nunca ejecutes los SQL anteriores en desarrollo compartido o producción. No versionar contraseñas, hashes de credenciales, tokens ni exportaciones Postman con valores sensibles.
