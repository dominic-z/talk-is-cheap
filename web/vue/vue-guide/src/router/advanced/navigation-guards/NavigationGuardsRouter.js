import { createRouter } from "vue-router"
import User from "./User.vue"
import { createWebHashHistory } from "vue-router"
import { createWebHistory } from "vue-router"
import Profile from "./Profile.vue"
import { inject } from "vue"
import Avatar from "./Avatar.vue"


const routes = [
    { path: '/user/:id', component: User, props: true, name: 'id' },
    { path: '/avatar', component: Avatar, props: true, name: 'avatar' },
    {
        path: '/showYourself/:id', component: Profile, props: true, meta: { needCamera: true },
        beforeEnter: (to, from) => {
            console.log('/showYourself', 'to', to)
            console.log('/showYourself', 'from', from)
            console.log('/showYourself', 'to.params', to.params)
            console.log('/showYourself', 'to.query', to.query)
            if (Object.keys(to.query).length) {
                // 如果有query，则会这个beforeEnter方法去掉query，然后因为没有改变路径，因此还会进入到该beforeEnter方法
                // 但第二次不会进入这个if判断中
                return { path: to.path, query: {} }
            }
        }

    },
]

export const navigationGuardsRouter = createRouter({
    routes: routes,
    history: createWebHistory()
})

navigationGuardsRouter.beforeResolve(async (to, from) => {
    if (to.meta.needCamera) {
        console.log('to.meta.needCamera', to.meta.needCamera)
    }
})

navigationGuardsRouter.beforeEach(async (to, from) => {
    console.log(to)
    console.log(from)

    // 等待1s
    await new Promise((resolve, reject) => {
        setTimeout(resolve, 500)
    }).then(() => { console.log('wait done') })
    console.log("inject('global')  ", inject('global'))
    console.log("to.params", to.params)
    console.log("to.meta", to.meta)
    console.log("to.name", to.name)
    if (to.name === 'id') {
        if (to.params.id === '1') {
            // 如果id是1，会被转到/user/2这个路由
            return '/user/2'
        } else if (to.params.id === '0') {
            throw new Error('Parameter is not a number!');
        } else if (to.params.id === '3') {
            // 如果id是1，会被转到/user/2这个路由
            return '/avatar'
        } else {
            return true
        }
    }
    return true
})

navigationGuardsRouter.onError(() => {
    console.log('error')
    navigationGuardsRouter.push('/user/-1')
})

navigationGuardsRouter.afterEach((to, from, failure) => {
    if (!failure) {
        console.log(failure)
    }
})