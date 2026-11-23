import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { z } from 'zod'
import { ErrorNote } from '../componentes/Aviso'
import { Badge } from '../componentes/Badge'
import { Field } from '../componentes/Campo'
import { QueryView } from '../componentes/EstadoConsulta'
import { Empty } from '../componentes/EstadoVacio'
import { Modal } from '../componentes/Modal'
import { Page } from '../componentes/Pagina'
import { Missing } from '../componentes/RecursoNoDisponible'
import { useEventActions } from '../funcionalidades/admin/hooks'
import { useArtists } from '../funcionalidades/artistas/hooks'
import { useEvent } from '../funcionalidades/eventos/hooks'
import { date } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { useValidatedForm } from '../lib/useValidatedForm'
import { codeSchema, required } from '../lib/validacion'

export function ManageEvent() {
  const code = codeSchema.safeParse(useParams().eventCode)
  const event = useEvent(code.data)
  if (!code.success) return <Missing />
  return <QueryView query={event} unavailable="Evento no disponible.">{data => <Page title={data.name} description={data.eventCode}>
    <div className="card space-y-3"><div className="flex gap-2"><Badge value={data.status} /><Badge value={data.category} /></div>
      <p>{date(data.eventDate)} · {data.venueName}</p>
      {data.description && <p>{data.description}</p>}
      <h2>Artistas asociados</h2>
      {data.artists.length ? <ul>{data.artists.map(artist => <li key={artist.id}><Link className="text-link" to={RUTAS.artista(artist.id)}>{artist.stageName}</Link></li>)}</ul> : <Empty>Este evento aún no tiene artistas.</Empty>}
    </div>
    <EventActions key={data.eventCode} code={data.eventCode} draft={data.status === 'DRAFT'} />
  </Page>}</QueryView>
}

function EventActions({ code, draft }: { code: string; draft: boolean }) {
  const [confirm, setConfirm] = useState(false)
  const artists = useArtists()
  const form = useValidatedForm(z.object({ artistId: required.regex(/^[1-9]\d*$/, 'Selecciona un artista.') }), { artistId: '' })
  const { publish, associate } = useEventActions(code, () => setConfirm(false), () => form.reset())
  return <div className="space-y-5">
    {draft && <button className="primary" onClick={() => { publish.reset(); setConfirm(true) }}>Publicar evento</button>}
    {publish.isSuccess && <p className="notice" role="status">Evento publicado.</p>}
    {confirm && <Modal title="¿Publicar evento?" close={() => setConfirm(false)} busy={publish.isPending}>
      <p>El evento aparecerá en la cartelera pública.</p>
      {publish.error && <ErrorNote error={publish.error} />}
      <button className="primary" disabled={publish.isPending} onClick={() => publish.mutate()}>{publish.isPending ? 'Publicando…' : 'Confirmar publicación'}</button>
    </Modal>}
    <div className="card space-y-4"><h2>Asociar artista</h2>
      <QueryView query={artists}>{items => items.length ? <form className="form" noValidate onSubmit={form.handleSubmit(values => associate.mutate(values.artistId))}>
        <Field label="Artista activo" error={form.formState.errors.artistId?.message}>{(id, description) => <select id={id} aria-describedby={description} aria-invalid={!!description} {...form.register('artistId')}>
          <option value="">Selecciona un artista</option>{items.map(artist => <option key={artist.id} value={artist.id}>{artist.stageName}</option>)}</select>}</Field>
        {associate.error && <ErrorNote error={associate.error} />}
        {associate.isSuccess && <p className="notice" role="status">Artista asociado.</p>}
        <button className="primary" disabled={associate.isPending}>{associate.isPending ? 'Asociando…' : 'Asociar artista'}</button>
      </form> : <Empty>No hay artistas activos disponibles.</Empty>}</QueryView>
    </div>
  </div>
}
