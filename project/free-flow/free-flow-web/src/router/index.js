import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

export const namedRouter = {
  index: {
    name: 'index',
    path: ''/''
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: namedRouter.index.path,
      name: namedRouter.index.name,
      component: HomeView,
    },
  ]
})

export default router

