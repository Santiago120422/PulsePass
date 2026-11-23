import { request } from '../../lib/http'
import { event, summary } from './tipos'

const enc = encodeURIComponent
export const eventosApi = {
  events: (artistName: string, signal?: AbortSignal) => request(summary.array(), artistName ? '/events/by-artist?stageName=' + enc(artistName) : '/events/published', 'GET', undefined, signal),
  event: (code: string, signal?: AbortSignal) => request(event, '/events/' + enc(code), 'GET', undefined, signal),
  createEvent: (body: unknown) => request(event, '/events', 'POST', body),
  publish: (code: string) => request(event, '/events/' + enc(code) + '/publish', 'PATCH'),
  associate: (code: string, artistId: number) => request(event, '/events/' + enc(code) + '/artists/' + artistId, 'POST'),
}
