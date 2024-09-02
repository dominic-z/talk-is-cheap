import { createRouter } from 'vue-router'
import User from './User.vue'
import { createWebHistory } from 'vue-router'


const routes = [
  { path: '/user/:id', component: User , props: true},

//   相当于访问/userStaticProps的时候，id字段就会写死是abc
  { path: '/userStaticProps', component: User , props: {id:'abc'}},
//   function模式 直接访问/userFunctionProps?id=ccc
  { path: '/userFunctionProps', component: User , props: route=>({id:route.query.id})},
]

const passingPropsRouter = createRouter({
  history: createWebHistory(),
  routes,
})

export default passingPropsRouter