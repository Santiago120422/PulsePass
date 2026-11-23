# PulsePass Web

SPA en español de Colombia. Consume la API REST de Spring Boot mediante rutas del mismo origen.

## Estructura del código

- `src/main.tsx` monta los proveedores y `src/router.tsx` carga las páginas y aplica las guardas de `src/layouts/`.
- `src/pages/` contiene las pantallas correspondientes a las rutas. `src/componentes/` contiene la interfaz reutilizable.
- `src/funcionalidades/` agrupa cada dominio con su API, esquemas Zod y hooks de TanStack Query. Los componentes propios de una funcionalidad viven junto a ella.
- `src/lib/` contiene el cliente HTTP compartido, las rutas, el formato y la configuración de consultas. `src/test/` contiene las pruebas con API simulada.

## Desarrollo local

Usa Node.js 22.22 o posterior.

1. Levanta el backend desde la raíz del repositorio. La configuración de PostgreSQL y el perfil local están documentados en el README principal.
2. Desde este directorio ejecuta los comandos siguientes.

```powershell
npm ci
npm run dev
```

Abre la dirección que muestra Vite. El proxy usa `http://localhost:8080` por defecto. Para cambiar el destino copia `.env.example` a `.env.local` y ajusta `PULSEPASS_API_TARGET`.

## Recorridos por rol

| Acceso | Rutas |
| --- | --- |
| Público | `/`, `/eventos`, `/artistas`, `/sedes`, `/login`, `/registro` |
| Autenticado | `/cuenta` |
| USER | `/mis-tickets`, `/compra/confirmacion/:ticketCode` |
| STAFF o ADMIN | `/staff/validacion`, `/staff/eventos/:eventCode/tickets` |
| ADMIN | `/admin/eventos`, `/admin/eventos/nuevo`, `/admin/eventos/:eventCode` |

El registro crea cuentas USER. Para comprobar STAFF y ADMIN usa las cuentas del perfil `dev` que se describen en el README principal. El correo del comprador se obtiene del perfil propio consultado por el nombre que devuelve `/api/auth/me`.

## Verificación

```powershell
npm run lint
npm test
npm run build
```

Las pruebas con Vitest, Testing Library y MSW simulan la API. La compilación escribe en `dist/`. Para servirla localmente con el proxy de Vite, usa `npm run preview`.

