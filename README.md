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