# PulsePass — Pruebas end-to-end manuales con Postman

## Objetivo y alcance

Ejecutar manualmente, mediante Postman, las 20 rutas de negocio de PulsePass más `POST /api/auth/login` y `GET /api/auth/me`. La prueba recorre la aplicación real: filtros de seguridad → Controllers → Services → repositorios → PostgreSQL. No hay mocks en este flujo.

**Restricción del PRD:** cada solicitud se envía manualmente con **Send**. No uses Pre-request Scripts, Post-response/Tests, Collection Runner, Newman, Postman CLI ni `curl` como sustituto de la evidencia manual.

## Archivos entregados

- `postman/PulsePass-Manual-E2E.postman_collection.json`: colección importable v2.1, organizada por módulo.
- `postman/PulsePass-local.postman_environment.json`: plantilla local sin contraseñas ni tokens reales.
- `fixtures/README.md` y `fixtures/seed-e2e.sql`: preparación segura de venue/artista y actores de prueba.
- `checklist-e2e-manual.md`: precondiciones y orden de ejecución.
- `registro-ejecucion-manual.md`: bitácora que debe completarse después de ejecutar las peticiones.
- `matriz-cobertura.md`: relación entre rutas, permisos, casos y evidencia esperada.

## 1. Preparar la aplicación

1. Usa una base de datos PostgreSQL **local y exclusiva para pruebas**. Revisa en `src/main/resources/application.properties` URL, usuario y contraseña antes de iniciar. No ejecutes fixtures ni pruebas destructivas contra datos compartidos o de producción.
2. Comprueba que Java 21 y Maven funcionan, y que PostgreSQL está disponible.
3. Configura las variables de entorno JWT en PowerShell. La clave debe ser Base64 de al menos 32 bytes aleatorios y nunca debe ir al repositorio:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:PULSEPASS_JWT_ISSUER = "pulsepass-api"
.\mvnw.cmd spring-boot:run
```

Mantén la terminal abierta. Flyway debe aplicar V1–V4 y Hibernate validar el esquema. Si arranca con un error, registra los casos E2E como `BLOCKED` hasta corregir la precondición.

4. En la carpeta `00 Preparación`, registra cuentas ADMIN, STAFF e INACTIVE_USER mediante peticiones públicas (todas empiezan con rol USER). Usa el procedimiento SQL de `fixtures/README.md` para asignar ADMIN/STAFF o desactivar INACTIVE_USER, exclusivamente en la base de prueba. Luego ejecuta sus requests de login y copia manualmente los tokens. Nunca eleves roles por el cuerpo HTTP.

## 2. Importar la colección y el ambiente

En Postman: **Import** → selecciona `PulsePass-Manual-E2E.postman_collection.json` y `PulsePass-local.postman_environment.json`. Elige el ambiente `PulsePass local (plantilla sin secretos)` y confirma `baseUrl=http://localhost:8080` o la URL real del servidor de pruebas.

Cambia `runId` antes de cada corrida y edita username/email de USER_A/USER_B para que sean únicos (usa el dominio reservado `example.test`). Completa las passwords solo en el ambiente local; los valores sensibles permanecen vacíos en el JSON exportable. No compartas capturas con tokens visibles.

## 3. Cómo encadenar los pasos manualmente

