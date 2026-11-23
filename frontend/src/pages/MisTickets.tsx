import { useState } from 'react'
import { useSearchParams } from 'react-router'
import { QueryView } from '../componentes/EstadoConsulta'
import { Empty } from '../componentes/EstadoVacio'
import { Page } from '../componentes/Pagina'
import { useTickets } from '../funcionalidades/tickets/hooks'
import { ticketStatus } from '../funcionalidades/tickets/tipos'
import { useProfile } from '../funcionalidades/usuarios/hooks'
import { labels } from '../lib/formato'

import { TicketTable } from '../funcionalidades/tickets/componentes/TicketTable'
export function Tickets() {
  const profile = useProfile()
  const [params, setParams] = useSearchParams()
  const status = ticketStatus.safeParse(params.get('estado')).data
  const email = profile.data?.email ?? ''
  const tickets = useTickets(email)
  const [cancelled, setCancelled] = useState(false)
  return <Page title="Mis tickets" description="Consulta tus entradas y el estado de cada una.">
    <div className="flex flex-wrap gap-2" role="group" aria-label="Filtrar tickets por estado">
      <button type="button" className="filter-chip" aria-pressed={!status} onClick={() => setParams({})}>Todos</button>
      {ticketStatus.options.map(value => <button type="button" key={value} className="filter-chip" aria-pressed={status === value} onClick={() => setParams({ estado: value })}>{labels[value]}</button>)}
    </div>
    {cancelled && <p className="notice" role="status">El ticket fue cancelado.</p>}
    <QueryView query={profile}>{() => <QueryView query={tickets}>{items => {
      const filtered = items.filter(ticket => !status || ticket.status === status)
      return filtered.length ? <TicketTable tickets={filtered} onCancel={() => setCancelled(true)} /> : <Empty>{status ? 'No tienes tickets con este estado.' : 'Todavía no tienes tickets.'}</Empty>
    }}</QueryView>}</QueryView>
  </Page>
}
