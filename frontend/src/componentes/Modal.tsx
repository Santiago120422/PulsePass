import { useEffect, useId, useRef, type ReactNode } from 'react'

export function Modal({ title, close, busy, children }: { title: string; close: () => void; busy?: boolean; children: ReactNode }) {
  const ref = useRef<HTMLDialogElement>(null)
  const id = useId()
  useEffect(() => {
    const dialog = ref.current
    dialog?.showModal()
    return () => { dialog?.close() }
  }, [])
  return <dialog ref={ref} aria-labelledby={id} onCancel={event => { event.preventDefault(); if (!busy) close() }}>
    <div className="space-y-5"><h2 id={id}>{title}</h2>{children}
      <button className="secondary" type="button" disabled={busy} onClick={close}>Volver</button></div>
  </dialog>
}
