# Bitácora de ejecución manual E2E — PulsePass

## 1. Información general

- **Proyecto:** PulsePass
- **Fase:** Pruebas manuales E2E de API REST
- **Fecha de ejecución:** 19 de octubre de 2026
- **Entorno:** Local
- **URL base:** `http://localhost:8080`
- **Base de datos:** PostgreSQL
- **Base de datos utilizada:** `pulsepass`
- **Contenedor PostgreSQL:** `pulsepass-db`
- **Herramienta de ejecución:** Postman
- **Colección:** `PulsePass — E2E Manual`
- **Total de solicitudes:** 64
- **Solicitudes ejecutadas:** 64
- **Solicitudes aprobadas:** 64
- **Solicitudes fallidas:** 0
- **Solicitudes pendientes:** 0

## 2. Resultado general

| Indicador | Resultado |
|---|---:|
| Total de solicitudes de la colección | 64 |
| Solicitudes ejecutadas | 64 |
| PASS | 64 |
| FAIL | 0 |
| NOT RUN | 0 |
| BLOCKED | 0 |
| Porcentaje de aprobación | 100 % |

Todas las solicitudes contempladas en la colección fueron ejecutadas manualmente. Los resultados obtenidos coincidieron con los resultados esperados de cada caso, incluidos los escenarios negativos, las restricciones de autorización y los conflictos de negocio.

Los códigos HTTP `401`, `403`, `404`, `400` y `409` se consideran resultados satisfactorios cuando corresponden al comportamiento definido por la prueba.

---

## 3. Preparación del entorno

Se inició PostgreSQL mediante Docker y se verificó que la aplicación Spring Boot estuviera disponible en el puerto 8080.

Se utilizaron las siguientes herramientas y recursos:

- Aplicación Spring Boot ejecutándose localmente.
- Base de datos PostgreSQL `pulsepass`.
- Colección manual de Postman.
- Entorno `PulsePass local (plantilla sin secretos)`.
- Datos de prueba para sedes, artistas, usuarios, eventos y entradas.
- Cuentas de prueba para los roles `USER`, `STAFF` y `ADMIN`.

### Datos principales utilizados

| Recurso | Identificador | Resultado |
|---|---|---|
| Sede E2E | `VEN-E2E-01` | Disponible y activa |
| Artista E2E | ID `1` | Disponible y activo |
| Evento publicado | `EVT-E2E-20261019-0002` | Publicado correctamente |
| Evento borrador | `EVT-DRAFT-E2E-20261019-0002` | Creado y conservado como borrador |
| USER_A | Cuenta de prueba | Registrada y autenticada |
| USER_B | Cuenta de prueba | Registrada y autenticada |
| ADMIN | Cuenta de prueba | Preparada para operaciones administrativas |
| STAFF | Cuenta de prueba | Preparada para operaciones de personal |

Las cuentas administrativas y de personal se prepararon con los roles necesarios en la base de datos local. Las pruebas que requerían estos roles se ejecutaron utilizando las cuentas correspondientes.

Los tokens y las contraseñas se mantuvieron en las variables locales de Postman y no se incluyen en esta bitácora.

---

## 4. Registro de las 64 solicitudes

### 4.1. Preparación de actores de prueba

| N.º | Caso | Método y ruta | HTTP observado | Resultado |
|---:|---|---|---|---|
| 1 | `SETUP-USR-ADMIN` — Registrar cuenta ADMIN de prueba | `POST /api/users` | `201 Created` | PASS |
| 2 | `SETUP-USR-STAFF` — Registrar cuenta STAFF de prueba | `POST /api/users` | `201 Created` | PASS |
| 3 | `SETUP-USR-INACTIVE` — Registrar cuenta inactiva de prueba | `POST /api/users` | `201 Created` | PASS |
| 4 | `SETUP-AUTH-ADMIN` — Login de ADMIN tras provisionar rol | `POST /api/auth/login` | `200 OK` | PASS |
| 5 | `SETUP-AUTH-STAFF` — Login de STAFF tras provisionar rol | `POST /api/auth/login` | `200 OK` | PASS |
| 6 | `SETUP-AUTH-INACTIVE` — Login inicial de usuario inactivo | `POST /api/auth/login` | `200 OK` | PASS |

**Resultado de la sección:** las cuentas de preparación se registraron y las operaciones iniciales de autenticación funcionaron según lo esperado.

### 4.2. Consultas de sedes y artistas

