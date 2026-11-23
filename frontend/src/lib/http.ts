import { z } from 'zod'

z.config(z.locales.es())

const errorBody = z.object({ message: z.string(), details: z.record(z.string(), z.string()).nullish() })

export class ApiError extends Error {
  constructor(public status: number, message: string, public details: Record<string, string> = {}, public path?: string) {
    super(message)
    this.name = 'ApiError'
  }
}

const messages: Record<number, string> = {
  0: 'No pudimos conectar con PulsePass. Revisa tu conexión e inténtalo de nuevo.',
  400: 'Revisa los datos ingresados.',
  401: 'Tu sesión expiró. Inicia sesión nuevamente.',
  403: 'No tienes permiso para acceder a este recurso.',
  404: 'No encontramos el recurso solicitado.',
  409: 'La operación no se puede completar en el estado actual.',
}
let token: string | undefined
let expire: () => void = () => {}
export function configureSession(value: string | undefined, onExpire = () => {}) {
  token = value
  expire = onExpire
}

export async function request<T>(schema: z.ZodType<T>, path: string, method = 'GET', body?: unknown, signal?: AbortSignal) {
  const sentToken = path === '/auth/login' || (path === '/users' && method === 'POST') ? undefined : token
  let response: Response
  try {
    response = await fetch(new URL('/api' + path, window.location.origin), {
      method, signal,
      headers: {
        Accept: 'application/json',
        ...(sentToken && { Authorization: 'Bearer ' + sentToken }),
        ...(body !== undefined && { 'Content-Type': 'application/json' }),
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch (error) {
    if (signal?.aborted) throw error
    throw new ApiError(0, messages[0])
  }
  const payload: unknown = await response.json().catch(() => undefined)
  // A response from a previous session must not refill the current query cache.
  if (sentToken && sentToken !== token) throw new DOMException('Sesión finalizada.', 'AbortError')
  if (!response.ok) {
    const parsed = errorBody.safeParse(payload)
    if (response.status === 401 && sentToken) expire()
    const message = response.status === 409 && parsed.success ? parsed.data.message : messages[response.status]
    throw new ApiError(response.status, message ?? 'Ocurrió un error en el servicio.', response.status === 400 && parsed.success ? parsed.data.details ?? {} : {}, path)
  }
  const parsed = schema.safeParse(payload)
  if (!parsed.success) throw new ApiError(502, 'PulsePass devolvió una respuesta inesperada.')
  return parsed.data
}
