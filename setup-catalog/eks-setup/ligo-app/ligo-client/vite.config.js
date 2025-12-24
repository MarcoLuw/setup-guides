import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  // base: '/dashboard/',   # use when deploying with EKS to enable path-based routing
  plugins: [react()],
})
