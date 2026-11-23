import { Link } from 'react-router'
import { QueryView } from '../componentes/EstadoConsulta'
import { Empty } from '../componentes/EstadoVacio'
import { Page } from '../componentes/Pagina'
import { useVenues } from '../funcionalidades/sedes/hooks'
import { RUTAS } from '../lib/rutas'

export function Venues() {
  const venues = useVenues()
  return <Page title="Sedes" description="Explora los espacios donde se realizan nuestros eventos.">
    <QueryView query={venues}>{items => !items.length ? <Empty>No hay sedes activas.</Empty> : <div className="tile-grid">{items.map(item =>
      <article className="card" key={item.code}><h2><Link to={RUTAS.sede(item.code)}>{item.name}</Link></h2><p>{item.city}</p><p className="muted">Capacidad: {item.capacity}</p></article>)}</div>}</QueryView>
  </Page>
}
