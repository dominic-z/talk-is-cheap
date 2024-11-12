import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";
import User from "./User.vue";
import Role from "./Role.vue";
import { createWebHashHistory } from "vue-router";



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

// 当使用hashmode的时候，如果访问主页：http://localhost:5173/
// url会自动变成http://localhost:5173/#
// 输入http://localhost:5173/user/erina无法命中任何route，并且url也会变成http://localhost:5173/user/erina#
// http://localhost:5173/#/user/erina才能命中'/user/:username'
export const historyModeRouter = createRouter({
    history: createWebHashHistory(),
    routes: routes
})