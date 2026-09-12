import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// El backend Spring Boot corre en http://localhost:8088 (server.port=8088).
// Usamos proxy de Vite para que el navegador vea todo en el mismo origen
// y no haga falta configurar CORS en el backend.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://localhost:8088',
        changeOrigin: true,
      },
    },
  },
})
