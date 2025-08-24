import { createRouter, createWebHistory } from 'vue-router'
import ClusterManageView from '../views/ClusterManageView.vue'
import TaskDefinitionView from '@/views/TaskDefinitionView.vue'
import IndexView from '@/views/IndexView.vue'
import TaskDefinitionDetailView from '@/views/TaskDefinitionDetailView.vue'

export const namedRoutes = {
  index: {
    name: 'index',
    path: '/',
    component: IndexView,
    clusterManage: {
      name: 'clusterManage',
      path: 'cluster-manage',
      component: ClusterManageView
    },
    taskDefinitionManage: {
      name: 'taskDefinitionManage',
      path: 'task-definition-manage',
      component: TaskDefinitionView
    },
    taskStartupManage: {
      name: 'taskStartupManage',
      path: 'task-startup-manage',
      component: ClusterManageView
    }
  },

  taskDefinitionDetail:{
    name: 'taskDefinitionDetail',
    path: '/task-definition/:taskId',
    component: TaskDefinitionDetailView,
    props: true,
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: namedRoutes.index.path,
      component: namedRoutes.index.component,
      children: [
        {
          path: '',
          redirect: {
            name: namedRoutes.index.clusterManage.name
          }
        },
        {
          path: namedRoutes.index.clusterManage.path,
          name: namedRoutes.index.clusterManage.name,
          component: namedRoutes.index.clusterManage.component,
        },
        {
          path: namedRoutes.index.taskDefinitionManage.path,
          name: namedRoutes.index.taskDefinitionManage.name,
          component: namedRoutes.index.taskDefinitionManage.component,
        },
        {
          path: namedRoutes.index.taskStartupManage.path,
          name: namedRoutes.index.taskStartupManage.name,
          component: namedRoutes.index.taskStartupManage.component,
        },
      ]
    },
    {
      name: namedRoutes.taskDefinitionDetail.name,
      path: namedRoutes.taskDefinitionDetail.path,
      component: namedRoutes.taskDefinitionDetail.component,
      props: namedRoutes.taskDefinitionDetail.props
    }
  ]
})

export default router