| N.º | Caso | Método y ruta | HTTP observado | Resultado |
|---:|---|---|---|---|
| 7 | `E2E-VEN-02` — Consultar sedes activas | `GET /api/venues/active` | `200 OK` | PASS |
| 8 | `E2E-VEN-01` — Consultar sede por código | `GET /api/venues/{venueActiveCode}` | `200 OK` | PASS |
| 9 | `E2E-ART-03` — Listar artistas activos | `GET /api/artists/active` | `200 OK` | PASS |
| 10 | `E2E-ART-01` — Consultar artista por ID | `GET /api/artists/{artistId}` | `200 OK` | PASS |
| 11 | `E2E-ART-02` — Consultar artista por nombre artístico | `GET /api/artists/by-stage-name?stageName={artistStageName}` | `200 OK` | PASS |

**Resultado de la sección:** las consultas públicas de sedes y artistas devolvieron los recursos de prueba esperados.

### 4.3. Registro y autenticación de usuarios

| N.º | Caso | Método y ruta | HTTP observado | Resultado |
|---:|---|---|---|---|
| 12 | `E2E-USR-01` — Registrar USER_A | `POST /api/users` | `201 Created` | PASS |
| 13 | `E2E-AUTH-01` — Login de USER_A | `POST /api/auth/login` | `200 OK` | PASS |
| 14 | `E2E-AUTH-02` — Identidad autenticada de USER_A | `GET /api/auth/me` | `200 OK` | PASS |
| 15 | `E2E-USR-02` — Consultar usuario propio por email | `GET /api/users/by-email?email={userAEmail}` | `200 OK` | PASS |
| 16 | `E2E-USR-03` — Consultar usuario propio por username | `GET /api/users/by-username?username={userAUsername}` | `200 OK` | PASS |

**Resultado de la sección:** el registro de USER_A, el inicio de sesión y las consultas de su propia identidad funcionaron correctamente.

### 4.4. Eventos

| N.º | Caso | Método y ruta | HTTP observado | Resultado |
|---:|---|---|---|---|
| 17 | `E2E-EVT-01` — Crear evento borrador | `POST /api/events` | `201 Created` | PASS |
| 18 | `E2E-EVT-05` — Asociar artista al evento | `POST /api/events/{newEventCode}/artists/{artistId}` | `200 OK` | PASS |
| 19 | `E2E-EVT-04` — Publicar evento | `PATCH /api/events/{newEventCode}/publish` | `200 OK` | PASS |
| 20 | `E2E-EVT-02` — Consultar evento por código | `GET /api/events/{publishedEventCode}` | `200 OK` | PASS |
| 21 | `E2E-EVT-03` — Listar eventos publicados | `GET /api/events/published` | `200 OK` | PASS |
| 22 | `E2E-EVT-06` — Buscar eventos por artista | `GET /api/events/by-artist?stageName={artistStageName}` | `200 OK` | PASS |
| 23 | `SETUP-EVT-DRAFT` — Crear borrador separado para escenarios negativos | `POST /api/events` | `201 Created` | PASS |

**Resultado de la sección:** se creó un evento en estado `DRAFT`, se asoció con un artista, se publicó y se verificó mediante las consultas correspondientes. También se creó un segundo evento que permaneció como borrador para las pruebas de autorización.

### 4.5. Compra y gestión de entradas

| N.º | Caso | Método y ruta | HTTP observado | Resultado |
|---:|---|---|---|---|
| 24 | `E2E-TKT-01` — Comprar ticket propio | `POST /api/tickets` | `201 Created` | PASS |
| 25 | `E2E-TKT-02` — Consultar ticket propio | `GET /api/tickets/{ticketCancelableCode}` | `200 OK` | PASS |
| 26 | `E2E-TKT-03` — Listar tickets propios | `GET /api/tickets/by-user?email={userAEmail}` | `200 OK` | PASS |
| 27 | `E2E-TKT-04` — Listar tickets pagados de un evento | `GET /api/events/{publishedEventCode}/tickets/paid` | `200 OK` | PASS |
| 28 | `E2E-TKT-05` — Cancelar ticket propio | `PATCH /api/tickets/{ticketCancelableCode}/cancel` | `200 OK` | PASS |
| 29 | `E2E-TKT-01B` — Comprar segundo ticket para poder marcarlo usado | `POST /api/tickets` | `201 Created` | PASS |
| 30 | `E2E-TKT-06` — Marcar como usado un ticket | `PATCH /api/tickets/{ticketUsableCode}/use` | `200 OK` | PASS |

