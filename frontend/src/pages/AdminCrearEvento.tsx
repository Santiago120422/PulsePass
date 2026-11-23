import { useNavigate } from 'react-router'
import { ErrorNote } from '../componentes/Aviso'
import { Field, Input } from '../componentes/Campo'
import { QueryView } from '../componentes/EstadoConsulta'
import { Empty } from '../componentes/EstadoVacio'
import { Page } from '../componentes/Pagina'
import { useCreateEvent } from '../funcionalidades/admin/hooks'
import { category, createSchema } from '../funcionalidades/eventos/tipos'
import { useVenues } from '../funcionalidades/sedes/hooks'
import { labels } from '../lib/formato'
import { RUTAS } from '../lib/rutas'
import { useValidatedForm } from '../lib/useValidatedForm'

export function CreateEvent() {
  const navigate = useNavigate()
  const venues = useVenues()
  const form = useValidatedForm(createSchema, { eventCode: '', name: '', description: '', category: '', eventDate: '', minimumAge: '', venueCode: '' })
  const errors = form.formState.errors
  const create = useCreateEvent(code => navigate(RUTAS.gestionar(code)), form.setApiErrors)
  return <Page title="Crear evento" description="El evento se guardará como borrador.">
    <form className="form card max-w-2xl" noValidate onSubmit={form.handleSubmit(values => create.mutate(values))}>
      <Input label="Código del evento" error={errors.eventCode?.message} {...form.register('eventCode')} />
      <Input label="Nombre" error={errors.name?.message} {...form.register('name')} />
      <Field label="Descripción (opcional)" error={errors.description?.message}>{(id, description) => <textarea id={id} aria-describedby={description} aria-invalid={!!description} rows={4} {...form.register('description')} />}</Field>
      <Field label="Categoría" error={errors.category?.message}>{(id, description) => <select id={id} aria-describedby={description} aria-invalid={!!description} {...form.register('category')}>
        <option value="">Selecciona una categoría</option>{category.options.map(value => <option key={value} value={value}>{labels[value]}</option>)}</select>}</Field>
      <Input label="Fecha y hora (Colombia)" type="datetime-local" error={errors.eventDate?.message} {...form.register('eventDate')} />
      <Input label="Edad mínima" type="number" min={0} error={errors.minimumAge?.message} {...form.register('minimumAge')} />
      <QueryView query={venues}>{items => items.length ? <Field label="Sede" error={errors.venueCode?.message}>{(id, description) => <select id={id} aria-describedby={description} aria-invalid={!!description} {...form.register('venueCode')}>
        <option value="">Selecciona una sede</option>{items.map(venue => <option key={venue.code} value={venue.code}>{venue.name} · {venue.city}</option>)}</select>}</Field> : <Empty>No hay sedes activas disponibles.</Empty>}</QueryView>
      {create.error && <ErrorNote error={create.error} />}
      <button className="primary" disabled={create.isPending || !venues.data?.length}>{create.isPending ? 'Guardando…' : 'Guardar borrador'}</button>
    </form>
  </Page>
}
