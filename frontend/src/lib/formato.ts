export const labels: Record<string, string> = {
  MUSIC: 'Música', SPORTS: 'Deportes', TECHNOLOGY: 'Tecnología', EDUCATION: 'Educación',
  CULTURE: 'Cultura', ENTERTAINMENT: 'Entretenimiento', PUBLISHED: 'Publicado', DRAFT: 'Borrador',
  SOLD_OUT: 'Agotado', CANCELLED: 'Cancelado', FINISHED: 'Finalizado', RESERVED: 'Reservado',
  PAID: 'Pagado', USED: 'Usado', GENERAL: 'General', VIP: 'VIP', BACKSTAGE: 'Backstage', STUDENT: 'Estudiante',
}
export const date = (value: string) => new Intl.DateTimeFormat('es-CO', { dateStyle: 'long', ...(value.includes('T') && { timeStyle: 'short' }), timeZone: 'America/Bogota' }).format(new Date(value.includes('T') ? /(?:Z|[+-]\d\d:\d\d)$/.test(value) ? value : value + '-05:00' : value + 'T12:00:00-05:00'))
export const price = (value: number) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2, minimumFractionDigits: 0 }).format(value)
