import { createRouter } from "vue-router"
import Post from "./Post.vue"
import { createWebHistory } from "vue-router"
import PostBeforeEnter from "./PostBeforeEnter.vue"



const routes = [
    {
        path: '/post/:id',
        component: Post,
    },
    {
        path: '/post/beforeEnterByQuery/:id',
        component: PostBeforeEnter,
        beforeEnter: async (to, from) => {
            const post = await getPost(to.params.id)
            console.log('post', post)
            console.log('to', to)

            if (Object.keys(to.query).length == 0) {
                return { path: to.path, query: { ...post }, props: 21 }
            }
        }
    }
]

async function getPost(id) {
    return new Promise((resolve, reject) => {
        setTimeout(resolve, 500)
    }).then(() => { return { title: id, body: "111" } })
}

export const dataFetchingRouter = createRouter(
    {
        routes: routes,
        history: createWebHistory()
    }
)