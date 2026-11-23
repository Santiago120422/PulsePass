import { Link } from 'react-router'
import { Badge } from '../../../componentes/Badge'
import { date, labels, price } from '../../../lib/formato'
import { RUTAS } from '../../../lib/rutas'
import { type Ticket } from '../tipos'

import { CancelTicket } from './CancelTicket'

export function TicketTable({ tickets, staff = false, onCancel }: { tickets: Ticket[]; staff?: boolean; onCancel?: () => void }) {
  return <div className="table-shell"><table role="table">
    <caption className="sr-only">{staff ? 'Tickets pagados por evento' : 'Mis tickets'}</caption>
    <thead role="rowgroup"><tr role="row"><th scope="col">Código</th><th scope="col">{staff ? 'Comprador' : 'Evento'}</th><th scope="col">Tipo</th><th scope="col">Precio</th><th scope="col">Estado</th><th scope="col">Compra</th>{onCancel && <th scope="col">Acciones</th>}</tr></thead>
    <tbody role="rowgroup">{tickets.map(ticket => <tr role="row" key={ticket.id}>
      <td data-label="Código" role="cell"><Link className="text-link" to={staff ? { pathname: RUTAS.validar, search: new URLSearchParams({ ticket: ticket.ticketCode }).toString() } : RUTAS.ticket(ticket.ticketCode)}>{ticket.ticketCode}</Link></td>
      <td data-label={staff ? "Comprador" : "Evento"} role="cell">{staff ? ticket.userEmail : ticket.eventName}</td><td data-label="Tipo" role="cell">{labels[ticket.type]}</td><td data-label="Precio" role="cell">{price(ticket.price)}</td>
      <td data-label="Estado" role="cell"><Badge value={ticket.status} /></td><td data-label="Compra" role="cell">{date(ticket.purchaseDate)}</td>
      {onCancel && <td data-label="Acciones" role="cell">{ticket.status === 'PAID' && <CancelTicket code={ticket.ticketCode} onCancel={onCancel} />}</td>}
    </tr>)}</tbody>
  </table></div>
}
