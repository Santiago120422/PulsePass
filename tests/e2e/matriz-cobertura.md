# Matriz de cobertura E2E — PulsePass

La columna “Ejecución” no se completa desde el código fuente: se llena después de enviar la petición en Postman y observar el resultado real. El estado inicial correspondiente es `NOT RUN`.

| ID base | Método y ruta | Actor para éxito | HTTP esperado | Controles principales |
|---|---|---|---:|---|
| E2E-VEN-01 | GET `/api/venues/{code}` | Anónimo | 200 | Venue real; inexistente 404 |
| E2E-VEN-02 | GET `/api/venues/active` | Anónimo | 200 | Solo venues activos |
| E2E-EVT-01 | POST `/api/events` | ADMIN | 201 | Evento DRAFT; USER 403/anónimo 401 |
| E2E-EVT-02 | GET `/api/events/{eventCode}` | Anónimo sobre publicado; STAFF/ADMIN para draft | 200 | Borrador oculto con 404 |
| E2E-EVT-03 | GET `/api/events/published` | Anónimo | 200 | No incluye borradores |
| E2E-EVT-04 | PATCH `/api/events/{eventCode}/publish` | ADMIN | 200 | Persistencia de status; USER 403 |
| E2E-EVT-05 | POST `/api/events/{eventCode}/artists/{artistId}` | ADMIN | 200 | Asociación visible; duplicado según Service |
| E2E-EVT-06 | GET `/api/events/by-artist?stageName=...` | Anónimo para publicados | 200 | No filtra eventos no publicados |
| E2E-ART-01 | GET `/api/artists/{id}` | Anónimo | 200 | Inexistente 404 |
| E2E-ART-02 | GET `/api/artists/by-stage-name?stageName=...` | Anónimo | 200 | DTO consistente |
| E2E-ART-03 | GET `/api/artists/active` | Anónimo | 200 | Solo activos |
| E2E-USR-01 | POST `/api/users` | Anónimo | 201 | Rol inicial USER; password/hash nunca devueltos |
| E2E-USR-02 | GET `/api/users/by-email?email=...` | Propio USER o ADMIN | 200 | USER ajeno 403 |
| E2E-USR-03 | GET `/api/users/by-username?username=...` | Propio USER o ADMIN | 200 | USER ajeno 403 |
| E2E-TKT-01 | POST `/api/tickets` | USER propietario | 201 | Solo compra para el principal; email ajeno 403 |
| E2E-TKT-02 | GET `/api/tickets/{ticketCode}` | Propietario/OPS autorizado | 200 | Usuario ajeno 403 |
| E2E-TKT-03 | GET `/api/tickets/by-user?email=...` | Propietario o ADMIN | 200 | Email ajeno 403 |
| E2E-TKT-04 | GET `/api/events/{eventCode}/tickets/paid` | STAFF/ADMIN | 200 | USER 403; solo PAID |
| E2E-TKT-05 | PATCH `/api/tickets/{ticketCode}/cancel` | Propietario o ADMIN | 200 | Ajeno 403; transición inválida 409 |
| E2E-TKT-06 | PATCH `/api/tickets/{ticketCode}/use` | STAFF/ADMIN | 200 | USER 403; transición inválida 409 |
| E2E-AUTH-01 | POST `/api/auth/login` | Anónimo con credenciales correctas | 200 | Bearer no vacío; credenciales erróneas 401 genérico |
| E2E-AUTH-02 | GET `/api/auth/me` | Actor autenticado | 200 | Identidad efectiva; sin token/token inválido 401 |

## Casos transversales de seguridad

| ID | Escenario | HTTP esperado | Evidencia requerida |
|---|---|---:|---|
| TC-SEC-001 | Anónimo intenta crear evento | 401 | ErrorResponse y evento no creado |
| TC-SEC-002 | USER intenta crear evento | 403 | ErrorResponse, sin mutación |
| TC-SEC-003 | USER intenta publicar | 403 | Estado sigue DRAFT |
| TC-SEC-004 | USER intenta asociar artista | 403 | Relación sin cambios |
| TC-SEC-005 | Anónimo lee borrador | 404 | Sin campos del borrador |
| TC-SEC-006/007 | USER_B consulta datos de USER_A | 403 | Sin datos personales filtrados |
| TC-SEC-008 | USER compra usando email ajeno | 403 | Ningún ticket nuevo para B |
| TC-SEC-009/010 | USER_B consulta ticket/lista de A | 403 | Sin datos de A |
| TC-SEC-011 | USER_B cancela ticket de A | 403 | GET posterior confirma que no mutó |
| TC-SEC-012 | USER intenta marcar ticket usado | 403 | GET posterior confirma que no mutó |
| TC-AUTH-004 | Login contraseña errónea | 401 | Mensaje genérico |
| TC-AUTH-007/008 | /me sin token o Bearer inválido | 401 | ErrorResponse; no 500 |

El contrato de propiedad de este proyecto elige `403` para un recurso ajeno; para eventos no publicados se elige `404` para ocultar la existencia del borrador. No aceptar cualquier 403/404 de forma indiferenciada: comparar con la política documentada.
