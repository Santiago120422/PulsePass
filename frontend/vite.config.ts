import react from '@vitejs/plugin-react'
import tailwind from '@tailwindcss/vite'
import { defineConfig, loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxy = { '/api': env.PULSEPASS_API_TARGET || 'http://localhost:8080' }
  return { plugins: [react(), tailwind()], server: { proxy }, preview: { proxy } }
})
