import { z } from 'zod'

export const id = z.number().int().positive()
export const text = z.string()
export const optional = text.nullish()
export const dateTime = z.iso.datetime({ local: true })
