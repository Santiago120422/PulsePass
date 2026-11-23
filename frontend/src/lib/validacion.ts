import { z } from 'zod'

export const required = z.string().trim().min(1, 'Este campo es obligatorio.')
export const codeSchema = required.max(50, 'Usa máximo 50 caracteres.').refine(value => !['.', '..'].includes(value) && !/[/?#]/.test(value), 'Ingresa un código válido.')
export const lookupSchema = z.object({ value: required.max(200, 'Usa máximo 200 caracteres.') })
export const codeLookupSchema = z.object({ value: codeSchema })
export const positiveId = z.string().regex(/^[1-9]\d*$/).transform(Number).pipe(z.number().int().positive())
