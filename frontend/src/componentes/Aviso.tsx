import { ApiError } from '../lib/http'

export function ErrorNote({ error, fallback }: { error: unknown; fallback?: string }) {
  return <p className="notice error" role="alert">{fallback ?? (error instanceof ApiError ? error.message : 'No se pudo completar la operación.')}</p>
}
