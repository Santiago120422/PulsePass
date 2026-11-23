import { z } from 'zod'

import { id, optional, text } from '../../lib/esquemas'

export const artist = z.object({ id, stageName: text, country: optional, genre: optional, active: z.boolean() })
