import { createWebHistory } from "vue-router"
import { createRouter } from "vue-router"
import PostsLayout from "./PostsLayout.vue"
import PostsNew from "./PostsNew.vue"
import PostsDetail from "./PostsDetail.vue"
import Login from "./Login.vue"
const routes = [
    {
        path: '/posts',
        component: PostsLayout,
        children: [
            {
                path: 'new',
                component: PostsNew,
                // 只有经过身份验证的用户才能创建帖子
                meta: { requiresAuth: true },
            },
            {
                path: ':id',
                component: PostsDetail,
                // 任何人都可以阅读文章
                meta: { requiresAuth: false },
            }
        ]
    },
    {
        path: '/login',
        component: Login
    }
]

export const metaRouter = createRouter({
    history: createWebHistory(),
    routes: routes
})

let auth = { isLoggedIn: () => false }
metaRouter.beforeEach((to, from) => {
    // 而不是去检查每条路由记录
    // to.matched.some(record => record.meta.requiresAuth)
    if (to.meta.requiresAuth && !auth.isLoggedIn()) {
        // 此路由需要授权，请检查是否已登录
        // 如果没有，则重定向到登录页面
        return {
            path: '/login',
            // 保存我们所在的位置，以便以后再来
            query: { redirect: to.fullPath },
        }
    }
})