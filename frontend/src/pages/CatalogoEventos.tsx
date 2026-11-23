import { useSearchParams } from 'react-router'
import { z } from 'zod'
import { Field } from '../componentes/Campo'
import { QueryView } from '../componentes/EstadoConsulta'
import { Page } from '../componentes/Pagina'
import { useArtists } from '../funcionalidades/artistas/hooks'
import { EventGrid } from '../funcionalidades/eventos/componentes/EventGrid'
import { useEvents } from '../funcionalidades/eventos/hooks'
import { category } from '../funcionalidades/eventos/tipos'
import { labels } from '../lib/formato'

const filters = z.object({ q: z.string().max(200).catch(''), categoria: category.or(z.literal('')).catch(''), artista: z.string().max(200).catch('') })
const normalized = (value: string) => value.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLocaleLowerCase('es-CO')

export function Catalog() {
  const [params, setParams] = useSearchParams()
  const { q, categoria, artista } = filters.parse(Object.fromEntries(params))
  const events = useEvents(artista)
  const artists = useArtists()
  function change(key: string, value: string) {
    const next = new URLSearchParams(params)
    if (value) next.set(key, value)
    else next.delete(key)
    setParams(next, { replace: true })
  }
  return <Page title={artista ? 'Eventos de ' + artista : 'Cartelera'} description="Encuentra tu próximo evento.">
    <div className="lookup">
      <Field label="Buscar eventos">{id => <input id={id} value={q} maxLength={200} onChange={event => change('q', event.target.value)} type="search" />}</Field>
      <Field label="Categoría">{id => <select id={id} value={categoria} onChange={event => change('categoria', event.target.value)}>
        <option value="">Todas las categorías</option>{category.options.map(value => <option key={value} value={value}>{labels[value]}</option>)}</select>}</Field>
      <Field label="Artista">{id => <select id={id} value={artista} onChange={event => change('artista', event.target.value)}>
        <option value="">Todos los artistas</option>
        {artista && !artists.data?.some(item => item.stageName === artista) && <option value={artista}>{artista}</option>}
        {artists.data?.map(item => <option key={item.id} value={item.stageName}>{item.stageName}</option>)}
      </select>}</Field>
      <button className="secondary" type="button" disabled={!q && !categoria && !artista} onClick={() => setParams({}, { replace: true })}>Limpiar</button>
    </div>
    <QueryView query={events}>{items => <EventGrid events={items.filter(event =>
      (!categoria || event.category === categoria) && normalized(event.name + ' ' + event.venueName).includes(normalized(q.trim())))} />}</QueryView>
  </Page>
}
