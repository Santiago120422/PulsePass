import { type ReactNode } from 'react'
import { ApiError } from '../lib/http'
import { ErrorNote } from './Aviso'

export function QueryView<T>({ query, children, unavailable }: {
  query: { isPending: boolean; error: Error | null; data?: T; refetch: () => unknown }
  children: (data: T) => ReactNode
  unavailable?: string
}) {
  if (query.isPending) return <p className="notice" role="status">Cargando…</p>
  if (query.error) {
    const missing = query.error instanceof ApiError && [403, 404].includes(query.error.status)
    return <div className="space-y-3"><ErrorNote error={query.error} fallback={missing ? unavailable : undefined} />
      {!missing && <button type="button" className="secondary" onClick={() => { void query.refetch() }}>Reintentar</button>}</div>
  }
  return query.data === undefined ? null : children(query.data)
}
