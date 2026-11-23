import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { QueryView } from '../componentes/EstadoConsulta'
import { Page } from '../componentes/Pagina'
import { Missing } from '../componentes/RecursoNoDisponible'
import { TicketInfo } from '../funcionalidades/tickets/componentes/TicketInfo'
import { RUTAS } from '../lib/rutas'
import { codeSchema } from '../lib/validacion'

import { CancelTicket } from '../funcionalidades/tickets/componentes/CancelTicket'
import { useTicket } from '../funcionalidades/tickets/hooks'
export function TicketDetail({ confirmation = false }: { confirmation?: boolean }) {
  const code = codeSchema.safeParse(useParams().ticketCode)
  const ticket = useTicket(code.data ?? '')
  const [cancelled, setCancelled] = useState<string>()
  if (!code.success) return <Missing />
  return <QueryView query={ticket} unavailable="Ticket no disponible.">{data =>
    <Page title={confirmation ? 'Compra confirmada' : 'Detalle del ticket'} description={data.eventName}>
      {cancelled === data.ticketCode && <p className="notice" role="status">El ticket fue cancelado.</p>}
      <div className="card"><TicketInfo ticket={data} /></div>
      <div className="flex flex-wrap gap-3">
        <Link className="secondary" to={RUTAS.tickets}>Mis tickets</Link>
        {confirmation ? <Link className="primary" to={RUTAS.ticket(data.ticketCode)}>Ver detalle</Link>
          : data.status === 'PAID' && <CancelTicket code={data.ticketCode} onCancel={() => setCancelled(data.ticketCode)} />}
      </div>
    </Page>}</QueryView>
}
