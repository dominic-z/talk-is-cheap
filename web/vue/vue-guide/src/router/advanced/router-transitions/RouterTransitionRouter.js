import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";
import User from "./User.vue";
import User2 from "./User2.vue";



const routes = [
    {
        path: '/custom-transition',
        component: User,
        meta: { transition: 'slide-left' },
    },
    {
        path: '/other-transition',
        component: User2,
        meta: { transition: 'slide-right' },
    },
]


export const routerTransitionRouter = createRouter({
    history: createWebHistory(),
    routes: routes
})