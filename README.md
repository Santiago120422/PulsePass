# PulsePass — Persistencia con Spring Boot + PostgreSQL

Caso de estudio académico enfocado en modelado de dominio, persistencia JPA,
migraciones versionadas con Flyway y pruebas de integración reales contra
PostgreSQL usando Testcontainers.

## Stack técnico

- Java 21
- Spring Boot 4.1.1
- Spring Data JPA (Hibernate)
- PostgreSQL
- Flyway
- Testcontainers + JUnit 5
- Lombok
- Maven

## Modelo de dominio
Venue 1 ---- N Event
Event N ---- M Artist
User 1 ---- 1 UserProfile
User 1 ---- N Ticket
Event 1 ---- N Ticket


| Entidad     | Atributos principales                                                     |
|-------------|-----------------------------------------------------------------------------|
| Venue       | code, name, city, address, capacity, active                                 |
| Event       | eventCode, name, description, category, status, eventDate, minimumAge, streamingUrl, venue |
| Artist      | stageName, country, genre, active                                           |
| User        | username, email, active                                                     |
| UserProfile | firstName, lastName, phone, city, birthDate, user                           |
| Ticket      | ticketCode, type, price, status, purchaseDate, user, event                  |

### Reglas de negocio clave

- `Ticket` es una entidad propia (no un `@ManyToMany` simple entre `User` y
  `Event`), porque contiene datos propios: código, tipo, precio, estado y
  fecha de compra.
- Los enums (`EventCategory`, `EventStatus`, `TicketType`, `TicketStatus`) se
  persisten con `@Enumerated(EnumType.STRING)`, nunca por ordinal, para evitar
  que un reordenamiento del enum corrompa datos existentes.
- Los precios se modelan con `BigDecimal`, nunca `float`/`double`.
- Los identificadores de negocio (`code`, `eventCode`, `ticketCode`,
  `username`, `email`, `stageName`) están protegidos con `UNIQUE` a nivel de
  PostgreSQL, no solo a nivel de aplicación.

## Migraciones (Flyway)

Ubicadas en `src/main/resources/db/migration/`:

| Migración | Contenido |
|---|---|
| `V1__create_core_tables.sql` | `venues`, `artists`, `users`, `user_profiles` |
| `V2__create_events_and_relations.sql` | `events`, `event_artists` (N:M), `tickets` |
| `V3__add_streaming_url_to_events.sql` | Agrega columna opcional `streaming_url` a `events` |

Hibernate corre en modo `ddl-auto=validate`: **nunca crea ni modifica el
esquema**, solo valida que las entidades JPA coincidan exactamente con lo que
Flyway ya construyó. Todo cambio de esquema debe hacerse siempre mediante una
nueva migración versionada, nunca editando una migración ya aplicada.

## Repositories y consultas

| Repository | Consultas destacadas |
|---|---|
| `VenueRepository` | `findByCode` |
| `EventRepository` | `findByEventCode`, `findByStatusOrderByEventDateAsc`, `findByVenue_Code`, búsquedas JPQL por artista/ciudad/fecha |
| `ArtistRepository` | `findByStageName` |
| `UserRepository` | `findByUsername`, `findByEmail` |
| `TicketRepository` | `findByUser_Email(AndStatus)`, `findByEvent_EventCodeAndStatus`, `countByEvent_EventCodeAndStatus`, tickets de eventos futuros |

Se usan tanto **Query Methods** (derivados del nombre del método) como
**JPQL explícito** (`@Query`) para los casos que requieren `JOIN`, `DISTINCT`
o filtros case-insensitive.

## Pruebas de integración

Todas las pruebas extienden `AbstractIntegrationTest`, que levanta un
contenedor real de PostgreSQL vía Testcontainers antes de cada clase de test
y aplica las migraciones Flyway sobre él. Cada test corre dentro de una
transacción con rollback automático (`@Transactional`), así que los datos no
persisten entre pruebas.

| Clase de test | Cubre |
|---|---|
| `repository/VenueRepositoryIT` | FR-VEN-* |
| `repository/EventRepositoryIT` | FR-EVT-* |
| `repository/ArtistRepositoryIT` | FR-ART-* |
| `repository/UserRepositoryIT` | FR-USR-*, BR-004 |
| `repository/TicketRepositoryIT` | FR-TKT-* |
| `repository/EventSearchIT` | FR-SRC-* |
| `FlywayMigrationIT` | NFR-002, NFR-003 (Flyway + Hibernate validate) |

### Requisitos para correr las pruebas

- **Docker Desktop** debe estar corriendo (Testcontainers lo necesita para
  levantar el contenedor de PostgreSQL).

### Cómo ejecutar

```bash
mvn clean test
```

Debe finalizar con `BUILD SUCCESS`.

## Cómo correr la aplicación localmente (opcional)

