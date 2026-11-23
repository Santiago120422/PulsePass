import { Link } from 'react-router'
import { date, labels } from '../../../lib/formato'
import { RUTAS } from '../../../lib/rutas'
import { type EventCard } from '../tipos'
import { CategoryIcon } from './CategoryIcon'

import { Badge } from '../../../componentes/Badge'
import { Empty } from '../../../componentes/EstadoVacio'

export function EventGrid({ events }: { events: EventCard[] }) {
  if (!events.length) return <Empty>No hay eventos para mostrar.</Empty>
  return <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{events.map(event =>
    <article className="event-card" key={event.eventCode}>
      <div className="category-art" data-category={event.category}><CategoryIcon category={event.category} /></div>
      <div className="space-y-2 p-4">
        <p className="event-category">{labels[event.category]}</p>
        <h2><Link to={RUTAS.evento(event.eventCode)}>{event.name}</Link></h2>
        <p className="muted">{date(event.eventDate)}</p><p className="muted">{event.venueName}</p>
        <Badge value={event.status} />
      </div>
    </article>)}</div>
}
