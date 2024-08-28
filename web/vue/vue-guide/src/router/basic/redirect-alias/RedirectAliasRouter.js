import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";
import HomeView from "./HomeView.vue";
import SearchView from "./SearchView.vue";
import ParentView from "./ParentView.vue";
import ChildView from "./ChildView.vue";

export const RedirectAliasRouter = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'homepage',
            component: HomeView,
            alias: ['/index']
        }, {
            path: '/home',
            redirect: '/'
        }, {
            path: '/homepage',
            redirect: { name: 'homepage' }
        }, {
            path: '/search',
            component: SearchView
        }, {
            // /search/screens -> /search?q=screens
            path: '/search/:searchText',
            redirect: to => {
                // 方法接收目标路由作为参数
                // return 重定向的字符串路径/路径对象
                return { path: '/search', query: { q: to.params.searchText } }
            },
        }, {
            path: '/parent',
            component: ParentView,
            redirect: '/',
            children: [
                {
                    path: 'child',
                    component: ChildView,
                    alias: ['/child', 'kid']
                }
            ]
        }
    ]
}
)