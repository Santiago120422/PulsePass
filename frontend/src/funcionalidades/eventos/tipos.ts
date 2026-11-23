import { z } from 'zod'
import { codeSchema, required } from '../../lib/validacion'

import { dateTime, id, optional, text } from '../../lib/esquemas'
import { artist } from '../artistas/tipos'

export const category = z.enum(['MUSIC', 'SPORTS', 'TECHNOLOGY', 'EDUCATION', 'CULTURE', 'ENTERTAINMENT'])
export const eventStatus = z.enum(['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'])
export const summary = z.object({ id, eventCode: text, name: text, category, status: eventStatus, eventDate: dateTime, venueCode: text, venueName: text })
export const event = summary.extend({ description: optional, minimumAge: z.number().int(), streamingUrl: optional, artists: artist.array() })
export type EventCard = z.infer<typeof summary>

export const createSchema = z.object({
  eventCode: codeSchema, name: required.max(200), description: z.string().max(2000),
  category: z.string().refine(value => category.safeParse(value).success, 'Selecciona una categoría.'),
  eventDate: z.iso.datetime({ local: true, precision: -1, error: 'Ingresa una fecha y hora válidas.' }),
  minimumAge: required.regex(/^\d+$/, 'Ingresa un número entero mayor o igual a cero.').refine(value => Number(value) <= 2147483647, 'La edad ingresada es demasiado alta.'),
  venueCode: required,
})