1. Levanta una instancia de PostgreSQL local o vía Docker.
2. Ajusta `src/main/resources/application.properties` con tus credenciales.
3. Ejecuta:
```bash
   mvn spring-boot:run
```
4. Flyway aplicará automáticamente V1, V2 y V3 al arrancar.

## Fuera de alcance (según el PRD)

Este proyecto es exclusivamente un caso de estudio de persistencia. No
incluye: autenticación/autorización, pasarela de pagos, notificaciones,
frontend, API REST/capa Service, ni inventario concurrente de entradas.

Proyecto desarrollado como parte del taller de persistencia.

## Capa de servicios

Frontera entre las futuras capas de exposición y el modelo persistente
(`com.pulsepass.pulsepass.service`). Cada servicio tiene interfaz + implementación,
inyección por constructor, DTOs `record` y mapeo Entity → DTO con MapStruct.

| Servicio | Responsabilidad |
|---|---|
| `VenueService` | Consulta de venues (por código, activos) |
| `ArtistService` | Consulta de artistas (id, nombre artístico, activos) |
| `EventService` | Crear (DRAFT), publicar, asociar artistas, consultas |
| `UserService` | Registro User + UserProfile, consultas |
| `TicketService` | Compra atómica, cancelación, uso, consultas |

Decisiones de diseño relevantes:

- Excepciones: `ResourceNotFoundException`, `DuplicateResourceException`, `BusinessRuleException`.
- Precio: lo calcula `PricingStrategy` (`DefaultPricingStrategy`), nunca el cliente. Precio base
  configurable con `pulsepass.pricing.base-price` (GENERAL x1, STUDENT x0.70, VIP x2, BACKSTAGE x3.5).
- Tiempo: se inyecta un `Clock` para poder fijar "ahora" en los tests.
- Capacidad: ocupan asiento los tickets `PAID` **y** `USED`.
- Cancelar un ticket de un evento `SOLD_OUT` lo devuelve a `PUBLISHED` (cupo liberado).
- Los emails se guardan normalizados en minúsculas.

Los unit tests (`*Test`) usan JUnit 5 + Mockito + AssertJ, sin Spring ni PostgreSQL:
`mvn clean test`. Los tests de repositorio (`*IT`) siguen requiriendo Docker.

## Fase 3 — Capa de Controladores REST

API REST sobre los Services existentes. Los Controllers son delgados: validan la
entrada (Bean Validation), delegan en el Service y devuelven `ResponseEntity<DTO>`.
Los errores se traducen en un único `GlobalExceptionHandler` (`@RestControllerAdvice`)
y siempre usan el contrato `ErrorResponse`.

### Endpoints principales

| Método | Endpoint | Éxito |
|---|---|---|
| GET | `/api/venues/{code}` | 200 |
| GET | `/api/venues/active` | 200 |
| POST | `/api/events` | 201 |
| GET | `/api/events/{eventCode}` | 200 |
| GET | `/api/events/published` | 200 |
| PATCH | `/api/events/{eventCode}/publish` | 200 |
| POST | `/api/events/{eventCode}/artists/{artistId}` | 200 |
| GET | `/api/events/by-artist?stageName=...` | 200 |
| GET | `/api/artists/{id}` | 200 |
| GET | `/api/artists/by-stage-name?stageName=...` | 200 |
| GET | `/api/artists/active` | 200 |
| POST | `/api/users` | 201 |
| GET | `/api/users/by-email?email=...` | 200 |
| GET | `/api/users/by-username?username=...` | 200 |
| POST | `/api/tickets` | 201 |
| GET | `/api/tickets/{ticketCode}` | 200 |
| GET | `/api/tickets/by-user?email=...` | 200 |
| GET | `/api/events/{eventCode}/tickets/paid` | 200 |
| PATCH | `/api/tickets/{ticketCode}/cancel` | 200 |
| PATCH | `/api/tickets/{ticketCode}/use` | 200 |

### Errores

| Condición | HTTP |
|---|---|
| Bean Validation / JSON mal formado | 400 |
| `ResourceNotFoundException` | 404 |
| `DuplicateResourceException` / `BusinessRuleException` | 409 |
| Error inesperado | 500 |

### Pruebas de Controller

Usan `@WebMvcTest`, `MockMvc` y `@MockitoBean` (los Services son mocks), por lo
que no necesitan PostgreSQL ni Docker:

```bash
mvn clean test -Dtest='*ControllerTest'
```

---

# Fase 4 — Seguridad (Spring Security + JJWT)

API **stateless** con JWT firmado (HS256), tres roles (`USER`, `STAFF`, `ADMIN`) y control de propiedad de recursos.

## Puesta en marcha

1. **Base de datos limpia.** La migración `V4__security_credentials_roles.sql` fue reescrita. Si tu BD local ya
   aplicó una V4 anterior, Flyway fallará con *checksum mismatch*. Recréala:
   ```sql
   DROP SCHEMA public CASCADE; CREATE SCHEMA public;
   ```
