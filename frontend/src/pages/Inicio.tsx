import { Link } from 'react-router'
import { QueryView } from '../componentes/EstadoConsulta'
import { useArtists } from '../funcionalidades/artistas/hooks'
import { useSession } from '../funcionalidades/auth/AuthContext'
import { EventGrid } from '../funcionalidades/eventos/componentes/EventGrid'
import { useEvents } from '../funcionalidades/eventos/hooks'
import { RUTAS } from '../lib/rutas'

export function Inicio() {
  const { session } = useSession()
  const events = useEvents()
  const artists = useArtists()
  return <div className="home space-y-10">
    <section className="hero">
      <div className="container-shell"><div className="hero-content"><h1>Descubre los mejores eventos</h1>
        <p>Música, cultura, deporte y tecnología en un solo lugar.</p>
        <div className="flex flex-wrap gap-3"><Link className="primary" to={RUTAS.eventos}>Ver cartelera</Link>
          {session.kind === 'anonymous' && <Link className="secondary hero-secondary" to={RUTAS.registro}>Crear cuenta</Link>}</div>
      </div></div>
    </section>
    <section className="space-y-4"><div className="flex flex-wrap items-baseline justify-between gap-3"><h2>Próximos eventos</h2><Link className="text-link" to={RUTAS.eventos}>Ver cartelera</Link></div>
      <QueryView query={events}>{items => <EventGrid events={items.slice(0, 3)} />}</QueryView>
    </section>
    <section className="space-y-4">
      <div className="flex flex-wrap items-baseline justify-between gap-3"><h2>Artistas destacados</h2><Link className="text-link" to={RUTAS.artistas}>Ver artistas</Link></div>
      <QueryView query={artists}>{items => items.length ? <ul className="flex flex-wrap gap-4">{items.slice(0, 3).map(artist =>
        <li key={artist.id}><Link className="artist-link" to={RUTAS.artista(artist.id)}><span aria-hidden="true">{artist.stageName.split(/\s+/).slice(0, 2).map(word => word[0]).join('')}</span>{artist.stageName}</Link></li>)}</ul>
        : <p className="muted">No hay artistas activos.</p>}</QueryView>
    </section>
  </div>
}
