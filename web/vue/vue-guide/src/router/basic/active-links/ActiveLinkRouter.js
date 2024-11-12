import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";
import User from "./User.vue";
import Role from "./Role.vue";



let routes = [
    {
        path: '/user/:username',
        component: User,
        children: [
            {
                path: 'role/:roleId',
                component: Role
            }
        ]
    }
]


export const activeLinkRouter = createRouter({
    history: createWebHistory(),
    routes: routes
})