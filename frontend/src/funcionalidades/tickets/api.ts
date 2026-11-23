import { z } from 'zod'

import { request } from '../../lib/http'
import { ticket, ticketType } from './tipos'

const enc = encodeURIComponent
export const ticketsApi = {
  prices: (signal?: AbortSignal) => request(z.record(ticketType, z.number().nonnegative()), '/tickets/prices', 'GET', undefined, signal),
  purchase: (body: { userEmail: string; eventCode: string; type: z.infer<typeof ticketType> }) => request(ticket, '/tickets', 'POST', body),
  ticket: (code: string, signal?: AbortSignal) => request(ticket, '/tickets/' + enc(code), 'GET', undefined, signal),
  tickets: (email: string, signal?: AbortSignal) => request(ticket.array(), '/tickets/by-user?email=' + enc(email), 'GET', undefined, signal),
  paid: (code: string, signal?: AbortSignal) => request(ticket.array(), '/events/' + enc(code) + '/tickets/paid', 'GET', undefined, signal),
  cancel: (code: string) => request(ticket, '/tickets/' + enc(code) + '/cancel', 'PATCH'),
  use: (code: string) => request(ticket, '/tickets/' + enc(code) + '/use', 'PATCH'),
}
