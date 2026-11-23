import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { z } from 'zod'
import { ErrorNote } from '../componentes/Aviso'
import { Input } from '../componentes/Campo'
import { Page } from '../componentes/Pagina'
import { useSession } from '../funcionalidades/auth/AuthContext'
import { useLogin } from '../funcionalidades/auth/hooks'
import { ApiError } from '../lib/http'
import { loginNavigation, RUTAS } from '../lib/rutas'
import { useValidatedForm } from '../lib/useValidatedForm'
import { required } from '../lib/validacion'

const loginSchema = z.object({ identifier: required, password: z.string().min(1, 'Ingresa tu contraseña.') })

export function Login() {
  const { expired } = useSession()
  const navigate = useNavigate()
  const location = useLocation()
  const state = loginNavigation.safeParse(location.state).data
  const form = useValidatedForm(loginSchema, { identifier: '', password: '' })
  const mutation = useLogin(() => navigate(state?.desde ?? RUTAS.inicio, { replace: true }), form.setApiErrors)
  const [showPassword, setShowPassword] = useState(false)
  return <div className="auth-panel"><Page title="Iniciar sesión" description="Accede a tu cuenta y gestiona tus entradas.">
    {expired && <p className="notice" role="status">Tu sesión expiró. Inicia sesión nuevamente.</p>}
    {state?.registrado && <p className="notice" role="status">Tu cuenta está lista. Ya puedes iniciar sesión.</p>}
    <form className="form" noValidate onChange={() => { if (mutation.isError) mutation.reset() }} onSubmit={form.handleSubmit(input => mutation.mutate(input))}>
      <Input label="Usuario o correo" autoComplete="username" error={form.formState.errors.identifier?.message} {...form.register('identifier')} />
      <Input label="Contraseña" type={showPassword ? 'text' : 'password'} autoComplete="current-password" error={form.formState.errors.password?.message} {...form.register('password')} />
      <button className="secondary" type="button" onClick={() => setShowPassword(visible => !visible)}>
        {showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
      </button>
      {mutation.error && <ErrorNote error={mutation.error} fallback={mutation.error instanceof ApiError && mutation.error.status === 401 && mutation.error.path === '/auth/login' ? 'Credenciales inválidas.' : undefined} />}
      <button className="primary" disabled={mutation.isPending}>{mutation.isPending ? 'Ingresando…' : 'Ingresar'}</button>
    </form>
    <p>¿Primera vez aquí? <Link className="text-link" to={RUTAS.registro}>Crea tu cuenta</Link></p>
  </Page></div>
}
