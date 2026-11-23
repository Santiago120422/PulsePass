import { useMutation } from '@tanstack/react-query'
import { useSession } from './AuthContext'

export function useLogin(onSuccess: () => void, onError: (error: Error) => void) {
  const { login } = useSession()
  return useMutation({ mutationFn: login, onSuccess, onError })
}
