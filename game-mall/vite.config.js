import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // 解决 sockjs-client 的 global 问题
  define: {
    global: 'globalThis',
  },
  server: {
    port: 5173,
    proxy: {
      // 静态图片资源代理 - GamePapers 目录
      // 注意：更具体的规则必须放在前面，否则会被 /api 规则先匹配
      '/api/GamePapers': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
        // 不需要 rewrite，因为后端已经配置了 /api/GamePapers/**
      },
      // 将认证相关的请求代理到 auth-service (8084端口)
      '/api/auth': {
        target: 'http://localhost:8084',
        changeOrigin: true,
        secure: false
      },
      // WebSocket代理
      '/api/ws': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        ws: true,  // 启用WebSocket代理
        secure: false
      },
      // 将所有其他 /api 开头的请求代理到 mall-service
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
