import { Link } from 'react-router'
import { RUTAS } from '../lib/rutas'
import { Page } from './Pagina'

export function Missing({ forbidden = false }: { forbidden?: boolean }) {
  return <Page title={forbidden ? 'Acceso restringido' : 'Página no encontrada'}>
    <p>{forbidden ? 'Tu cuenta no tiene permisos para esta sección.' : 'La página que buscas ya no está disponible.'}</p>
    <Link className="primary" to={RUTAS.inicio}>Volver al inicio</Link>
  </Page>
}
