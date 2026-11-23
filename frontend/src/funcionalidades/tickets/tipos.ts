import { z } from 'zod'

import { dateTime, id, text } from '../../lib/esquemas'

export const ticketType = z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'])
export const ticketStatus = z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED'])
export const ticket = z.object({ id, ticketCode: text, type: ticketType, status: ticketStatus, price: z.number(), purchaseDate: dateTime, userEmail: z.email(), eventCode: text, eventName: text })
export type Ticket = z.infer<typeof ticket>
