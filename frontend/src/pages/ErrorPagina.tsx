import { Link } from 'react-router'
import { Page } from '../componentes/Pagina'
import { RUTAS } from '../lib/rutas'

export function ErrorPagina() {
  return <div className="container-shell main"><Page title="Algo salió mal"><p>No pudimos cargar esta página.</p><Link className="primary" to={RUTAS.inicio}>Volver al inicio</Link></Page></div>
}
