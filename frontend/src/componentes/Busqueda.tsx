import { useValidatedForm } from '../lib/useValidatedForm'
import { codeLookupSchema, lookupSchema } from '../lib/validacion'
import { Input } from './Campo'

export function Lookup({ label, action = 'Buscar', initial = '', submit, code = true }: { label: string; action?: string; initial?: string; submit: (value: string) => void; code?: boolean }) {
  const form = useValidatedForm(code ? codeLookupSchema : lookupSchema, { value: initial })
  return <form className="lookup" noValidate onSubmit={form.handleSubmit(({ value }) => submit(value))}>
    <Input label={label} error={form.formState.errors.value?.message} {...form.register('value')} />
    <button className="primary">{action}</button>
  </form>
}
