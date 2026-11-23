import { useQuery, useQueryClient } from '@tanstack/react-query'
import { createContext, useCallback, useContext, useLayoutEffect, useState, type ReactNode } from 'react'
import { z } from 'zod'
import { configureSession } from '../../lib/http'
import { authApi } from './api'
import { type Principal } from './tipos'

const key = 'pulsepass.session'
const stored = z.object({ token: z.string().min(1), expiresAt: z.number().positive() })
type Stored = z.infer<typeof stored>
type Session = { kind: 'anonymous' } | { kind: 'loading' } | { kind: 'failed'; error: Error; retry: () => void } | { kind: 'ready'; principal: Principal }
const SessionContext = createContext<{
  session: Session
  expired: boolean
  login: (input: { identifier: string; password: string }) => Promise<void>
  logout: () => void
} | null>(null)

function restore(): { stored?: Stored; expired: boolean } {
  try {
    const raw = sessionStorage.getItem(key)
    if (!raw) return { expired: false }
    const value = stored.parse(JSON.parse(raw))
    if (value.expiresAt > Date.now()) return { stored: value, expired: false }
  } catch {
    sessionStorage.removeItem(key)
    return { expired: false }
  }
  sessionStorage.removeItem(key)
  return { expired: true }
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const client = useQueryClient()
  const [current, setCurrent] = useState(restore)
  const clear = useCallback((expired: boolean) => {
    configureSession(undefined)
    sessionStorage.removeItem(key)
    client.clear()
    setCurrent({ expired })
  }, [client])
  const storedToken = current.stored?.token
  useLayoutEffect(() => {
    configureSession(storedToken, () => clear(true))
  }, [storedToken, clear])
  const principal = useQuery({ queryKey: ['principal'], queryFn: ({ signal }) => authApi.me(signal), enabled: !!current.stored })
  const session: Session = !current.stored ? { kind: 'anonymous' }
    : principal.isSuccess ? { kind: 'ready', principal: principal.data }
    : principal.isError ? { kind: 'failed', error: principal.error, retry: () => { void principal.refetch() } }
    : { kind: 'loading' }
  async function login(input: { identifier: string; password: string }) {
    const result = await authApi.login(input)
    const value = { token: result.accessToken, expiresAt: Date.now() + result.expiresIn * 1000 }
    configureSession(value.token)
    try {
      await client.fetchQuery({ queryKey: ['principal'], queryFn: ({ signal }) => authApi.me(signal) })
      sessionStorage.setItem(key, JSON.stringify(value))
      setCurrent({ stored: value, expired: false })
    } catch (error) {
      clear(false)
      throw error
    }
  }
  return <SessionContext.Provider value={{ session, expired: current.expired, login, logout: () => clear(false) }}>{children}</SessionContext.Provider>
}

export function useSession() {
  const value = useContext(SessionContext)
  if (!value) throw new Error('Falta el proveedor de sesión')
  return value
}
