import { useQuery } from '@tanstack/react-query'
import { eventosApi } from './api'

export function useEvents(artist = '') {
  return useQuery({ queryKey: ['events', artist], queryFn: ({ signal }) => eventosApi.events(artist, signal) })
}

export function useEvent(code?: string) {
  return useQuery({ queryKey: ['event', code], queryFn: ({ signal }) => eventosApi.event(code ?? '', signal), enabled: code !== undefined })
}
