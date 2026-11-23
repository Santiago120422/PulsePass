import { request } from '../../lib/http'
import { auth, me } from './tipos'

export const authApi = {
  login: (body: unknown) => request(auth, '/auth/login', 'POST', body),
  me: (signal?: AbortSignal) => request(me, '/auth/me', 'GET', undefined, signal),
}
