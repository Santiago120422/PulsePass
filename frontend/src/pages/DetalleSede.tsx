import { useParams } from 'react-router'
import { QueryView } from '../componentes/EstadoConsulta'
import { Page } from '../componentes/Pagina'
import { Missing } from '../componentes/RecursoNoDisponible'
import { useVenue } from '../funcionalidades/sedes/hooks'
import { codeSchema } from '../lib/validacion'

export function VenueDetail() {
  const code = codeSchema.safeParse(useParams().code)
  const venue = useVenue(code.data)
  if (!code.success) return <Missing />
  return <QueryView query={venue} unavailable="Sede no encontrada.">{data => <Page title={data.name} description={data.city}>
    <dl className="card data-list"><div><dt>Código</dt><dd>{data.code}</dd></div><div><dt>Dirección</dt><dd>{data.address}</dd></div>
      <div><dt>Capacidad</dt><dd>{data.capacity} personas</dd></div><div><dt>Estado</dt><dd>{data.active ? 'Activa' : 'Inactiva'}</dd></div></dl>
  </Page>}</QueryView>
}
