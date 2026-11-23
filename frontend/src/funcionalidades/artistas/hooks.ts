import { useMutation, useQuery } from '@tanstack/react-query'
import { artistasApi } from './api'

export function useArtists() {
  return useQuery({ queryKey: ['artists'], queryFn: ({ signal }) => artistasApi.artists(signal) })
}

export function useArtist(id?: number) {
  return useQuery({ queryKey: ['artist', id], queryFn: ({ signal }) => artistasApi.artist(id ?? 0, signal), enabled: id !== undefined })
}

export function useArtistLookup(onFound: (id: number) => void) {
  return useMutation({ mutationFn: artistasApi.artistByName, onSuccess: item => onFound(item.id) })
}
