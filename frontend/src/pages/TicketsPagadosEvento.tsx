import { useNavigate, useParams } from 'react-router'
import { usePaidTickets } from '../funcionalidades/tickets/hooks'

import { Lookup } from '../componentes/Busqueda'
import { QueryView } from '../componentes/EstadoConsulta'
import { Empty } from '../componentes/EstadoVacio'
import { Page } from '../componentes/Pagina'
import { TicketTable } from '../funcionalidades/tickets/componentes/TicketTable'
import { RUTAS } from '../lib/rutas'
import { codeSchema } from '../lib/validacion'

export function Paid() {
  const navigate = useNavigate()
  const code = codeSchema.safeParse(useParams().eventCode).data ?? ''
  const tickets = usePaidTickets(code)
  return <Page title="Tickets pagados" description="Consulta las entradas pagadas para un evento.">
    <Lookup key={code} label="Código del evento" initial={code} submit={evento => navigate(RUTAS.pagadosEvento(evento))} />
    {code && <QueryView query={tickets}>{items => items.length ? <TicketTable tickets={items} staff /> : <Empty>No hay tickets pagados para este evento.</Empty>}</QueryView>}
  </Page>
}
