import { defineConfig } from 'vite'
import { resolve } from 'path'

export default defineConfig({
  root: 'src/main/frontend',

  build: {
    outDir: '../resources/static/dist',
    emptyOutDir: true,
    manifest: true,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'src/main/frontend/css/main.css'),
        app: resolve(__dirname, 'src/main/frontend/js/main.ts'),
      },
    },
  },

  server: {
    port: 5173,
    strictPort: true,
    // Proxy API requests to Spring Boot during development
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },

  // Ensure assets are served from the correct path
  base: process.env.NODE_ENV === 'production' ? '/dist/' : '/',
})
