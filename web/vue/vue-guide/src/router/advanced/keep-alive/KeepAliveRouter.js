// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import List from './List.vue'
import Detail from './Detail.vue'
import ListWrapper from './ListView.vue'
import ListView from './ListView.vue'
import KeepAliveListView from './KeepAliveListView.vue'

const routes = [
  {
    path: '/',
    name: 'List',
    component: List,
    meta: { title: '用户列表' }
  },
  {
    path: '/ListView',
    name: 'ListView',
    component: ListView,
    meta: { title: '用户列表-List被包装一层' }
  },
  {
    path: '/KeepAliveListView',
    name: 'KeepAliveListView',
    component: KeepAliveListView,
    meta: { title: '用户列表-List被包装一层',isKeepAlive:true } // 通过isKeepAlive:true这个meta告诉路由，具备这个meta的都是需要keep alive的，并且这个keep alive还tm支持修改是最过分的。。。
  },
  {
    path: '/detail/:id',
    name: 'Detail',
    component: Detail,
    meta: { title: '用户详情' }
  }
]

const keepAliveRouter = createRouter({
  history: createWebHistory(),
  routes
})

export default keepAliveRouter