import { Link, useParams } from 'react-router'
import { QueryView } from '../componentes/EstadoConsulta'
import { Page } from '../componentes/Pagina'
import { Missing } from '../componentes/RecursoNoDisponible'
import { useArtist } from '../funcionalidades/artistas/hooks'
import { EventGrid } from '../funcionalidades/eventos/componentes/EventGrid'
import { useEvents } from '../funcionalidades/eventos/hooks'
import { RUTAS } from '../lib/rutas'
import { positiveId } from '../lib/validacion'

export function ArtistDetail() {
  const id = positiveId.safeParse(useParams().id)
  const artist = useArtist(id.data)
  if (!id.success) return <Missing />
  return <QueryView query={artist} unavailable="Artista no encontrado.">{data => <Page title={data.stageName} description={[data.genre, data.country].filter(Boolean).join(' · ')}>
    <h2>Eventos asociados</h2><ArtistEvents name={data.stageName} />
    <Link className="text-link" to={{ pathname: RUTAS.eventos, search: new URLSearchParams({ artista: data.stageName }).toString() }}>Ver en la cartelera</Link>
  </Page>}</QueryView>
}

function ArtistEvents({ name }: { name: string }) {
  return <QueryView query={useEvents(name)}>{events => <EventGrid events={events} />}</QueryView>
}
