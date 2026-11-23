import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { createMemoryRouter } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { afterAll, afterEach, beforeAll, expect, it, vi } from 'vitest'
import { SessionProvider } from '../funcionalidades/auth/AuthContext'
import type { Role } from '../funcionalidades/auth/tipos'
import { RutaProtegida as Guard } from '../layouts/RutaProtegida'
import { CreateEvent } from '../pages/AdminCrearEvento'
import { ManageEvent } from '../pages/AdminGestionEvento'
import { Catalog } from '../pages/CatalogoEventos'
import { EventDetail } from '../pages/DetalleEvento'
import { TicketDetail } from '../pages/DetalleTicket'
import { Login } from '../pages/Login'
import { Register } from '../pages/Registro'
import { Paid } from '../pages/TicketsPagadosEvento'
import { Validate } from '../pages/ValidacionIngreso'

const base = 'http://localhost:5173/api'
const server = setupServer(http.get(base + '/artists/active', () => HttpResponse.json([])))
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => { cleanup(); sessionStorage.clear(); server.resetHandlers() })
afterAll(() => server.close())

function setup(path: string) {
  const router = createMemoryRouter([{ children: [
    { path: '/login', Component: Login },
    { path: '/registro', Component: Register },
    { path: '/eventos', Component: Catalog },
    { path: '/eventos/:eventCode', Component: EventDetail },
    { Component: () => <Guard roles={['USER']} />, children: [
      { path: '/compra/confirmacion/:ticketCode', element: <TicketDetail confirmation /> },
      { path: '/mis-tickets/:ticketCode', Component: TicketDetail },
    ] },
    { Component: () => <Guard roles={['STAFF', 'ADMIN']} />, children: [
      { path: '/staff/validacion', Component: Validate },
      { path: '/staff/eventos/:eventCode/tickets', Component: Paid },
    ] },
    { path: '/403', element: <h1>Acceso restringido</h1> },
    { Component: () => <Guard roles={['ADMIN']} />, children: [
      { path: '/admin/eventos', element: <h1>Administración</h1> },
      { path: '/admin/eventos/:eventCode', Component: ManageEvent },
      { path: '/admin/eventos/nuevo', Component: CreateEvent },
    ] },
  ] }], { initialEntries: [path] })
  const client = new QueryClient({ defaultOptions: { queries: { retry: false, staleTime: 30_000 } } })
  render(<QueryClientProvider client={client}><SessionProvider><RouterProvider router={router} /></SessionProvider></QueryClientProvider>)
  return router
}

const principal = (roles: Role[]) => HttpResponse.json({ id: 1, username: 'pulse', roles })

it('redirige al login y después vuelve a la ruta protegida', async () => {
  server.use(
    http.post(base + '/auth/login', () => HttpResponse.json({ accessToken: 'jwt', tokenType: 'Bearer', expiresIn: 900 })),
    http.get(base + '/auth/me', ({ request }) => request.headers.get('Authorization') === 'Bearer jwt'
      ? principal(['ADMIN'])
      : new HttpResponse(null, { status: 401 })),
  )
  const user = userEvent.setup()
  setup('/admin/eventos')
  await screen.findByRole('heading', { name: 'Iniciar sesión' })
  await user.type(screen.getByLabelText('Usuario o correo'), 'pulse')
  await user.type(screen.getByLabelText('Contraseña'), 'secret')
  await user.click(screen.getByRole('button', { name: 'Ingresar' }))
  await screen.findByRole('heading', { name: 'Administración' })
  expect(sessionStorage.getItem('pulsepass.session')).toContain('jwt')
})

it('distingue un 401 al cargar el perfil de una contraseña rechazada', async () => {
  server.use(
    http.post(base + '/auth/login', () => HttpResponse.json({ accessToken: 'jwt', tokenType: 'Bearer', expiresIn: 900 })),
    http.get(base + '/auth/me', () => new HttpResponse(null, { status: 401 })),
  )
  const user = userEvent.setup()
  setup('/login')
  await user.type(await screen.findByLabelText('Usuario o correo'), 'pulse')
  await user.type(screen.getByLabelText('Contraseña'), 'secret')
  await user.click(screen.getByRole('button', { name: 'Ingresar' }))
  expect((await screen.findByRole('alert')).textContent).toContain('Tu sesión expiró')
  expect(sessionStorage.getItem('pulsepass.session')).toBeNull()
})

it.each(['USER', 'STAFF', 'ADMIN'] as const)('restaura sesión y verifica permiso para %s', async role => {
  sessionStorage.setItem('pulsepass.session', JSON.stringify({ token: 'jwt', expiresAt: Date.now() + 900_000 }))
  server.use(http.get(base + '/auth/me', () => principal([role])))
  setup('/admin/eventos')
  await screen.findByRole('heading', { name: role === 'ADMIN' ? 'Administración' : 'Acceso restringido' })
})

