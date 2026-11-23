import { request } from '../../lib/http'
import { user } from './tipos'

const enc = encodeURIComponent
export const usuariosApi = {
  register: (body: unknown) => request(user, '/users', 'POST', body),
  byUsername: (name: string, signal?: AbortSignal) => request(user, '/users/by-username?username=' + enc(name), 'GET', undefined, signal),
  byEmail: (email: string, signal?: AbortSignal) => request(user, '/users/by-email?email=' + enc(email), 'GET', undefined, signal),
}
