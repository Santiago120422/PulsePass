# Bitácora de ejecución manual — PulsePass E2E

**Estado inicial:** todos los casos están `NOT RUN`. Esta bitácora no afirma que se haya ejecutado Postman; debe actualizarse después de enviar cada solicitud contra la API real.

## Registro por solicitud

| Caso | Actor | Método / ruta | HTTP esperado | HTTP observado | Resultado inicial | Evidencia / observación |
|---|---|---|---|---|---|---|
| SETUP-USR-ADMIN | ANONYMOUS | `POST /api/users` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| SETUP-USR-STAFF | ANONYMOUS | `POST /api/users` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| SETUP-USR-INACTIVE | ANONYMOUS | `POST /api/users` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| SETUP-AUTH-ADMIN | ADMIN | `POST /api/auth/login` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| SETUP-AUTH-STAFF | STAFF | `POST /api/auth/login` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| SETUP-AUTH-INACTIVE | INACTIVE_USER | `POST /api/auth/login` | 200 OK antes de desactivar | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-VEN-02 | ANONYMOUS | `GET /api/venues/active` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-VEN-01 | ANONYMOUS | `GET /api/venues/{{venueActiveCode}}` | 200 OK; código inexistente: 404 | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-ART-03 | ANONYMOUS | `GET /api/artists/active` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-ART-01 | ANONYMOUS | `GET /api/artists/{{artistId}}` | 200 OK; id inexistente: 404 | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-ART-02 | ANONYMOUS | `GET /api/artists/by-stage-name?stageName={{artistStageName}}` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-USR-01 | ANONYMOUS | `POST /api/users` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-AUTH-01 | USER_A | `POST /api/auth/login` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-AUTH-02 | USER_A | `GET /api/auth/me` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-USR-02 | USER_A | `GET /api/users/by-email?email={{userAEmail}}` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-USR-03 | USER_A | `GET /api/users/by-username?username={{userAUsername}}` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-EVT-01 | ADMIN | `POST /api/events` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-EVT-05 | ADMIN | `POST /api/events/{{newEventCode}}/artists/{{artistId}}` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-EVT-04 | ADMIN | `PATCH /api/events/{{newEventCode}}/publish` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-EVT-02 | ANONYMOUS | `GET /api/events/{{publishedEventCode}}` | 200 OK; evento ausente o borrador oculto: 404 | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-EVT-03 | ANONYMOUS | `GET /api/events/published` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-EVT-06 | ANONYMOUS | `GET /api/events/by-artist?stageName={{artistStageName}}` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| SETUP-EVT-DRAFT | ADMIN | `POST /api/events` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-TKT-01 | USER_A | `POST /api/tickets` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-TKT-02 | USER_A | `GET /api/tickets/{{ticketCancelableCode}}` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-TKT-03 | USER_A | `GET /api/tickets/by-user?email={{userAEmail}}` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-TKT-04 | STAFF o ADMIN | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-TKT-05 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-TKT-01B | USER_A | `POST /api/tickets` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-TKT-06 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-USR-01B | ANONYMOUS | `POST /api/users` | 201 Created | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| E2E-AUTH-01B | USER_B | `POST /api/auth/login` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-016 | USER_B | `GET /api/auth/me` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-001 | ANONYMOUS | `POST /api/events` | 401 Unauthorized | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-002 | USER_A | `POST /api/events` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-003 | USER_A | `PATCH /api/events/{{draftEventCode}}/publish` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-004 | USER_A | `POST /api/events/{{draftEventCode}}/artists/{{artistId}}` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-005 | ANONYMOUS | `GET /api/events/{{draftEventCode}}` | 404 Not Found | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-006 | USER_B | `GET /api/users/by-email?email={{userAEmail}}` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-007 | USER_B | `GET /api/users/by-username?username={{userAUsername}}` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-008 | USER_A | `POST /api/tickets` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-009 | USER_B | `GET /api/tickets/{{ticketCancelableCode}}` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-010 | USER_B | `GET /api/tickets/by-user?email={{userAEmail}}` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-011 | USER_B | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-012 | USER_A | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-AUTH-004 | ANONYMOUS | `POST /api/auth/login` | 401 Unauthorized | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-AUTH-007 | ANONYMOUS | `GET /api/auth/me` | 401 Unauthorized | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-AUTH-008 | ANONYMOUS | `GET /api/auth/me` | 401 Unauthorized | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-AUTH-013 | INACTIVE_USER | `GET /api/auth/me` | 401 Unauthorized | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-013 | USER_A | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 Forbidden | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-014 | ANONYMOUS | `POST /api/tickets` | 401 Unauthorized | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-SEC-015 | ADMIN | `GET /api/events/{{draftEventCode}}` | 200 OK | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-HTTP-400-001 | ANONYMOUS | `POST /api/users` | 400 Bad Request | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-HTTP-400-002 | ADMIN | `POST /api/events` | 400 Bad Request | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-BIZ-409-001 | ANONYMOUS | `POST /api/users` | 409 Conflict | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-BIZ-409-002 | ADMIN | `PATCH /api/events/{{publishedEventCode}}/publish` | 409 Conflict | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-BIZ-409-003 | USER_A | `PATCH /api/tickets/{{ticketCancelableCode}}/cancel` | 409 Conflict | Pendiente | NOT RUN | Pendiente de ejecución manual. |
| TC-BIZ-409-004 | STAFF | `PATCH /api/tickets/{{ticketUsableCode}}/use` | 409 Conflict | Pendiente | NOT RUN | Pendiente de ejecución manual. |

| TC-AUTH-009 | USER_A | `GET /api/auth/me` con JWT manipulado | 401 Unauthorized | Pendiente | NOT RUN | Copiar token y cambiar un carácter; nunca guardar el token en evidencia. |
| TC-AUTH-010 | USER_A | `GET /api/auth/me` con JWT expirado | 401 Unauthorized | Pendiente | NOT RUN | Si no hay fixture expirado de prueba, marcar BLOCKED. |
| TC-AUTH-011 | USER_A | `GET /api/auth/me` con clave/issuer diferente | 401 Unauthorized | Pendiente | NOT RUN | Usar solo fixture de test; si no existe, BLOCKED. |
| TC-AUTH-012 | ANONYMOUS | `GET /api/auth/me?access_token={{userAToken}}` | 401 Unauthorized | Pendiente | NOT RUN | No enviar header Authorization. |
| TC-AUTH-015 | ANONYMOUS + Bearer inválido | `GET /api/venues/active` | 401 Unauthorized | Pendiente | NOT RUN | El Bearer inválido presente debe rechazarse. |
| TC-SEC-017 | STAFF con token previo | `GET /api/events/{{publishedEventCode}}/tickets/paid` | 403 Forbidden tras revocar STAFF | Pendiente | NOT RUN | Revocar rol temporalmente y restaurarlo; no usar otro entorno. |

## Consolidado

- PASS: 0 (pendiente de ejecución manual).
- FAIL: 0 (pendiente de ejecución manual).
- BLOCKED: 0 (marcar cuando falte una precondición).
- NOT RUN: 64 solicitudes registradas.

Registrar fecha/hora, commit probado, ambiente, actor, HTTP y Content-Type observados, campos JSON relevantes, efecto persistido y evidencia redactada. Nunca copiar tokens, contraseñas, hashes ni secretos a esta bitácora.
