import type { EventCard } from '../tipos'

const icons: Record<EventCard['category'], string> = {
  MUSIC: 'M9 18V5l12-2v13M9 18a3 3 0 1 1-3-3c2 0 3 1 3 3Zm12-2a3 3 0 1 1-3-3c2 0 3 1 3 3Z',
  SPORTS: 'M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0ZM12 2v20M2 12h20',
  TECHNOLOGY: 'M4 4h16v12H4zM12 16v5m-4 0h8',
  EDUCATION: 'm2 9 10-5 10 5-10 5-10-5Zm4 2v6l6 3 6-3v-6',
  CULTURE: 'm2 9 10-6 10 6M5 10v9m5-9v9m4-9v9m5-9v9M3 21h18',
  ENTERTAINMENT: 'm12 2 3 6 7 1-5 5 1 7-6-3-6 3 1-7-5-5 7-1 3-6Z',
}

export function CategoryIcon({ category }: { category: EventCard['category'] }) {
  return <svg aria-hidden="true" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"><path d={icons[category]} /></svg>
}
