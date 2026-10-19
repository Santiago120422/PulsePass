# Checklist de ejecución manual E2E — PulsePass

## 1. Información de la ejecución

- [x] Proyecto: PulsePass
- [x] Entorno local configurado
- [x] Aplicación Spring Boot iniciada
- [x] PostgreSQL iniciado mediante Docker
- [x] Base de datos `pulsepass` disponible
- [x] Colección manual importada en Postman
- [x] Entorno `PulsePass local (plantilla sin secretos)` seleccionado
- [x] Variables de prueba configuradas
- [x] Datos de prueba preparados
- [x] Tokens gestionados mediante variables locales de Postman

## 2. Preparación de actores de prueba

- [x] SETUP-USR-ADMIN — Registrar cuenta ADMIN
- [x] SETUP-USR-STAFF — Registrar cuenta STAFF
- [x] SETUP-USR-INACTIVE — Registrar cuenta inactiva
- [x] SETUP-AUTH-ADMIN — Login de ADMIN tras provisionar rol
- [x] SETUP-AUTH-STAFF — Login de STAFF tras provisionar rol
- [x] SETUP-AUTH-INACTIVE — Login inicial de usuario inactivo

## 3. Sedes y artistas

- [x] E2E-VEN-01 — Consultar sede por código
- [x] E2E-VEN-02 — Listar sedes activas
- [x] E2E-ART-01 — Consultar artista por ID
- [x] E2E-ART-02 — Consultar artista por nombre artístico
- [x] E2E-ART-03 — Listar artistas activos

## 4. Usuarios y autenticación básica

- [x] E2E-USR-01 — Registrar USER_A
- [x] E2E-AUTH-01 — Login de USER_A
- [x] E2E-AUTH-02 — Identidad autenticada de USER_A
- [x] E2E-USR-02 — Consultar usuario propio por email
- [x] E2E-USR-03 — Consultar usuario propio por username

## 5. Eventos

- [x] E2E-EVT-01 — Crear evento borrador
- [x] E2E-EVT-02 — Consultar evento por código
- [x] E2E-EVT-03 — Listar eventos publicados
- [x] E2E-EVT-04 — Publicar evento
- [x] E2E-EVT-05 — Asociar artista al evento
- [x] E2E-EVT-06 — Buscar eventos por artista
- [x] SETUP-EVT-DRAFT — Crear borrador separado para escenarios negativos

## 6. Compra y gestión de entradas

- [x] E2E-TKT-01 — Comprar ticket propio
- [x] E2E-TKT-02 — Consultar ticket propio
- [x] E2E-TKT-03 — Listar tickets propios
- [x] E2E-TKT-04 — Listar tickets pagados de un evento
- [x] E2E-TKT-05 — Cancelar ticket propio
- [x] E2E-TKT-01B — Comprar segundo ticket para marcarlo como usado
- [x] E2E-TKT-06 — Marcar como usado un ticket

## 7. Seguridad y autorización

- [x] E2E-USR-01B — Registrar USER_B
- [x] E2E-AUTH-01B — Login de USER_B
- [x] TC-SEC-016 — Verificar que el rol enviado en el registro no eleve privilegios
- [x] TC-SEC-001 — Sin token no puede crear eventos
- [x] TC-SEC-002 — USER no puede crear eventos
- [x] TC-SEC-003 — USER no puede publicar eventos
- [x] TC-SEC-004 — USER no puede asociar artistas
- [x] TC-SEC-005 — Visitante no puede ver un borrador
- [x] TC-SEC-006 — USER_B no puede consultar datos de USER_A por email
- [x] TC-SEC-007 — USER_B no puede consultar datos de USER_A por username
- [x] TC-SEC-008 — Compra con email ajeno es denegada
- [x] TC-SEC-009 — USER_B no puede leer ticket de USER_A
- [x] TC-SEC-010 — USER_B no puede listar tickets de USER_A
- [x] TC-SEC-011 — USER_B no puede cancelar ticket ajeno
- [x] TC-SEC-012 — USER normal no puede marcar un ticket como usado
- [x] TC-SEC-013 — USER no puede listar tickets pagados del evento
- [x] TC-SEC-014 — Anónimo no puede comprar ticket
- [x] TC-SEC-015 — ADMIN puede consultar borrador
- [x] TC-