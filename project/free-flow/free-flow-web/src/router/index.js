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
  },
  taskDefinitionManage: {
    name: 'taskDefinitionManage',
    path: '/task-definition-manage'
  },
  taskStartupManage: {
    name: 'taskStartupManage',
    path: '/task-startup-manage'
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
    {
      path: namedRouter.taskDefinitionManage.path,
      name: namedRouter.taskDefinitionManage.name,
      component: ClusterManageView,
    },
    {
      path: namedRouter.taskStartupManage.path,
      name: namedRouter.taskStartupManage.name,
      component: ClusterManageView,
    },

  ]
})

export default router

