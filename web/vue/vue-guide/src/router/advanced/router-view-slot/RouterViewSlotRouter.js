import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";
import User from "./User.vue";



let routes = [
    {
        path: '/user/:username',
        component: User,
    }
]


export const routerViewSlotRouter = createRouter({
    history: createWebHistory(),
    routes: routes
})