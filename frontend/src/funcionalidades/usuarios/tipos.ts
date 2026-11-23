import { z } from 'zod'
import { required } from '../../lib/validacion'

import { id, optional, text } from '../../lib/esquemas'

export const user = z.object({ id, username: text, email: z.email(), active: z.boolean(), firstName: optional, lastName: optional, phone: optional, city: optional, birthDate: z.iso.date().nullish() })

export const registerSchema = z.object({
  username: required.max(100), email: z.email('Ingresa un correo válido.').max(150),
  firstName: required.max(100), lastName: required.max(100),
  phone: z.string().trim().max(30), city: z.string().trim().max(100),
  birthDate: z.iso.date('Ingresa tu fecha de nacimiento.'),
  password: z.string().min(12, 'Usa mínimo 12 caracteres.').max(72, 'Usa máximo 72 caracteres.'),
})
