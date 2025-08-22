import { createRouter, createWebHistory } from 'vue-router'
import ClusterManageView from '../views/ClusterManageView.vue'

export const namedRouter = {
  index: {
    name: 'index',
    path: '/'
  },
  clusterManage: {
    name: 'clusterManage',
    path: '/cluster-manage'
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: namedRouter.index.path,
      redirect: {
        name: namedRouter.clusterManage.name
      }
    },
    {
      path: namedRouter.clusterManage.path,
      name: namedRouter.clusterManage.name,
      component: ClusterManageView,
    },
  ]
})

export default router

