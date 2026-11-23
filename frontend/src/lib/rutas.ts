import { z } from 'zod'

const origin = 'https://pulsepass.invalid'
const internalPath = z.string().refine(value => {
  if (!value.startsWith('/')) return false
  try { return new URL(value, origin).origin === origin }
  catch { return false }
})
export const loginNavigation = z.object({
  desde: internalPath.optional(),
  registrado: z.boolean().optional(),
})
const path = (base: string, value: string | number) => base + '/' + encodeURIComponent(value)
export const RUTAS = {
  inicio: '/', eventos: '/eventos', evento: (code: string) => path('/eventos', code),
  artistas: '/artistas', artista: (id: number) => path('/artistas', id),
  sedes: '/sedes', sede: (code: string) => path('/sedes', code),
  login: '/login', registro: '/registro', cuenta: '/cuenta',
  tickets: '/mis-tickets', ticket: (code: string) => path('/mis-tickets', code),
  confirmacion: (code: string) => path('/compra/confirmacion', code),
  validar: '/staff/validacion', pagados: '/staff/eventos', pagadosEvento: (code: string) => path('/staff/eventos', code) + '/tickets',
  admin: '/admin/eventos', crear: '/admin/eventos/nuevo', gestionar: (code: string) => path('/admin/eventos', code),
  patrones: { evento: '/eventos/:eventCode', artista: '/artistas/:id', sede: '/sedes/:code', ticket: '/mis-tickets/:ticketCode', confirmacion: '/compra/confirmacion/:ticketCode', pagados: '/staff/eventos/:eventCode/tickets', gestionar: '/admin/eventos/:eventCode' },
  prohibido: '/403', perdido: '/404',
}
