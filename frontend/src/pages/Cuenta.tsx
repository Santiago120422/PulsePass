import { QueryView } from '../componentes/EstadoConsulta'
import { Page } from '../componentes/Pagina'
import { useSession } from '../funcionalidades/auth/AuthContext'
import { useProfile, useProfileByEmail } from '../funcionalidades/usuarios/hooks'
import { date } from '../lib/formato'

export function Account() {
  const { session } = useSession()
  const profile = useProfile()
  const email = profile.data?.email ?? ''
  const user = useProfileByEmail(email)
  return <Page title="Mi cuenta">
    <QueryView query={profile}>{() => <QueryView query={user}>{data =>
      <dl className="card data-list">
        {(data.firstName || data.lastName) && <div><dt>Nombre</dt><dd>{[data.firstName, data.lastName].filter(Boolean).join(' ')}</dd></div>}
        <div><dt>Usuario</dt><dd>{data.username}</dd></div><div><dt>Correo</dt><dd>{data.email}</dd></div>
        {data.birthDate && <div><dt>Nacimiento</dt><dd>{date(data.birthDate)}</dd></div>}
        {data.phone && <div><dt>Teléfono</dt><dd>{data.phone}</dd></div>}
        {data.city && <div><dt>Ciudad</dt><dd>{data.city}</dd></div>}
        {session.kind === 'ready' && <div><dt>Roles</dt><dd>{session.principal.roles.join(', ')}</dd></div>}
      </dl>}</QueryView>}</QueryView>
  </Page>
}
