import { createRouter, createWebHistory } from 'vue-router'
import UserPost from './UserPost.vue'
import NotFound from './NotFound.vue'

export const dmRouter = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/users/:username/posts/:postId', component: UserPost },
    // 相当于定义了一个路由器的通配表达式，可以用于表明未找到页面时应该被路由至什么页面，例如404
    { path: '/users/:pathMatch(.*)*', component: NotFound },
    { path: '/users/user-:pathMatch(.*)*', component: NotFound },
    { path: '/:pathMatch(.*)*', component: NotFound },
  ],
})
