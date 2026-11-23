import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import { useSession } from '../funcionalidades/auth/AuthContext'
import { useProfile } from '../funcionalidades/usuarios/hooks'
import { RUTAS } from '../lib/rutas'

function AccountBadge({ username }: { username: string }) {
  const { data: profile } = useProfile()
  const initials = [profile?.firstName, profile?.lastName]
    .map(name => Array.from(name?.trim() ?? '')[0] ?? '')
    .join('')
  const avatar = (initials || Array.from(username).slice(0, 2).join('')).toLocaleUpperCase('es-CO')

  return <Link className="account-badge" to={RUTAS.cuenta} aria-label={`Mi cuenta, ${username}`}>
    <span className="account-avatar" aria-hidden="true">{avatar}</span>
    <span className="account-name">{username}</span>
  </Link>
}

export function Layout() {
  const { session, logout, expired } = useSession()
  const navigate = useNavigate()
  const location = useLocation()
  const [theme, setTheme] = useState(() => {
    const value = localStorage.getItem('pulsepass.theme')
    return value === 'dark' || (value !== 'light' && matchMedia('(prefers-color-scheme: dark)').matches)
  })
  const [menu, setMenu] = useState<string>()
  const roles = session.kind === 'ready' ? session.principal.roles : []
  const open = menu === location.key
  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme)
  }, [theme])
  useEffect(() => {
    if (expired && location.pathname !== RUTAS.login) {
      void navigate(RUTAS.login, { replace: true, state: { desde: location.pathname + location.search } })
    }
  }, [expired, location.pathname, location.search, navigate])
  return <div className="site" onKeyDown={event => {
    if (event.key === 'Escape' && open) {
      setMenu(undefined)
      document.getElementById('menu-button')?.focus()
    }
  }}>
    <a className="skip" href="#contenido">Saltar al contenido</a>
    <header className="site-header">
      <div className="container-shell header-inner">
        <Link to={RUTAS.inicio} className="brand" aria-label="PulsePass, inicio"><span className="brand-mark" aria-hidden="true"><svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"><path d="M4 7h16v3a2 2 0 0 0 0 4v3H4v-3a2 2 0 0 0 0-4V7Zm9 0v2m0 2v2m0 2v2" /></svg></span>PulsePass</Link>
        <button type="button" className="secondary menu-button" id="menu-button" aria-expanded={open} aria-controls="principal" onClick={() => setMenu(open ? undefined : location.key)}>Menú</button>
        <nav id="principal" aria-label="Navegación principal" data-open={open}>
          <NavLink to={RUTAS.eventos}>Eventos</NavLink><NavLink to={RUTAS.artistas}>Artistas</NavLink><NavLink to={RUTAS.sedes}>Sedes</NavLink>
          {roles.includes('USER') && <NavLink to={RUTAS.tickets}>Mis tickets</NavLink>}
          {session.kind === 'ready' && <NavLink to={RUTAS.cuenta}>Mi cuenta</NavLink>}
          {(roles.includes('STAFF') || roles.includes('ADMIN')) && <><NavLink to={RUTAS.validar}>Validar ingreso</NavLink><NavLink to={RUTAS.pagados}>Tickets pagados</NavLink></>}
          {roles.includes('ADMIN') && <NavLink to={RUTAS.admin}>Administración</NavLink>}
        </nav>
        <div className="header-actions">
          <button className="theme" type="button" aria-label="Modo oscuro" aria-pressed={theme} onClick={() => {
            localStorage.setItem('pulsepass.theme', theme ? 'light' : 'dark')
            setTheme(!theme)
          }}><span aria-hidden="true">{theme ? '☀' : '☾'}</span></button>
          {session.kind === 'ready'
            ? <>
              <AccountBadge username={session.principal.username} />
              <button className="secondary" onClick={() => { logout(); navigate(RUTAS.inicio, { replace: true }) }}>Cerrar sesión</button>
            </>
            : session.kind !== 'loading' && <><Link className="secondary" to={RUTAS.login}>Ingresar</Link><Link className="primary" to={RUTAS.registro}>Registrarse</Link></>}
        </div>
      </div>
    </header>
    <main className="container-shell main" id="contenido" tabIndex={-1}><Outlet /></main>
    <footer className="site-footer"><div className="container-shell flex flex-wrap justify-between gap-3"><Link className="font-bold" to={RUTAS.inicio}>PulsePass</Link><div className="flex gap-3"><Link to={RUTAS.sedes}>Sedes</Link><Link to={RUTAS.artistas}>Artistas</Link></div></div></footer>
  </div>
}
