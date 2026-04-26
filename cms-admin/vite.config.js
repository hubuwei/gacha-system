import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/cms/',  // CMS部署在/cms子路径下
  server: {
    port: 5174,
    open: false,  // 不自动打开浏览器
    historyApiFallback: true,  // SPA路由fallback
  },
  define: {
    'import.meta.env.VITE_API_BASE_URL': JSON.stringify('/api/cms')
  }
})