**Resultado de la sección:** se comprobó la compra, consulta, listado, cancelación y uso de entradas. La primera entrada se canceló correctamente y la segunda pasó de `PAID` a `USED` tras la operación de personal.

### 4.6. Seguridad y escenarios negativos

| N.º | Caso | Método y ruta | HTTP observado | Resultado |
|---:|---|---|---|---|
| 31 | `E2E-USR-01B` — Registrar USER_B | `POST /api/users` | `201 Created` | PASS |
| 32 | `E2E-AUTH-01B` — Login de USER_B | `POST /api/auth/login` | `200 OK` | PASS |
| 33 | `TC-SEC-016` — Verificar que el rol inyectado no elevó a USER_B | `GET /api/auth/me` | `200 OK` | PASS |
| 34 | `TC-SEC-001` — Sin token no puede crear eventos | `POST /api/events` | `401 Unauthorized` | PASS |
| 35 | `TC-SEC-002` — USER no puede crear eventos | `POST /api/events` | `403 Forbidden` | PASS |
| 36 | `TC-SEC-003` — USER no puede publicar | `PATCH /api/events/{draftEventCode}/publish` | `403 Forbidden` | PASS |
| 37 | `TC-SEC-004` — USER no puede asociar artista | `POST /api/events/{draftEventCode}/artists/{artistId}` | `403 Forbidden` | PASS |
| 38 | `TC-SEC-005` — Visitante no puede ver borrador | `GET /api/events/{draftEventCode}` | `404 Not Found` | PASS |
| 39 | `TC-SEC-006` — USER_B no puede consultar datos de USER_A por email | `GET /api/users/by-email?email={userAEmail}` | `403 Forbidden` | PASS |
| 40 | `TC-SEC-007` — USER_B no puede consultar datos de USER_A por username | `GET /api/users/by-username?username={userAUsername}` | `403 Forbidden` | PASS |
| 41 | `TC-SEC-008` — Compra con email ajeno es denegada | `POST /api/tickets` | `403 Forbidden` | PASS |
| 42 | `TC-SEC-009` — USER_B no puede leer ticket de USER_A | `GET /api/tickets/{ticketCancelableCode}` | `403 Forbidden` | PASS |
| 43 | `TC-SEC-010` — USER_B no puede listar tickets de USER_A | `GET /api/tickets/by-user?email={userAEmail}` | `403 Forbidden` | PASS |
| 44 | `TC-SEC-011` — USER_B no puede cancelar ticket ajeno | `PATCH /api/tickets/{ticketCancelableCode}/cancel` | `403 Forbidden` | PASS |
| 45 | `TC-SEC-012` — USER normal no puede usar ticket | `PATCH /api/tickets/{ticketUsableCode}/use` | `403 Forbidden` | PASS |
| 46 | `TC-AUTH-004` — Login con contraseña incorrecta | `POST /api/auth/login` | `401 Unauthorized` | PASS |
| 47 | `TC-AUTH-007` — `/auth/me` sin token | `GET /api/auth/me` | `401 Unauthorized` | PASS |
| 48 | `TC-AUTH-008` — Token Bearer malformado | `GET /api/auth/me` | `401 Unauthorized` | PASS |
| 49 | `TC-AUTH-013` — Token de usuario desactivado deja de funcionar | `GET /api/auth/me` | `401 Unauthorized` | PASS |
| 50 | `TC-SEC-013` — USER no puede listar tickets pagados del evento | `GET /api/events/{publishedEventCode}/tickets/paid` | `403 Forbidden` | PASS |
| 51 | `TC-SEC-014` — Anónimo no puede comprar ticket | `POST /api/tickets` | `401 Unauthorized` | PASS |
| 52 | `TC-SEC-015` — ADMIN puede consultar borrador | `GET /api/events/{draftEventCode}` | `200 OK` | PASS |
| 53 | `TC-AUTH-009` — JWT manipulado al cambiar un carácter | `GET /api/auth/me` | `401 Unauthorized` | PASS |
| 54 | `TC-AUTH-010` — JWT expirado | `GET /api/auth/me` | `401 Unauthorized` | PASS |
| 55 | `TC-AUTH-011` — JWT de otra clave o issuer | `GET /api/auth/me` | `401 Unauthorized` | PASS |
| 56 | `TC-AUTH-012` — Token enviado por query string no autentica | `GET /api/auth/me?access_token={userAToken}` | `401 Unauthorized` | PASS |
| 57 | `TC-AUTH-015` — Bearer inválido en ruta pública | `GET /api/venues/active` | `401 Unauthorized` | PASS |
| 58 | `TC-SEC-017` — Revocar rol STAFF con token previo | `GET /api/events/{publishedEventCode}/tickets/paid` | `403 Forbidden` | PASS |

