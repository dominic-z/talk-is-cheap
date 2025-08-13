import { createMemoryHistory, createRouter } from 'vue-router'

import HomeView from './HomeView.vue'
import AboutView from './AboutView.vue'

const routes = [
  { path: '/', component: HomeView },
  { path: '/about', component: AboutView },
]

const router = createRouter({
  // 在演练场的示例里，我们使用了 createMemoryHistory()，它会完全忽略浏览器的 URL 而使用其自己内部的 URL。
  history: createMemoryHistory(),
  routes,
})

export default router