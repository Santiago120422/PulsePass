import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { z } from 'zod'
import { ErrorNote } from '../componentes/Aviso'
import { Badge } from '../componentes/Badge'
import { QueryView } from '../componentes/EstadoConsulta'
import { Empty } from '../componentes/EstadoVacio'
import { Modal } from '../componentes/Modal'
import { Page } from '../componentes/Pagina'
import { Missing } from '../componentes/RecursoNoDisponible'
import { useSession } from '../funcionalidades/auth/AuthContext'
import { CategoryIcon } from '../funcionalidades/eventos/componentes/CategoryIcon'
import { useEvent } from '../funcionalidades/eventos/hooks'
import { useVenue } from '../funcionalidades/sedes/hooks'
import { usePurchase, useTicketPrices } from '../funcionalidades/tickets/hooks'
import { ticketType } from '../funcionalidades/tickets/tipos'
import { useProfile } from '../funcionalidades/usuarios/hooks'
import { date, labels, price } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { useValidatedForm } from '../lib/useValidatedForm'
import { codeSchema } from '../lib/validacion'

const purchaseForm = z.object({ type: ticketType })
type TicketType = z.infer<typeof ticketType>

export function EventDetail() {
  const code = codeSchema.safeParse(useParams().eventCode)
  const event = useEvent(code.data)
  if (!code.success) return <Missing />
  return <div className="space-y-5">
    <Link className="text-link" to={RUTAS.eventos}>Volver a la cartelera</Link>
    <QueryView query={event} unavailable="Evento no disponible.">{data =>
      <div className="grid items-start gap-5 lg:grid-cols-[2fr_1fr]">
        <article className="event-card">
          <div className="category-art detail-art" data-category={data.category}><CategoryIcon category={data.category} /></div>
          <div className="space-y-5 p-5 sm:p-7">
            <Page title={data.name}>
              <div className="flex flex-wrap gap-2"><Badge value={data.category} /><Badge value={data.status} /></div>
              <div className="flex flex-wrap gap-x-5 gap-y-2 muted"><p>{date(data.eventDate)}</p><p>Edad mínima: {data.minimumAge} años</p></div>
              {data.description && <p className="whitespace-pre-wrap">{data.description}</p>}
              {data.streamingUrl && /^https?:\/\//i.test(data.streamingUrl) && <a className="text-link" href={data.streamingUrl}>Ver transmisión</a>}
              <div className="space-y-3"><h2>Artistas</h2>
                {data.artists.length ? <ul className="flex flex-wrap gap-x-4 gap-y-2">{data.artists.map(artist =>
                  <li key={artist.id}><Link className="text-link" to={RUTAS.artista(artist.id)}>{artist.stageName}</Link></li>)}</ul> : <Empty>Artistas por anunciar.</Empty>}
              </div>
              <div className="space-y-3"><h2>Sede</h2><VenueSnippet code={data.venueCode} name={data.venueName} /></div>
            </Page>
          </div>
        </article>
        <EntryPanel key={data.eventCode} code={data.eventCode} name={data.name} published={data.status === 'PUBLISHED'} />
      </div>}
    </QueryView>
  </div>
}

function VenueSnippet({ code, name }: { code: string; name: string }) {
  const venue = useVenue(code)
  return <div className="venue-snippet"><Link className="text-link" to={RUTAS.sede(code)}>{name}</Link>
    {venue.data && <p className="muted">{venue.data.city} · Capacidad: {venue.data.capacity}</p>}</div>
}

function EntryPanel({ code, name, published }: { code: string; name: string; published: boolean }) {
  const { session } = useSession()
  const navigate = useNavigate()
  const form = useValidatedForm(purchaseForm, { type: 'GENERAL' })
  const [selected, setSelected] = useState<TicketType>()
  const prices = useTicketPrices(published)
  const canBuy = published && session.kind !== 'loading' && (session.kind !== 'ready' || session.principal.roles.includes('USER'))
  function start(type: TicketType) {
    if (session.kind !== 'ready') navigate(RUTAS.login, { state: { desde: RUTAS.evento(code) } })
    else setSelected(type)
  }
  const submit = form.handleSubmit(({ type }) => start(type))
  return <aside className="card space-y-4" aria-label="Compra de entradas">
    <h2>Entradas</h2>
    {published ? <form className="form" onSubmit={submit}>
      <fieldset className="ticket-options">
        <legend>Tipo de entrada</legend>
        {ticketType.options.map(type =>
          <label key={type}><input type="radio" value={type} {...form.register('type')} />{labels[type]}
            {prices.data && <span className="ml-auto">{price(prices.data[type])}</span>}</label>)}
      </fieldset>
      <QueryView query={prices}>{() => <button className="primary buy-main" disabled={!canBuy}>Comprar entrada</button>}</QueryView>
    </form> : <p className="muted">Compra no disponible para este evento.</p>}
    {canBuy && prices.data && <div className="purchase-bar"><button type="button" className="primary" onClick={() => { void submit() }}>Comprar entrada<span className="sr-only"> en móvil</span></button></div>}
    {selected && prices.data && session.kind === 'ready' && <Purchase code={code} name={name} type={selected} amount={prices.data[selected]} close={() => setSelected(undefined)} />}
  </aside>
}

function Purchase({ code, name, type, amount, close }: { code: string; name: string; type: TicketType; amount: number; close: () => void }) {
  const profile = useProfile()
  const navigate = useNavigate()
  const purchase = usePurchase(code, type, ticket => navigate(RUTAS.confirmacion(ticket.ticketCode)))
  return <Modal title="Confirmar compra" close={close} busy={purchase.isPending}>
    <dl className="data-list"><div><dt>Evento</dt><dd>{name}</dd></div><div><dt>Tipo</dt><dd>{labels[type]}</dd></div>
      <div><dt>Total a pagar</dt><dd>{price(amount)} COP</dd></div></dl>
    <QueryView query={profile}>{user => <>
      {purchase.error && <ErrorNote error={purchase.error} />}
      <button type="button" className="primary" disabled={purchase.isPending} onClick={() => purchase.mutate(user.email)}>{purchase.isPending ? 'Confirmando…' : 'Confirmar compra'}</button>
    </>}</QueryView>
  </Modal>
}
