# Matriz de cobertura E2E — PulsePass

## 1. Información general

- **Proyecto:** PulsePass
- **Tipo:** Pruebas manuales de API REST
- **Herramienta:** Postman
- **Entorno:** Local
- **Fecha:** 19 de octubre de 2026
- **Total de solicitudes:** 64
- **Resultado global:** PASS

## 2. Matriz de cobertura funcional

| ID | Funcionalidad o requisito | Casos relacionados | Verificación realizada | Estado |
|---|---|---|---|---|
| COV-01 | Preparación del entorno y actores | SETUP-USR-ADMIN, SETUP-USR-STAFF, SETUP-USR-INACTIVE, SETUP-AUTH-ADMIN, SETUP-AUTH-STAFF, SETUP-AUTH-INACTIVE | Preparación y autenticación de las cuentas necesarias para los escenarios E2E | PASS |
| COV-02 | Consulta de sedes | E2E-VEN-01, E2E-VEN-02 | Consulta por código y listado de sedes activas | PASS |
| COV-03 | Consulta de artistas | E2E-ART-01, E2E-ART-02, E2E-ART-03 | Consulta por ID, búsqueda por nombre artístico y listado de artistas activos | PASS |
| COV-04 | Registro de usuarios | E2E-USR-01, E2E-USR-01B | Creación de cuentas de usuario | PASS |
| COV-05 | Inicio de sesión e identidad | E2E-AUTH-01, E2E-AUTH-02, E2E-AUTH-01B | Autenticación y consulta de identidad de usuarios | PASS |
| COV-06 | Consulta de datos propios | E2E-USR-02, E2E-USR-03 | Consulta del usuario por email y username | PASS |
| COV-07 | Creación y publicación de eventos | E2E-EVT-01, E2E-EVT-04, E2E-EVT-02, E2E-EVT-03 | Creación de borrador, publicación y consultas posteriores | PASS |
| COV-08 | Asociación de artistas con eventos | E2E-EVT-05, E2E-EVT-06 | Asociación de artista y búsqueda de eventos relacionados | PASS |
| COV-09 | Gestión de borradores | SETUP-EVT-DRAFT, TC-SEC-005, TC-SEC-015 | Creación de borrador, restricción de acceso público y acceso administrativo | PASS |
| COV-10 | Compra y consulta de entradas | E2E-TKT-01, E2E-TKT-02, E2E-TKT-03, E2E-TKT-04, E2E-TKT-01B | Compra, consulta propia, listado de entradas y consulta de entradas pagadas | PASS |
| COV-11 | Cancelación de entradas | E2E-TKT-05, TC-SEC-011, TC-BIZ-409-003 | Cancelación propia, denegación de cancelación ajena y conflicto por cancelación repetida | PASS |
| COV-12 | Uso de entradas | E2E-TKT-06, TC-SEC-012, TC-BIZ-409-004 | Marcado como usada, restricción por rol y conflicto por uso repetido | PASS |
| COV-13 | Autorización por roles | TC-SEC-001, TC-SEC-002, TC-SEC-003, TC-SEC-004, TC-SEC-013, TC-SEC-014, TC-SEC-015, TC-SEC-017 | Restricciones para visitantes y usuarios, acceso administrativo y revocación de rol | PASS |
| COV-14 | Protección de datos entre usuarios | TC-SEC-006, TC-SEC-007, TC-SEC-008, TC-SEC-009, TC-SEC-010, TC-SEC-011 | Restricción de consultas, compras y modificaciones sobre recursos ajenos | PASS |
| COV-15 | Prevención de elevación de privilegios | TC-SEC-016 | Verificación de que enviar un rol en el registro no otorgue automáticamente permisos de administrador | PASS |
| COV-16 | Credenciales incorrectas y autenticación ausente | TC-AUTH-004, TC-AUTH-007, TC-AUTH-014 | Rechazo de credenciales incorrectas y peticiones sin autenticación | PASS |
| COV-17 | Integridad y validez de tokens | TC-AUTH-008, TC-AUTH-009, TC-AUTH-010, TC-AUTH-011, TC-AUTH-012, TC-AUTH-013, TC-AUTH-015 | Rechazo de tokens inválidos, manipulados, expirados, no aceptados o pertenecientes a usuarios desactivados | PASS |
| COV-18 | Validación de datos de entrada | TC-HTTP-400-001, TC-HTTP-400-002 | Rechazo de email inválido y de solicitudes de evento incompletas | PASS |
| COV-19 | Conflictos de negocio | TC-BIZ-409-001, TC-BIZ-409-002, TC-BIZ-409-003, TC-BIZ-409-004 | Rechazo de duplicados y de operaciones incompatibles con el estado del recurso | PASS |

