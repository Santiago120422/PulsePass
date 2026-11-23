import { Link, useNavigate } from 'react-router'
import { ErrorNote } from '../componentes/Aviso'
import { Lookup } from '../componentes/Busqueda'
import { QueryView } from '../componentes/EstadoConsulta'
import { Empty } from '../componentes/EstadoVacio'
import { Page } from '../componentes/Pagina'
import { useArtistLookup, useArtists } from '../funcionalidades/artistas/hooks'
import { RUTAS } from '../lib/rutas'

export function Artists() {
  const navigate = useNavigate()
  const artists = useArtists()
  const lookup = useArtistLookup(id => navigate(RUTAS.artista(id)))
  return <Page title="Artistas" description="Conoce a quienes hacen parte de la cartelera.">
    <Lookup label="Nombre artístico" code={false} submit={value => lookup.mutate(value)} />
    {lookup.error && <ErrorNote error={lookup.error} />}
    <QueryView query={artists}>{items => !items.length ? <Empty>No hay artistas activos.</Empty> : <div className="tile-grid">{items.map(item =>
      <article className="card" key={item.id}><h2><Link to={RUTAS.artista(item.id)}>{item.stageName}</Link></h2><p className="muted">{[item.genre, item.country].filter(Boolean).join(' · ')}</p></article>)}</div>}</QueryView>
  </Page>
}
