import UserProfile from "@/router/basic/nested-routes/UserProfile.vue"
import { createRouter,createWebHistory } from "vue-router"

const routes = [
  {
    path: '/user/:username',
    name: 'profile', 
    component: UserProfile
  }
]

export const namedRouter = createRouter({
    history: createWebHistory(),
    routes:routes
})