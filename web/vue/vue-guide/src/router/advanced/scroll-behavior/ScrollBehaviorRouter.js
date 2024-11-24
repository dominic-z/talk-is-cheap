import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";
import User from "./User.vue";
import { createWebHashHistory } from "vue-router";



let routes = [
    {
        path: '/user/:username',
        component: User,
    }
]


export const scrollBehaviorRouter = createRouter({
    history: createWebHashHistory(),
    routes: routes,
    scrollBehavior(to, from, savedPosition) {
        // return 期望滚动到哪个的位置
        // 始终滚动到顶部
        // console.log(to.hash)
        // return { top: 100, behavior: 'smooth', }
        // 挪到div右边100，上面100的地方
        // return { el: '#div', left: -100, top: 100 }
        // 挪到anchor右边100，下面面100的地方，丝滑
        return { el: '#anchor', left: -100, top: -100, behavior: 'smooth', }
    }
})