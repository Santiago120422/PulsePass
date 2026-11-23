import { request } from '../../lib/http'
import { venue } from './tipos'

const enc = encodeURIComponent
export const sedesApi = {
  venues: (signal?: AbortSignal) => request(venue.array(), '/venues/active', 'GET', undefined, signal),
  venue: (code: string, signal?: AbortSignal) => request(venue, '/venues/' + enc(code), 'GET', undefined, signal),
}
