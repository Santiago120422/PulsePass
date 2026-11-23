import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, type DefaultValues, type FieldValues, type Path } from 'react-hook-form'
import { z } from 'zod'
import { ApiError } from './http'

export function useValidatedForm<T extends FieldValues>(schema: z.ZodType<T, T>, defaults?: DefaultValues<NoInfer<T>>) {
  const form = useForm<T>({ resolver: zodResolver(schema), defaultValues: defaults })
  function setApiErrors(error: Error) {
    if (!(error instanceof ApiError) || error.status !== 400) return
    for (const [name, message] of Object.entries(error.details)) {
      if (Object.hasOwn(form.getValues(), name)) form.setError(name as Path<T>, { type: 'server', message })
    }
  }
  return { ...form, setApiErrors }
}
