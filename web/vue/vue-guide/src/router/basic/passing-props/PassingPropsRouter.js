import { createRouter } from 'vue-router'
import User from './User.vue'
import { createWebHistory } from 'vue-router'


const routes = [
  // 相当于:id就会被指定为User组件的id参数字段
  { path: '/user/:id', component: User, props: true },

  //   相当于访问/userStaticProps的时候，id字段就会写死是abc
  { path: '/userStaticProps', component: User, props: { id: 'abc' } },
  //   function模式 直接访问/userFunctionProps?id=ccc
  { path: '/userFunctionProps', component: User, props: route => ({ id: route.query.id }) },
  //   实验，尝试直接通过props传递对象试试，直接访问/userObjProps?id=ccc
  // 你可以创建一个返回 props 的函数。这允许你将参数转换为其他类型，将静态值与基于路由的值相结合等等。
  { path: '/userObjProps', component: User, props: route => ({ id: route.query.id, obj: { id: 'aaa' } }) },
]

const passingPropsRouter = createRouter({
  history: createWebHistory(),
  routes,
})

export default passingPropsRouter