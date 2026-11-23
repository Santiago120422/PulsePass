import { useMutation, useQueryClient } from '@tanstack/react-query'
import type { z } from 'zod'
import { eventosApi } from '../eventos/api'
import type { createSchema } from '../eventos/tipos'
import { readRecent, recentKey } from './recientes'

export function useCreateEvent(onCreated: (code: string) => void, onError: (error: Error) => void) {
  const client = useQueryClient()
  return useMutation({
    mutationFn: (input: z.infer<typeof createSchema>) => eventosApi.createEvent({ ...input, minimumAge: Number(input.minimumAge), description: input.description || null }),
    onError,
    onSuccess: event => {
      client.setQueryData(['event', event.eventCode], event)
      sessionStorage.setItem(recentKey, JSON.stringify([event.eventCode, ...readRecent().filter(code => code !== event.eventCode)].slice(0, 10)))
      onCreated(event.eventCode)
    },
  })
}

export function useEventActions(code: string, onPublished: () => void, onAssociated: () => void) {
  const client = useQueryClient()
  function refresh(event: Awaited<ReturnType<typeof eventosApi.event>>) {
    client.setQueryData(['event', code], event)
    void client.invalidateQueries({ queryKey: ['events'] })
  }
  const publish = useMutation({ mutationFn: () => eventosApi.publish(code), onSuccess: event => { refresh(event); onPublished() } })
  const associate = useMutation({ mutationFn: (artistId: string) => eventosApi.associate(code, Number(artistId)), onSuccess: event => { refresh(event); onAssociated() } })
  return { publish, associate }
}
