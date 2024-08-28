
import { createRouter } from "vue-router";
import User from "./User.vue";
import UserProfile from "./UserProfile.vue";
import { createWebHistory } from "vue-router";
import NotAssign from "./NotAssign.vue";

export const nestedRouter = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/user/:id', component: User,
            children: [
                { path: 'profile', component: UserProfile },
                { path: '', component: NotAssign }
            ]
        },
        // 该route永远不会被命中，因为会先被上面的route拦截
        {
            path: '/user/:id/profile', component: NotAssign,

        },
    ]
})