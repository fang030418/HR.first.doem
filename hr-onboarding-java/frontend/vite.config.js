import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    // host: true 表示监听 0.0.0.0，避免默认只绑 IPv6 回环导致 127.0.0.1 访问不到
    host: true,
    port: 5173,
    strictPort: true,
    proxy: {
      // 前后端分离：devServer 代理 /api 到后端，避免开发期跨域
      // 默认指向 SQLite 实例(9090)；后端跑 MySQL 时用环境变量切换：
      //   VITE_API_TARGET=http://127.0.0.1:9091 npm run dev
      '/api': {
        target: process.env.VITE_API_TARGET || 'http://127.0.0.1:9090',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false
  }
})