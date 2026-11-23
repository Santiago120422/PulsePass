import { Link, useNavigate } from 'react-router'
import { ErrorNote } from '../componentes/Aviso'
import { Input } from '../componentes/Campo'
import { Page } from '../componentes/Pagina'
import { useRegister } from '../funcionalidades/usuarios/hooks'
import { registerSchema } from '../funcionalidades/usuarios/tipos'
import { ApiError } from '../lib/http'
import { RUTAS } from '../lib/rutas'
import { useValidatedForm } from '../lib/useValidatedForm'

const fields = [
  ['username', 'Nombre de usuario', 'text', 'username'], ['email', 'Correo electrónico', 'email', 'email'],
  ['firstName', 'Nombres', 'text', 'given-name'], ['lastName', 'Apellidos', 'text', 'family-name'],
  ['phone', 'Teléfono (opcional)', 'tel', 'tel'], ['city', 'Ciudad (opcional)', 'text', 'address-level2'],
  ['birthDate', 'Fecha de nacimiento', 'date', 'bday'], ['password', 'Contraseña', 'password', 'new-password'],
] as const

export function Register() {
  const form = useValidatedForm(registerSchema, { username: '', email: '', firstName: '', lastName: '', phone: '', city: '', birthDate: '', password: '' })
  const navigate = useNavigate()
  const mutation = useRegister(() => navigate(RUTAS.login, { replace: true, state: { registrado: true } }), form.setApiErrors)
  return <div className="auth-panel"><Page title="Crear cuenta" description="Completa tus datos para reservar y consultar tus entradas.">
    <form className="form" noValidate onSubmit={form.handleSubmit(input => mutation.mutate(input))}>
      <div className="grid gap-4 sm:grid-cols-2">{fields.map(([name, label, type, autoComplete]) =>
        <Input key={name} label={label} type={type} autoComplete={autoComplete} error={form.formState.errors[name]?.message} {...form.register(name)} />)}</div>
      {mutation.error && <ErrorNote error={mutation.error} fallback={mutation.error instanceof ApiError && mutation.error.status === 409 ? 'El usuario o correo ya está registrado.' : undefined} />}
      <button className="primary" disabled={mutation.isPending}>{mutation.isPending ? 'Creando cuenta…' : 'Registrarme'}</button>
    </form>
    <Link className="text-link" to={RUTAS.login}>Ya tengo cuenta</Link>
  </Page></div>
}
