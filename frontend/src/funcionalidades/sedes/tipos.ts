import { z } from 'zod'

import { id, text } from '../../lib/esquemas'

export const venue = z.object({ id, code: text, name: text, city: text, address: text, capacity: z.number().int(), active: z.boolean() })
