import { createWebHistory } from "vue-router";
import { createRouter } from "vue-router";
import AvatarView from "./AvatarView.vue";
import UserSettings from "./UserSettings.vue";
import UserEmailsSubscriptions from "./UserEmailsSubscriptions.vue";
import UserProfile from "./UserProfile.vue";
import UserProfilePreview from "./UserProfilePreview.vue";
import Nothing from "./Nothing.vue";

export const namedViewRouter = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/settings',
            // 你也可以在顶级路由就配置命名视图
            component: UserSettings,
            children: [
                {
                    path: 'emails',
                    component: UserEmailsSubscriptions,
                    children: {
                        path: 'withAvatar',
                        component: AvatarView
                    }
                }, {
                    path: 'profile',
                    components: {
                        default: UserProfile,
                        helper: UserProfilePreview
                    },
                    children: [{
                        path: 'withAvatar',
                        component: AvatarView
                    }]
                }]
        }
    ]
})