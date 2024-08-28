import OrderId from "./OrderId.vue"
import ProductName from "./ProductName.vue"
import { createRouter, createWebHistory } from "vue-router"
import RepeatC from "./RepeatC.vue"
import NotFound from "./NotFound.vue"

export const routeMatchingRouter = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/o/:orderId', component: OrderId },
        { path: '/p/:productName', component: ProductName },
        { path: '/:orderId(\\d+)', component: OrderId },
        { path: '/:productName', component: ProductName },
        { path: '/repeat/:c(\\d+)+', component: RepeatC },
        { path: '/:notfound(.*)*', component: NotFound },
    ]
})