1. Envía `POST /api/users` para registrar USER_A; revisa `201`, campos de `UserResponse` y ausencia de credenciales.
2. Envía `POST /api/auth/login`. Copia manualmente `accessToken` a la variable local `userAToken` (no lo escribas en la colección ni en un commit). Repite para USER_B, STAFF y ADMIN después de provisionarlos.
3. Envía `GET /api/auth/me` con los tokens USER_A, USER_B, STAFF y ADMIN y compara id, username y roles reales (el DTO /me no incluye email). Para INACTIVE_USER, captura primero un token, desactiva la cuenta en la base de prueba y confirma que ese token deja de funcionar.
4. Envía las consultas públicas; copia los IDs/códigos reales a `venueActiveCode`, `artistId` y `artistStageName`.
5. Como ADMIN, crea un evento con `newEventCode`, asocia el artista y publícalo. Solo después de comprobar que quedó `PUBLISHED`, copia el código a `publishedEventCode`.
6. Como USER_A, compra ticket; copia el código devuelto a `ticketCancelableCode`. Ejecuta una segunda compra independiente y copia el código a `ticketUsableCode`. No reutilices tickets entre cancelación y uso.
7. Registra USER_B y captura `userBToken`. Ejecuta los escenarios negativos de la carpeta de seguridad. Antes y después de una denegación sobre recursos mutables, consulta el estado con el propietario o un operador autorizado y confirma que no hubo mutación.
8. Para cada petición, inspecciona **Status**, `Content-Type`, Body JSON, campos DTO y, cuando aplique, una consulta GET posterior. Un 2xx por sí solo no es prueba suficiente.

## 4. Estados y criterios de aceptación

- `PASS`: la petición se ejecutó realmente; HTTP exacto, contenido JSON y efecto observable concuerdan con lo esperado.
- `FAIL`: el request se pudo ejecutar pero algo incumple el contrato.
- `BLOCKED`: falta un prerrequisito, fixture, token o endpoint requerido.
- `NOT RUN`: aún no se envió manualmente.

No marques un caso `PASS` por revisar solo el código fuente, por compilar, ni por ver un 2xx. La bitácora entregada parte deliberadamente en `NOT RUN` porque no se han ejecutado estas peticiones desde tu Postman local.

## 5. Decisiones de seguridad usadas en esta guía

- Un visitante/USER que solicita un evento no publicado recibe `404`, igual que ante un evento no existente, para no revelar borradores.
- Los accesos de un USER a datos/tickets de otro usuario reciben `403` según la política elegida para este proyecto.
- `401` identifica ausencia/invalidación de autenticación; `403` identifica falta de permisos; `400` corresponde a entrada inválida; `404` a recurso inexistente u oculto; `409` a conflictos/reglas de negocio.
- STAFF es global para las operaciones operativas descritas en el MVP; el dominio no asigna STAFF a eventos individuales. Documenta esto como limitación académica y no como control de producción.

## 6. Repetibilidad y secretos

Usa códigos y usuarios únicos por corrida. Si debes restablecer datos, hazlo únicamente en una base de pruebas descartable, verifica `current_database()` y conserva el SQL usado como evidencia. No ejecutes `DROP`, `TRUNCATE` ni borrados masivos en una base no confirmada. Los tokens vencen a los 15 minutos por defecto: haz login de nuevo y reemplaza manualmente la variable local.

## Inicio rápido para Windows (sin modificar el backend)

1. En VS Code, abre la carpeta raíz del proyecto `PulsePass` (donde están `pom.xml` y `mvnw.cmd`).
2. En Postman importa la colección y el ambiente incluidos en esta carpeta; selecciona el ambiente local. Completa `runId`, los usuarios y las contraseñas ficticias.
3. Antes de iniciar la aplicación, confirma que la URL de PostgreSQL apunta a una base local exclusiva para E2E. El archivo `application.properties` trae `pulsepass`; no ejecutes estas pruebas sobre una base compartida o con datos que necesites conservar.
4. Define el secreto JWT en PowerShell como se muestra arriba y ejecuta `.\mvnw.cmd spring-boot:run`. Mantén esa terminal abierta.
5. En otra terminal o cliente SQL, ejecuta `fixtures/seed-e2e.sql` únicamente en la base E2E. Para STAFF/ADMIN sigue `fixtures/README.md`; el registro público nunca debe asignar esos roles.
6. Envía las peticiones una por una con **Send**. No uses Runner ni scripts. Copia los tokens solo a variables locales de Postman y completa la bitácora con el resultado real.

La respuesta real de `GET /api/auth/me` contiene `id`, `username` y `roles`, no email. Los ejemplos deben compararse con los DTO del proyecto.
