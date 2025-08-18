import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      // 这个是别名，@代表的就是./src这个路径
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      // 为啥加这个，见readme中创建一个应用
      vue: 'vue/dist/vue.esm-bundler.js'
    }
  }
})
