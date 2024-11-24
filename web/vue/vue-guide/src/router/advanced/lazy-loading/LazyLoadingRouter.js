import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";



let routes = [
    {
        path: '/user/:username',
        component: () => import('./User.vue'),
    }
]


export const lazyLoadingRouter = createRouter({
    history: createWebHistory(),
    routes: routes
})