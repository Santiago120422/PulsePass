import type { ComponentProps, ReactNode } from 'react';
import { useId } from 'react';

export function Field({ label, error, children }: { label: string; error?: string; children: (id: string, description?: string) => ReactNode }) {
  const id = useId()
  const description = error ? id + '-error' : undefined
  return <div className="field"><label htmlFor={id}>{label}</label>{children(id, description)}
    {error && <p id={description} className="field-error" role="alert">{error}</p>}</div>
}

export function Input({ label, error, ...props }: ComponentProps<'input'> & { label: string; error?: string }) {
  return <Field label={label} error={error}>{(id, description) => <input {...props} id={id} aria-describedby={description} aria-invalid={!!error} />}</Field>
}