it('registro inválido muestra errores antes de enviar', async () => {
  const submitted = vi.fn()
  server.use(http.post(base + '/users', () => { submitted(); return new HttpResponse(null, { status: 400 }) }))
  const user = userEvent.setup()
  setup('/registro')
  await user.type(screen.getByLabelText('Contraseña'), 'corta')
  await user.click(screen.getByRole('button', { name: 'Registrarme' }))
  expect(await screen.findByText('Usa mínimo 12 caracteres.')).toBeTruthy()
  expect(submitted).not.toHaveBeenCalled()
})

it('muestra los eventos de la API y sincroniza el filtro con la URL', async () => {
  server.use(http.get(base + '/events/published', () => HttpResponse.json([
    { id: 1, eventCode: 'CMF', name: 'Caribbean Music Fest', category: 'MUSIC', status: 'PUBLISHED', eventDate: '2030-11-22T18:00:00', venueCode: 'VEN01', venueName: 'Marina' },
  ])))
  const user = userEvent.setup()
  const router = setup('/eventos')
  await screen.findByRole('heading', { name: 'Caribbean Music Fest' })
  await user.selectOptions(screen.getByLabelText('Categoría'), 'SPORTS')
  expect(router.state.location.search).toBe('?categoria=SPORTS')
  expect(await screen.findByText('No hay eventos para mostrar.')).toBeTruthy()
})

const eventFixture = {
  id: 1, eventCode: 'CMF', name: 'Festival Caribe', category: 'MUSIC', status: 'PUBLISHED',
  eventDate: '2030-11-22T18:00:00', venueCode: 'VEN01', venueName: 'Marina', artists: [], minimumAge: 18,
}
const profileFixture = { id: 1, username: 'pulse', email: 'pulse@example.com', active: true }
const ticketFixture = {
  id: 1, ticketCode: 'TKT-123', type: 'GENERAL', status: 'PAID', price: 120000,
  purchaseDate: '2030-11-11T12:00:00', userEmail: profileFixture.email, eventCode: 'CMF', eventName: 'Festival Caribe',
}
function authenticate(role: Role) {
  sessionStorage.setItem('pulsepass.session', JSON.stringify({ token: 'jwt', expiresAt: Date.now() + 900_000 }))
  server.use(http.get(base + '/auth/me', () => principal([role])))
}

it('compra con el correo del perfil y muestra la confirmación', async () => {
  authenticate('USER')
  server.use(
    http.get(base + '/events/CMF', () => HttpResponse.json(eventFixture)),
    http.get(base + '/tickets/prices', () => HttpResponse.json({ GENERAL: 120000, VIP: 240000, STUDENT: 84000, BACKSTAGE: 420000 })),
    http.get(base + '/venues/VEN01', () => HttpResponse.json({ id: 1, code: 'VEN01', city: 'Santa Marta', name: 'Marina', address: 'Centro', capacity: 300, active: true })),
    http.get(base + '/users/by-username', () => HttpResponse.json(profileFixture)),
    http.get(base + '/tickets/TKT-123', () => HttpResponse.json(ticketFixture)),
    http.post(base + '/tickets', async ({ request }) => {
      expect(await request.json()).toEqual({ userEmail: 'pulse@example.com', eventCode: 'CMF', type: 'GENERAL' })
      return HttpResponse.json(ticketFixture, { status: 201 })
    }),
  )
  const user = userEvent.setup()
  setup('/eventos/CMF')
  await user.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
  expect((await screen.findByRole('dialog', { name: 'Confirmar compra' })).textContent).toMatch(/Total a pagar.*120\.000.*COP/)
  await user.click(await screen.findByRole('button', { name: 'Confirmar compra' }))
  await screen.findByRole('heading', { name: 'Compra confirmada' })
  expect(screen.getByText('TKT-123')).toBeTruthy()
})

it('mantiene abierto el diálogo si el backend rechaza la cancelación', async () => {
  authenticate('USER')
  server.use(
    http.get(base + '/tickets/TKT-123', () => HttpResponse.json(ticketFixture)),
    http.patch(base + '/tickets/TKT-123/cancel', () => HttpResponse.json({ message: 'El ticket ya fue utilizado.' }, { status: 409 })),
  )
  const user = userEvent.setup()
  setup('/mis-tickets/TKT-123')
  await user.click(await screen.findByRole('button', { name: 'Cancelar ticket' }))
  await user.click(screen.getByRole('button', { name: 'Confirmar cancelación' }))
  expect((await screen.findByRole('alert')).textContent).toBe('El ticket ya fue utilizado.')
  expect(screen.getByRole('dialog', { name: '¿Cancelar este ticket?' })).toBeTruthy()
})

