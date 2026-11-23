import { useQuery } from '@tanstack/react-query'
import { sedesApi } from './api'

export function useVenues() {
  return useQuery({ queryKey: ['venues'], queryFn: ({ signal }) => sedesApi.venues(signal) })
}

export function useVenue(code?: string) {
  return useQuery({ queryKey: ['venue', code], queryFn: ({ signal }) => sedesApi.venue(code ?? '', signal), enabled: code !== undefined })
}
