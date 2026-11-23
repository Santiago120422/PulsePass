import '@fontsource-variable/inter'
import '@fontsource/poppins/latin-700.css'
import '@fontsource/poppins/latin-800.css'
import { QueryClientProvider } from '@tanstack/react-query'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router/dom'
import { SessionProvider } from './funcionalidades/auth/AuthContext'
import './index.css'
import { client } from './lib/queryClient'
import { router } from './router'

const root = document.getElementById('root')
if (root) createRoot(root).render(<QueryClientProvider client={client}><SessionProvider><RouterProvider router={router} /></SessionProvider></QueryClientProvider>)
