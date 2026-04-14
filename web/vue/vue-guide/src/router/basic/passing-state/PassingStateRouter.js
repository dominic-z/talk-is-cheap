import { createRouter, createWebHistory } from 'vue-router'
import List from './List.vue'
import Detail from './Detail.vue'

const routes = [
  { path: '/', redirect: '/list' }, // 默认重定向到列表页
  { path: '/list', component: List, name: 'List' },
  { path: '/detail', component: Detail, name: 'Detail' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router