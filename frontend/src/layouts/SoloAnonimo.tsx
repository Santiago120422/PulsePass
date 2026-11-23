import { Navigate, Outlet, useLocation } from 'react-router'
import { useSession } from '../funcionalidades/auth/AuthContext'
import { loginNavigation, RUTAS } from '../lib/rutas'

export function SoloAnonimo() {
  const { session } = useSession()
  const location = useLocation()
  if (session.kind === 'ready') return <Navigate to={loginNavigation.safeParse(location.state).data?.desde ?? RUTAS.inicio} replace />
  if (session.kind === 'loading') return <p role="status">Restaurando sesión…</p>
  return <Outlet />
}
