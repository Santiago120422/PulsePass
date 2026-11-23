import { createBrowserRouter } from 'react-router'
import { Layout } from './layouts/Layout'
import { RutaPorRol } from './layouts/RutaPorRol'
import { RutaProtegida } from './layouts/RutaProtegida'
import { SoloAnonimo } from './layouts/SoloAnonimo'
import { RUTAS } from './lib/rutas'
import { ErrorPagina } from './pages/ErrorPagina'
import { Inicio } from './pages/Inicio'
import { NoEncontrada } from './pages/NoEncontrada'
import { SinPermisos } from './pages/SinPermisos'

export const router = createBrowserRouter([{
  Component: Layout, ErrorBoundary: ErrorPagina, HydrateFallback: () => <p className="notice" role="status">Cargando PulsePass…</p>,
  children: [
    { index: true, Component: Inicio },
    { path: RUTAS.eventos, lazy: async () => ({ Component: (await import('./pages/CatalogoEventos')).Catalog }) },
    { path: RUTAS.patrones.evento, lazy: async () => ({ Component: (await import('./pages/DetalleEvento')).EventDetail }) },
    { path: RUTAS.artistas, lazy: async () => ({ Component: (await import('./pages/ListaArtistas')).Artists }) },
    { path: RUTAS.patrones.artista, lazy: async () => ({ Component: (await import('./pages/DetalleArtista')).ArtistDetail }) },
    { path: RUTAS.sedes, lazy: async () => ({ Component: (await import('./pages/ListaSedes')).Venues }) },
    { path: RUTAS.patrones.sede, lazy: async () => ({ Component: (await import('./pages/DetalleSede')).VenueDetail }) },
    { Component: SoloAnonimo, children: [
      { path: RUTAS.login, lazy: async () => ({ Component: (await import('./pages/Login')).Login }) },
      { path: RUTAS.registro, lazy: async () => ({ Component: (await import('./pages/Registro')).Register }) },
    ] },
    { Component: RutaProtegida, children: [
      { path: RUTAS.cuenta, lazy: async () => ({ Component: (await import('./pages/Cuenta')).Account }) },
    ] },
    { Component: () => <RutaPorRol roles={['USER']} />, children: [
      { path: RUTAS.tickets, lazy: async () => ({ Component: (await import('./pages/MisTickets')).Tickets }) },
      { path: RUTAS.patrones.ticket, lazy: async () => ({ Component: (await import('./pages/DetalleTicket')).TicketDetail }) },
      { path: RUTAS.patrones.confirmacion, lazy: async () => ({ Component: (await import('./pages/ConfirmacionCompra')).ConfirmacionCompra }) },
    ] },
    { Component: () => <RutaPorRol roles={['STAFF', 'ADMIN']} />, children: [
      { path: RUTAS.patrones.pagados, lazy: async () => ({ Component: (await import('./pages/TicketsPagadosEvento')).Paid }) },
      { path: RUTAS.validar, lazy: async () => ({ Component: (await import('./pages/ValidacionIngreso')).Validate }) },
      { path: RUTAS.pagados, lazy: async () => ({ Component: (await import('./pages/TicketsPagadosEvento')).Paid }) },
    ] },
    { Component: () => <RutaPorRol roles={['ADMIN']} />, children: [
      { path: RUTAS.admin, lazy: async () => ({ Component: (await import('./pages/AdminEventos')).Admin }) },
      { path: RUTAS.crear, lazy: async () => ({ Component: (await import('./pages/AdminCrearEvento')).CreateEvent }) },
      { path: RUTAS.patrones.gestionar, lazy: async () => ({ Component: (await import('./pages/AdminGestionEvento')).ManageEvent }) },
    ] },
    { path: RUTAS.prohibido, Component: SinPermisos },
    { path: RUTAS.perdido, Component: NoEncontrada },
    { path: '*', Component: NoEncontrada },
  ],
}])
