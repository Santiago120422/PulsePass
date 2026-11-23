import { type ReactNode } from 'react'

export function Page({ title, description, children }: { title: string; description?: string; children: ReactNode }) {
  return <section className="space-y-7"><header className="space-y-2"><h1>{title}</h1>{description && <p className="muted">{description}</p>}</header>{children}</section>
}
