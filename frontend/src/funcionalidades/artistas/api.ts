import { request } from '../../lib/http'
import { artist } from './tipos'

const enc = encodeURIComponent
export const artistasApi = {
  artists: (signal?: AbortSignal) => request(artist.array(), '/artists/active', 'GET', undefined, signal),
  artist: (artistId: number, signal?: AbortSignal) => request(artist, '/artists/' + artistId, 'GET', undefined, signal),
  artistByName: (name: string) => request(artist, '/artists/by-stage-name?stageName=' + enc(name)),
}
