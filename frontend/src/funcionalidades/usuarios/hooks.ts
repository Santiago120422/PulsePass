import { useMutation, useQuery } from '@tanstack/react-query'
import type { z } from 'zod'
import { usuariosApi } from './api'
import type { registerSchema } from './tipos'

import { useSession } from '../auth/AuthContext'

export function useProfile() {
  const { session } = useSession()
  const name = session.kind === 'ready' ? session.principal.username : ''
  return useQuery({ queryKey: ['profile', name], queryFn: ({ signal }) => usuariosApi.byUsername(name, signal), enabled: !!name })
}

export function useProfileByEmail(email: string) {
  return useQuery({ queryKey: ['profile-email', email], queryFn: ({ signal }) => usuariosApi.byEmail(email, signal), enabled: !!email })
}

export function useRegister(onSuccess: () => void, onError: (error: Error) => void) {
  return useMutation({
    mutationFn: (input: z.infer<typeof registerSchema>) => usuariosApi.register({ ...input, phone: input.phone || null, city: input.city || null }),
    onSuccess,
    onError,
  })
}
