import { QueryClient } from '@tanstack/react-query'
import { ApiError } from './http'

export const client = new QueryClient({ defaultOptions: { queries: {
  staleTime: 30_000, retry: (count, error) => count < 1 && (!(error instanceof ApiError) || error.status === 0 || error.status >= 500),
} } })
