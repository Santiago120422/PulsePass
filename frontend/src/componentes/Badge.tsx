import { labels } from '../lib/formato'

export function Badge({ value }: { value: string }) {
  return <span className="badge" data-status={value}>{labels[value] ?? value}</span>
}
