import { useSearchParams } from 'react-router'
import { ticketsApi } from '../funcionalidades/tickets/api'

import { ErrorNote } from '../componentes/Aviso'
import { Lookup } from '../componentes/Busqueda'
import { QueryView } from '../componentes/EstadoConsulta'
import { Page } from '../componentes/Pagina'
import { TicketInfo } from '../funcionalidades/tickets/componentes/TicketInfo'
import { useTicket, useTicketAction } from '../funcionalidades/tickets/hooks'
import { codeSchema } from '../lib/validacion'

export function Validate() {
  const [params, setParams] = useSearchParams()
  const code = codeSchema.safeParse(params.get('ticket')).data ?? ''
  return <Page title="Validar ingreso" description="Busca una entrada por su código antes de registrar el ingreso.">
    <Lookup key={'lookup-' + code} label="Código de ticket" initial={code} submit={ticket => setParams({ ticket })} />
    {code && <ValidationResult key={code} code={code} />}
  </Page>
}

function ValidationResult({ code }: { code: string }) {
  const ticket = useTicket(code)
  const validate = useTicketAction(ticketsApi.use)
  return <QueryView query={ticket} unavailable="Ticket no disponible.">{data => <div className="card space-y-4">
    <h2>{data.eventName}</h2><TicketInfo ticket={data} />
    {validate.error && <ErrorNote error={validate.error} />}
    {validate.isSuccess && <p className="notice" role="status">Ingreso registrado.</p>}
    <button className="primary" disabled={validate.isPending} onClick={() => validate.mutate(code)}>{validate.isPending ? 'Validando…' : 'Marcar como usado'}</button>
  </div>}</QueryView>
}
