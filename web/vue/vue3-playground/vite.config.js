import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path';
// https://vite.dev/config/

// vite工程的vite.config.js的写法和vue工程的vue.config.js不太一样，但是作用是相同的。

export default defineConfig({
  plugins: [vue()],
  resolve: {
//     // 引入 src/components 目录下的组件
// import MyComponent from '@components/MyComponent.vue';

// // 引入 src/views 目录下的视图
// import MyView from '@/views/MyView.vue';
    alias: {
      '@': path.resolve(__dirname, 'src'),
      '@components': path.resolve(__dirname, 'src/components')
    }
    
  },

  // 浏览器出于安全考虑，实施了同源策略。这意味着当浏览器发起请求时，若请求的目标 URL 与当前页面的协议、域名或端口号不一致，就会产生跨域问题，请求可能会被浏览器阻止。在开发 Vue 项目时，前端应用通常运行在本地的一个端口（如 http://localhost:8080），而后端 API 服务可能运行在另一个端口（如 http://localhost:3000），此时就会出现跨域问题。
// vite.config.js 中的 proxy 配置项可以让你在开发环境中创建一个代理服务器。当你发起请求时，请求会先发送到本地的代理服务器，然后由代理服务器将请求转发到实际的后端 API 服务器，这样浏览器会认为请求是同源的，从而避免跨域问题。
//   在这个示例中：
// /api 是代理的匹配规则，表示当请求的 URL 以 /api 开头时，会触发代理。
// target 指定了后端 API 服务器的地址，请求会被转发到这个地址。
// changeOrigin 设置为 true 表示改变请求源，让服务器认为请求是从代理服务器发出的。
// rewrite 用于重写请求路径，将 /api 替换为空，这样请求转发到后端时就不会包含 /api。
// 仅适用于开发环境：proxy 配置只在开发环境（npm run serve）中生效，生产环境需要在服务器端进行跨域配置。
// 说人话就是，举个例子，如果有个axios实例访问了本地的/api，那实际上访问的是http://localhost:3000这个服务
  server: {
    proxy: {
        '/httpbin-api': {
            target: 'http://httpbin.org/', // 后端 API 服务器地址
            changeOrigin: true, // 是否改变请求源
            rewrite: (path) => path.replace(/^\/httpbin-api/, ''),
       },
        '/local': {
            target: 'http://localhost:8082/', // 后端 API 服务器地址
            changeOrigin: true, // 是否改变请求源
            rewrite: (path) => path.replace(/^\/local/, ''),
            // 是否启用 WebSocket 服务（通常无需手动设置，Vite 默认启用）
            ws: true, // 表示开启 WebSocket 代理。如果后端服务使用了 WebSocket，那么这个选项需要设置为 true
        },
    }
}
})
