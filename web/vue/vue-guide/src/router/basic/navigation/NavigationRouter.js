import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";
import User from "./User.vue";


export const navigationRouter = createRouter({
    history: createWebHistory(),
    routes: [{ path: '/user/:id', name: 'user', component: User }]
})