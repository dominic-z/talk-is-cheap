import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/HomeView.vue') },
  { path: '/favorites', name: 'Favorites', component: () => import('../views/FavoritesView.vue') },
  { path: '/plan/create', name: 'PlanCreate', component: () => import('../views/PlanCreateView.vue') },
  { path: '/plan/:id', name: 'PlanDetail', component: () => import('../views/PlanDetailView.vue') },
  { path: '/plan/:id/day/:dayId', name: 'DayPlan', component: () => import('../views/DayPlanView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
