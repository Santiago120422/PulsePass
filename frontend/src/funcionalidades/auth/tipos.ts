import { z } from 'zod'

import { id, text } from '../../lib/esquemas'

export const role = z.enum(['USER', 'STAFF', 'ADMIN'])
export type Role = z.infer<typeof role>
export const me = z.object({ id, username: text, roles: role.array() })
export const auth = z.object({ accessToken: text.min(1), tokenType: z.literal('Bearer'), expiresIn: z.number().positive() })
export type Principal = z.infer<typeof me>
