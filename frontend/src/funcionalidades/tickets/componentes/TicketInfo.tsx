import { date, labels, price } from '../../../lib/formato'
import { type Ticket } from '../tipos'

import { Badge } from '../../../componentes/Badge'

export function TicketInfo({ ticket }: { ticket: Ticket }) {
  return <dl className="data-list"><div><dt>Código</dt><dd className="font-mono">{ticket.ticketCode}</dd></div>
    <div><dt>Tipo</dt><dd>{labels[ticket.type]}</dd></div><div><dt>Precio</dt><dd>{price(ticket.price)}</dd></div>
    <div><dt>Estado</dt><dd><Badge value={ticket.status} /></dd></div><div><dt>Compra</dt><dd>{date(ticket.purchaseDate)}</dd></div></dl>
}
