import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag: string) => tag === 'lottie-player'
        }
      }
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000, // 端口
    host: '127.0.0.1',  // 使用IPv4地址
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        configure: (proxy: any, options: any) => {
          proxy.on('proxyReq', (proxyReq: any, req: any, res: any) => {
            // 添加CORS相关的请求头
            proxyReq.setHeader('Origin', 'http://127.0.0.1:3000');
            proxyReq.setHeader('Referer', 'http://127.0.0.1:3000/');
            proxyReq.setHeader('Host', 'localhost:8082');
          });
        }
      },
      '/user': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/user/, '/user'),
      },
      '/biyingapi': {
        target: 'https://api.biyingapi.com', // 正式API (HTTPS)
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/biyingapi/, ''),
        secure: false // 允许不安全的HTTPS连接
      },
      '/juheapi': {
        target: 'http://web.juhe.cn',
        changeOrigin: true,
        rewrite: (path: string) => path.replace(/^\/juheapi/, ''),
      }
    }
  }
}) 