## 3. Matriz de cobertura de seguridad

| Requisito de seguridad | Casos de prueba | Resultado esperado | Cobertura |
|---|---|---|---|
| Una operación protegida no debe aceptar solicitudes sin token | TC-SEC-001, TC-SEC-014, TC-AUTH-007 | `401 Unauthorized` | Cubierto |
| Un usuario normal no debe ejecutar operaciones administrativas | TC-SEC-002, TC-SEC-003, TC-SEC-004 | `403 Forbidden` | Cubierto |
| Un usuario no debe acceder a información privada de otro usuario | TC-SEC-006, TC-SEC-007, TC-SEC-009, TC-SEC-010 | `403 Forbidden` | Cubierto |
| Un usuario no debe comprar o modificar recursos en nombre de otra persona | TC-SEC-008, TC-SEC-011 | `403 Forbidden` | Cubierto |
| Un usuario normal no debe utilizar funciones reservadas al personal | TC-SEC-012, TC-SEC-013 | `403 Forbidden` | Cubierto |
| Un visitante no debe consultar un evento borrador | TC-SEC-005 | `404 Not Found` | Cubierto |
| El administrador debe poder consultar un borrador | TC-SEC-015 | `200 OK` | Cubierto |
| El registro no debe permitir elevar privilegios mediante el campo de rol | TC-SEC-016 | Usuario conserva rol USER | Cubierto |
| La revocación de STAFF debe impedir la operación protegida | TC-SEC-017 | `403 Forbidden` | Cubierto |
| Las credenciales incorrectas deben rechazarse | TC-AUTH-004 | `401 Unauthorized` | Cubierto |
| Los tokens inválidos o no aceptados deben rechazarse | TC-AUTH-008, TC-AUTH-009, TC-AUTH-010, TC-AUTH-011, TC-AUTH-012, TC-AUTH-013, TC-AUTH-015 | `401 Unauthorized` | Cubierto |

## 4. Matriz de validaciones y conflictos

| Tipo de validación | Caso | Resultado esperado | Estado |
|---|---|---|---|
| Validación de formato de email | TC-HTTP-400-001 | `400 Bad Request` | PASS |
| Campos obligatorios ausentes | TC-HTTP-400-002 | `400 Bad Request` | PASS |
| Registro duplicado | TC-BIZ-409-001 | `409 Conflict` | PASS |
| Publicación repetida | TC-BIZ-409-002 | `409 Conflict` | PASS |
| Cancelación repetida | TC-BIZ-409-003 | `409 Conflict` | PASS |
| Uso repetido de una entrada | TC-BIZ-409-004 | `409 Conflict` | PASS |

## 5. Matriz de cobertura de estados de recursos

| Recurso | Estado inicial | Operación | Estado o resultado final | Estado |
|---|---|---|---|---|
| Evento principal | `DRAFT` | Publicar evento | `PUBLISHED` | PASS |
| Evento principal | `PUBLISHED` | Consultar por código | Datos del evento devueltos | PASS |
| Evento principal | `PUBLISHED` | Listar eventos publicados | Evento presente en la lista | PASS |
| Primera entrada | Sin entrada | Comprar | `PAID` | PASS |
| Primera entrada | `PAID` | Cancelar | `CANCELLED` | PASS |
| Primera entrada | `CANCELLED` | Cancelar nuevamente | `409 Conflict` | PASS |
| Segunda entrada | Sin entrada | Comprar | `PAID` | PASS |
| Segunda entrada | `PAID` | Marcar como usada | `USED` | PASS |
| Segunda entrada | `USED` | Marcar como usada nuevamente | `409 Conflict` | PASS |
| Evento independiente | Sin evento | Crear borrador | `DRAFT` | PASS |

## 6. Resumen de cobertura

| Indicador | Resultado |
|---|---:|
| Solicitudes E2E ejecutadas | 64 |
| Solicitudes aprobadas | 64 |
| Solicitudes fallidas | 0 |
| Cobertura de los casos registrados | 100 % |
| Áreas funcionales cubiertas | Consulta, usuarios, autenticación, eventos, entradas, seguridad, validaciones y conflictos |
| Estado final | COMPLETADO |

## 7. Conclusión

La matriz documenta la cobertura de los principales flujos funcionales, los controles de autorización, la validación de solicitudes y los conflictos de negocio de PulsePass.

Las pruebas positivas verificaron que las operaciones válidas se completaran correctamente. Las pruebas negativas verificaron que las solicitudes no autorizadas o incompatibles con el estado del recurso fueran rechazadas con el código HTTP previsto.

**Resultado de la ejecución manual: 64/64 solicitudes aprobadas.**