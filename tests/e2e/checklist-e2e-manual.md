# Checklist manual E2E — PulsePass

Marcar una casilla solo después de realizar la acción manualmente. Si no se pudo ejecutar por falta de precondiciones, anotar `BLOCKED`; lo no probado permanece `NOT RUN`.

## Preflight

- [ ] Confirmé que la URL y la base son un entorno local de prueba.
- [ ] La aplicación arranca con Java 21 y las migraciones V1–V4 están aplicadas.
- [ ] El secreto JWT se definió en el entorno local, no en el repositorio.
- [ ] Importé la colección y el ambiente Postman; no hay scripts habilitados en requests.
- [ ] Preparé venue y artista activos, evento publicado/por publicar, USER_A y USER_B, y cuentas STAFF/ADMIN.
- [ ] `runId`, usuarios/emails y códigos de eventos son únicos.

## Flujo público

- [ ] `E2E-VEN-02`: GET `/api/venues/active` → 200.
- [ ] `E2E-VEN-01`: GET `/api/venues/{code}` → 200; código ausente → 404.
- [ ] `E2E-ART-03`: GET `/api/artists/active` → 200.
- [ ] `E2E-ART-01`: GET `/api/artists/{id}` → 200; ID ausente → 404.
- [ ] `E2E-ART-02`: GET `/api/artists/by-stage-name` → 200.
- [ ] `E2E-EVT-03`: GET `/api/events/published` → 200, sin borradores.
- [ ] `E2E-EVT-02`: GET evento publicado → 200; borrador oculto/ausente → 404.
- [ ] `E2E-EVT-06`: GET eventos por artista → no expone eventos no publicados al público.

## Registro y autenticación

- [ ] `E2E-USR-01`: registrar USER_A → 201, solo rol USER, sin hash/contraseña en respuesta.
- [ ] `E2E-AUTH-01`: login USER_A → 200, Bearer y TTL configurado; copié token manualmente.
- [ ] `E2E-AUTH-02`: GET `/api/auth/me` → 200 y usuario correcto.
- [ ] Registré USER_B con identidad independiente e hice login; copié token manualmente.
- [ ] Login con contraseña incorrecta → 401 genérico.
- [ ] GET `/api/auth/me` sin token y con token inválido → 401 JSON, no 500.
- [ ] Probé token expirado/alterado si hay una forma segura de generar el fixture; si no, registré `BLOCKED`.
- [ ] `TC-AUTH-009`: JWT manipulado → 401.
- [ ] `TC-AUTH-010`: JWT expirado → 401; si no hubo fixture, `BLOCKED`.
- [ ] `TC-AUTH-011`: JWT con clave/issuer distintos → 401; si no hubo fixture seguro, `BLOCKED`.
- [ ] `TC-AUTH-012`: token solo en query string → 401.
- [ ] `TC-AUTH-015`: Bearer inválido en ruta pública → 401 según el filtro implementado.
- [ ] `TC-SEC-017`: revocar STAFF en BD, verificar que el token previo ya no autoriza y restaurar el rol.

## Usuario, eventos y tickets

- [ ] `E2E-USR-02`: USER_A consulta su email → 200.
- [ ] `E2E-USR-03`: USER_A consulta su username → 200.
- [ ] USER_B no consulta los datos de USER_A: email y username → 403.
- [ ] `E2E-EVT-01`: ADMIN crea evento → 201, inicia DRAFT.
- [ ] `E2E-EVT-05`: ADMIN asocia artista → 200 y la relación es observable.
- [ ] `E2E-EVT-04`: ADMIN publica → 200; GET posterior confirma PUBLISHED.
- [ ] `E2E-TKT-01`: USER_A compra → 201; guardé ticketCancelableCode.
- [ ] `E2E-TKT-02`: USER_A consulta su ticket → 200.
- [ ] `E2E-TKT-03`: USER_A lista sus tickets → 200, solo propios.
- [ ] USER_B no consulta el ticket de A ni lista tickets de A → 403.
- [ ] USER_A compra un segundo ticket independiente; guardé ticketUsableCode.
- [ ] `E2E-TKT-04`: STAFF/ADMIN lista tickets PAID del evento → 200.
- [ ] USER_B intenta cancelar ticket de A → 403 y el estado no cambia (hacer antes de cancelar como propietario).
- [ ] USER intenta marcar ticket como usado → 403 y el estado no cambia (hacer antes del uso por STAFF).
- [ ] USER intenta comprar con email de B → 403 y no aparece ticket nuevo.
- [ ] `E2E-TKT-05`: USER_A cancela ticket cancelable → 200 y GET posterior confirma estado.
- [ ] `E2E-TKT-06`: STAFF usa el segundo ticket → 200 y GET posterior confirma USED.

## Autorización, errores y evidencia

- [ ] Anónimo no crea eventos → 401 con ErrorResponse.
- [ ] USER no crea/publica eventos ni asocia artistas → 403 con ErrorResponse.
- [ ] `400`, `404` y `409` se probaron donde aplican.
- [ ] Inspeccioné `timestamp`, `status`, `error`, `message`, `details` y la ausencia de stack traces/secrets.
- [ ] Registré HTTP esperado/observado, resultado y evidencia sin credenciales/tokens.
- [ ] Ningún escenario no ejecutado se marcó como PASS.