**Resultado de la sección:** se comprobaron las restricciones de acceso, la protección de los datos de otros usuarios, el control de permisos por rol y el rechazo de solicitudes sin autenticación o con tokens no aceptados.

La prueba `TC-SEC-016` devolvió `200 OK` para la consulta autenticada, mientras que el usuario conservó el rol `USER`, sin obtener privilegios de `ADMIN` por enviar el campo `role` durante el registro.

En `TC-SEC-017` se revocó temporalmente el rol `STAFF`; la operación protegida fue rechazada con `403 Forbidden`.

### 4.7. Validaciones HTTP y conflictos de negocio

| N.º | Caso | Método y ruta | HTTP observado | Resultado |
|---:|---|---|---|---|
| 59 | `TC-HTTP-400-001` — Email inválido en registro | `POST /api/users` | `400 Bad Request` | PASS |
| 60 | `TC-HTTP-400-002` — Request de evento inválido | `POST /api/events` | `400 Bad Request` | PASS |
| 61 | `TC-BIZ-409-001` — Duplicado de username/email | `POST /api/users` | `409 Conflict` | PASS |
| 62 | `TC-BIZ-409-002` — Publicar evento ya publicado | `PATCH /api/events/{publishedEventCode}/publish` | `409 Conflict` | PASS |
| 63 | `TC-BIZ-409-003` — Cancelar ticket ya cancelado | `PATCH /api/tickets/{ticketCancelableCode}/cancel` | `409 Conflict` | PASS |
| 64 | `TC-BIZ-409-004` — Usar ticket ya usado | `PATCH /api/tickets/{ticketUsableCode}/use` | `409 Conflict` | PASS |

**Resultado de la sección:** las solicitudes con datos inválidos fueron rechazadas con `400 Bad Request`, y las operaciones que entraban en conflicto con el estado existente devolvieron `409 Conflict`.

En `TC-BIZ-409-004`, el primer intento devolvió `403 Forbidden` debido a que el rol `STAFF` se había revocado en una prueba anterior. Tras restaurarlo, la ejecución se repitió y la API devolvió `409 Conflict`, que era el resultado esperado para un ticket que ya estaba usado.

---

## 5. Incidencias y ajustes realizados

Durante la ejecución manual se realizaron los siguientes ajustes en el entorno local:

1. Se inició PostgreSQL mediante Docker y se verificó la conexión de Spring Boot.
2. Se cargaron los datos de prueba de sedes y artistas.
3. Se completaron las variables requeridas en el entorno de Postman.
4. Se registraron las cuentas necesarias para los escenarios de usuario, administrador y personal.
5. Se asignaron manualmente los roles `ADMIN` y `STAFF` a las cuentas de preparación en la base de datos local.
6. Se renovaron los tokens de autenticación cuando fue necesario.
7. Se prepararon los códigos de evento y de entrada utilizados en las solicitudes posteriores.
8. Se creó un segundo ticket para comprobar de forma independiente el estado `USED`.
9. Se revocó el rol `STAFF` para verificar los permisos después de la revocación.
10. Se restauró ese rol al terminar la prueba para poder completar los conflictos de negocio restantes.

Estos ajustes permitieron ejecutar las solicitudes que dependían de datos previos y comprobar los flujos positivos y negativos de la API.

---

## 6. Resultado final de la ejecución

| Indicador | Total |
|---|---:|
| Solicitudes registradas en la colección | 64 |
| Solicitudes ejecutadas | 64 |
| Solicitudes PASS | 64 |
| Solicitudes FAIL | 0 |
| Solicitudes pendientes | 0 |
| Porcentaje de aprobación | 100 % |

### Conclusión

La colección **PulsePass — E2E Manual** se completó con las 64 solicitudes ejecutadas y un resultado satisfactorio en todos los casos.

Las pruebas cubrieron la preparación de actores, las consultas de sedes y artistas, el registro y la autenticación de usuarios, la creación y publicación de eventos, la compra y gestión de entradas, la autorización basada en roles, el rechazo de accesos no permitidos, la validación de datos y los conflictos de negocio.

Los casos negativos también se consideran aprobados porque la API devolvió el código de rechazo definido para cada escenario.

**Resultado global: 64/64 PASS — 100 % de aprobación.**

No se incluyen valores de contraseñas, tokens ni secretos en esta bitácora.