it('registra el ingreso y deja visible el rechazo de una segunda validación', async () => {
  authenticate('STAFF')
  let used = false
  server.use(
    http.get(base + '/tickets/TKT-123', () => HttpResponse.json(ticketFixture)),
    http.patch(base + '/tickets/TKT-123/use', () => {
      if (used) return HttpResponse.json({ message: 'El ticket ya fue utilizado.' }, { status: 409 })
      used = true
      return HttpResponse.json({ ...ticketFixture, status: 'USED' })
    }),
  )
  const user = userEvent.setup()
  setup('/staff/validacion')
  await user.type(await screen.findByLabelText('Código de ticket'), 'TKT-123')
  await user.click(screen.getByRole('button', { name: 'Buscar' }))
  await user.click(await screen.findByRole('button', { name: 'Marcar como usado' }))
  expect(await screen.findByText('Ingreso registrado.')).toBeTruthy()
  await user.click(screen.getByRole('button', { name: 'Marcar como usado' }))
  expect((await screen.findByRole('alert')).textContent).toBe('El ticket ya fue utilizado.')
})

it('publica un borrador y actualiza su estado visible', async () => {
  authenticate('ADMIN')
  server.use(
    http.get(base + '/events/CMF', () => HttpResponse.json({ ...eventFixture, status: 'DRAFT' })),
    http.patch(base + '/events/CMF/publish', () => HttpResponse.json(eventFixture)),
  )
  const user = userEvent.setup()
  setup('/admin/eventos/CMF')
  await user.click(await screen.findByRole('button', { name: 'Publicar evento' }))
  await user.click(screen.getByRole('button', { name: 'Confirmar publicación' }))
  expect(await screen.findByText('Evento publicado.')).toBeTruthy()
  expect(screen.getByText('Publicado')).toBeTruthy()
})

it('una sesión rechazada por la API se elimina al restaurar', async () => {
  authenticate('ADMIN')
  server.use(http.get(base + '/auth/me', () => new HttpResponse(null, { status: 401 })))
  const router = setup('/admin/eventos')
  await screen.findByRole('heading', { name: 'Iniciar sesión' })
  expect(screen.getByRole('status').textContent).toContain('Tu sesión expiró')
  expect(sessionStorage.getItem('pulsepass.session')).toBeNull()
  expect(router.state.location.state).toEqual({ desde: '/admin/eventos' })
})

it('registro exitoso redirige al login con un mensaje', async () => {
  server.use(http.post(base + '/users', () => HttpResponse.json(profileFixture, { status: 201 })))
  const user = userEvent.setup()
  setup('/registro')
  await user.type(screen.getByLabelText('Nombre de usuario'), 'pulse')
  await user.type(screen.getByLabelText('Correo electrónico'), profileFixture.email)
  await user.type(screen.getByLabelText('Nombres'), 'Andrea')
  await user.type(screen.getByLabelText('Apellidos'), 'Pérez')
  await user.type(screen.getByLabelText('Fecha de nacimiento'), '1998-08-01')
  await user.type(screen.getByLabelText('Contraseña'), 'pass-segura-123')
  await user.click(screen.getByRole('button', { name: 'Registrarme' }))
  await screen.findByRole('heading', { name: 'Iniciar sesión' })
  expect(screen.getByRole('status').textContent).toContain('Tu cuenta está lista')
})

it('lista los tickets pagados de un evento para STAFF', async () => {
  authenticate('STAFF')
  server.use(http.get(base + '/events/CMF/tickets/paid', () => HttpResponse.json([ticketFixture])))
  setup('/staff/eventos/CMF/tickets')
  await screen.findByRole('table', { name: 'Tickets pagados por evento' })
  expect(screen.getByRole('link', { name: 'TKT-123' }).getAttribute('href')).toBe('/staff/validacion?ticket=TKT-123')
})

it('crea un borrador con la fecha local y selecciona una sede activa', async () => {
  authenticate('ADMIN')
  server.use(
    http.get(base + '/venues/active', () => HttpResponse.json([{ id: 1, code: 'VEN01', city: 'Santa Marta', name: 'Marina', address: 'Centro', capacity: 300, active: true }])),
    http.post(base + '/events', async ({ request }) => {
      expect(await request.json()).toEqual({ eventCode: 'CMF', name: 'Festival Caribe', category: 'MUSIC', eventDate: '2030-11-22T18:00', venueCode: 'VEN01', minimumAge: 18, description: null })
      return HttpResponse.json({ ...eventFixture, status: 'DRAFT' }, { status: 201 })
    }),
  )
  const user = userEvent.setup()
  setup('/admin/eventos/nuevo')
  await user.type(await screen.findByLabelText('Código del evento'), 'CMF')
  await user.type(screen.getByLabelText('Nombre', { exact: true }), 'Festival Caribe')
  await user.selectOptions(screen.getByLabelText('Categoría'), 'MUSIC')
  await user.type(screen.getByLabelText('Fecha y hora (Colombia)'), '2030-11-22T18:00')
  await user.type(screen.getByLabelText('Edad mínima'), '18')
  await user.selectOptions(await screen.findByLabelText('Sede'), 'VEN01')
  await user.click(screen.getByRole('button', { name: 'Guardar borrador' }))
  await screen.findByRole('heading', { name: 'Festival Caribe' })
  expect(screen.getByText('Borrador')).toBeTruthy()
})