2. **Clave JWT** (obligatoria; la app NO arranca sin ella). Base64 de al menos 32 bytes aleatorios:
   ```bash
   # Linux / macOS / Git Bash
   export PULSEPASS_JWT_SECRET_BASE64=$(openssl rand -base64 32)
   ```
   ```powershell
   # PowerShell
   $env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
   ```
   Opcional: `PULSEPASS_JWT_ISSUER` (por defecto `pulsepass-api`). Plantilla en `.env.example`. **Nunca** subas la clave al repo.
3. **Arrancar** con el perfil `dev` (crea las cuentas semilla):
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

## Cuentas de prueba (solo perfil `dev`)

| Usuario | Rol | Contraseña (ficticia) |
|---|---|---|
| `admin` | ADMIN | `DemoPulsePass2026!` |
| `laura`, `miguel` | STAFF | `DemoPulsePass2026!` |

El registro público (`POST /api/users`) **solo** crea cuentas `USER`. Los roles privilegiados nunca se asignan por HTTP.

## Endpoints de autenticación

| Verbo | Ruta | Acceso |
|---|---|---|
| POST | `/api/users` | Público — body igual que antes **+ `password`** (12 a 72 caracteres) |
| POST | `/api/auth/login` | Público — `{"identifier": "usuario o correo", "password": "..."}` → `{accessToken, tokenType, expiresIn: 900}` |
| GET | `/api/auth/me` | Bearer |

```bash
curl -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
     -d '{"identifier":"admin","password":"DemoPulsePass2026!"}'
curl localhost:8080/api/auth/me -H "Authorization: Bearer <TOKEN>"
```

## Matriz de autorización

| Operación | Regla |
|---|---|
| `GET /api/venues/**`, `/api/artists/**`, `/api/events/published` | Público |
| `GET /api/events/{code}`, `/api/events/by-artist` | Público **solo eventos PUBLISHED**; borradores → 404 salvo STAFF/ADMIN |
| `POST /api/events`, `PATCH /api/events/*/publish`, `POST /api/events/*/artists/*` | ADMIN |
| `PATCH /api/tickets/*/use`, `GET /api/events/*/tickets/paid` | STAFF / ADMIN |
| `POST /api/tickets` | USER, y `userEmail` debe ser el del principal |
| `GET /api/tickets/{code}` | Propietario, o STAFF/ADMIN |
| `PATCH /api/tickets/*/cancel` | Propietario, o ADMIN |
| `GET /api/tickets/by-user`, `/api/users/by-email`, `/api/users/by-username` | Propio usuario, o ADMIN |

Respuestas: `401` sin identidad válida, `403` sin permiso (ticket ajeno o inexistente → 403, política única),
todas con el contrato `ErrorResponse` de 6 campos.

## Arquitectura

- `SecurityConfig`: cadena de filtros y reglas de URL. `JwtAuthenticationFilter` (no es `@Component`, se instancia en la config).
- `JwtService`: firma/valida (HS256, issuer, exp, sub, iat, jti). El token solo lleva el **ID persistente**; estado y roles se leen de la BD en **cada** request.
- `CustomUserDetailsService`: usuario + hash (`user_credentials`) + roles (`user_roles`) + estado `active`.
- `AuthService`: registro atómico (User + perfil + hash BCrypt + `ROLE_USER`).
- `AuthorizationService` (bean `authz`): propiedad anti-IDOR, usada desde `@PreAuthorize` en `TicketService` y `UserService`.
- `RestAuthenticationEntryPoint` / `RestAccessDeniedHandler`: 401/403 en JSON; `GlobalExceptionHandler` también traduce `AccessDeniedException` y `AuthenticationException`.

## Verificación

```bash
./mvnw clean test      # unitarias + MockMvc con filtros reales + integración (requiere Docker)
```

Escenario manual: registrar a Andrea → login → intentar `GET /api/users/by-email?email=<otro>` (403) → intentar publicar un evento (403) → login como `admin` y publicar → alterar un carácter del JWT (401) → desactivar el usuario en BD y repetir el request (401).

## Limitaciones conocidas (MVP académico, no listo para producción)

- Sin refresh tokens, sin revocación por `jti` (el logout no invalida tokens ya emitidos; vida corta de 15 min).
- STAFF es global: el dominio aún no asigna personal a eventos.
- Sin límite de intentos de login, sin rotación de claves, sin MFA ni recuperación de contraseña.
- Usuarios creados antes de la Fase 4 no tienen credenciales y no pueden iniciar sesión hasta una activación segura.
- HTTPS obligatorio fuera de localhost.

# Autores
## Santiago Pacheco Ossío
## Hewitt Daniel Rocha Navarro

