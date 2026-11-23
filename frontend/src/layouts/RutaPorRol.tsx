import type { Role } from '../funcionalidades/auth/tipos'
import { RutaProtegida } from './RutaProtegida'

export function RutaPorRol({ roles }: { roles: Role[] }) {
  return <RutaProtegida roles={roles} />
}
