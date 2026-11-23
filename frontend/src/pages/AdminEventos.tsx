import { Link, useNavigate } from 'react-router'
import { Lookup } from '../componentes/Busqueda'
import { Empty } from '../componentes/EstadoVacio'
import { Page } from '../componentes/Pagina'
import { RUTAS } from '../lib/rutas'

import { readRecent } from '../funcionalidades/admin/recientes'
export function Admin() {
  const navigate = useNavigate()
  const recent = readRecent()
  return <Page title="Administración de eventos" description="Crea eventos o gestiona un evento existente por su código.">
    <Link className="primary" to={RUTAS.crear}>Crear evento</Link>
    <Lookup label="Código del evento" submit={code => navigate(RUTAS.gestionar(code))} />
    <h2>Eventos recientes</h2>
    {recent.length ? <ul className="card space-y-2">{recent.map(code => <li key={code}><Link className="text-link" to={RUTAS.gestionar(code)}>{code}</Link></li>)}</ul> : <Empty>Los eventos creados en esta pestaña aparecerán aquí.</Empty>}
  </Page>
}
