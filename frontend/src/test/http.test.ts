import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest'
import { authApi } from '../funcionalidades/auth/api'
import { eventosApi } from '../funcionalidades/eventos/api'
import { price } from '../lib/formato'
import { ApiError, configureSession } from '../lib/http'

const base = 'http://localhost:5173/api'
const server = setupServer()
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => { server.resetHandlers(); configureSession(undefined) })
afterAll(() => server.close())

describe('contrato HTTP', () => {
  it('envía el JWT únicamente en Authorization', async () => {
    configureSession('jwt-prueba')
    server.use(http.get(base + '/auth/me', ({ request }) => {
      expect(request.headers.get('Authorization')).toBe('Bearer jwt-prueba')
      expect(request.url).not.toContain('jwt-prueba')
      return HttpResponse.json({ id: 1, username: 'andrea', roles: ['USER'] })
    }))
    await expect(authApi.me()).resolves.toMatchObject({ username: 'andrea' })
  })

  it.each([400, 403, 404, 409, 500])('traduce HTTP %i y conserva solo los detalles pertinentes', async status => {
    server.use(http.get(base + '/events/CMF', () => HttpResponse.json({ message: 'Regla de negocio', details: { name: 'Obligatorio' } }, { status })))
    const error = await eventosApi.event('CMF').catch((error: unknown) => error)
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ status, details: status === 400 ? { name: 'Obligatorio' } : {} })
    if (status === 409) expect(error).toMatchObject({ message: 'Regla de negocio' })
  })

  it('cierra la sesión ante 401 autenticado y omite el token en login', async () => {
    const expire = vi.fn()
    configureSession('jwt-prueba', expire)
    server.use(
      http.get(base + '/auth/me', () => new HttpResponse(null, { status: 401 })),
      http.post(base + '/auth/login', ({ request }) => {
        expect(request.headers.has('Authorization')).toBe(false)
        return new HttpResponse(null, { status: 401 })
      }),
    )
    await expect(authApi.login({ identifier: 'x', password: 'x' })).rejects.toMatchObject({ status: 401 })
    expect(expire).not.toHaveBeenCalled()
    await expect(authApi.me()).rejects.toMatchObject({ status: 401 })
    expect(expire).toHaveBeenCalledTimes(1)
  })

  it('descarta respuestas de una sesión que ya se cerró', async () => {
    let finish: () => void = () => {}
    let started: () => void = () => {}
    const received = new Promise<void>(resolve => { started = resolve })
    const release = new Promise<void>(resolve => { finish = resolve })
    configureSession('jwt-prueba')
    server.use(http.get(base + '/auth/me', async () => {
      started()
      await release
      return HttpResponse.json({ id: 1, username: 'andrea', roles: ['USER'] })
    }))
    const response = authApi.me()
    await received
    configureSession(undefined)
    finish()
    await expect(response).rejects.toMatchObject({ name: 'AbortError' })
  })

  it('rechaza una respuesta exitosa fuera del contrato', async () => {
    server.use(http.get(base + '/events/published', () => HttpResponse.json([{ id: 'inválido' }])))
    await expect(eventosApi.events('')).rejects.toMatchObject({ status: 502 })
  })

  it('formatea precios como pesos colombianos', () => {
    expect(price(250000)).toBe(new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0, maximumFractionDigits: 2 }).format(250000))
  })
})
