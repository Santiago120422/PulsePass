import { z } from 'zod'

export const recentKey = 'pulsepass.recent'
const recentSchema = z.array(codeSchema).max(10)
export function readRecent() {
  try { return recentSchema.parse(JSON.parse(sessionStorage.getItem(recentKey) ?? '[]')) }
  catch { return [] }
}

import { codeSchema } from '../../lib/validacion'
