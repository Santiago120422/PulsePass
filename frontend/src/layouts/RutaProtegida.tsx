import { Navigate, Outlet, useLocation } from 'react-router'
import { QueryView } from '../componentes/EstadoConsulta'
import { type Role } from '../funcionalidades/auth/tipos'
import { RUTAS } from '../lib/rutas'

import { useSession } from '../funcionalidades/auth/AuthContext'

export function RutaProtegida({ roles }: { roles?: Role[] }) {
  const { session } = useSession()
  const location = useLocation()
  if (session.kind === 'anonymous') return <Navigate to={RUTAS.login} replace state={{ desde: location.pathname + location.search }} />
  if (session.kind === 'loading') return <p role="status">Restaurando sesión…</p>
  if (session.kind === 'failed') return <QueryView query={{ isPending: false, error: session.error, refetch: session.retry }}>{() => null}</QueryView>
  if (roles && !roles.some(role => session.principal.roles.includes(role))) return <Navigate to={RUTAS.prohibido} replace />
  return <Outlet />
}
