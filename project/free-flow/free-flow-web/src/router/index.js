import { createRouter, createWebHistory } from 'vue-router'
import ClusterNodeListView from '../views/ClusterNodeListView.vue'
import TaskDefinitionListView from '@/views/task-definition/TaskDefinitionListView.vue'
import IndexView from '@/views/IndexView.vue'
import TaskDefinitionDetailView from '@/views/task-definition/TaskDefinitionDetailView.vue'
import TaskStartupListView from '@/views/task-startup/TaskStartupListView.vue'
import TaskStarupDetailView from '@/views/task-startup/TaskStarupDetailView.vue'

export const namedRoutes = {
  index: {
    name: 'index',
    path: '/',
    component: IndexView,
    clusterNodeList: {
      name: 'clusterManage',
      path: 'cluster-manage',
      component: ClusterNodeListView
    },
    taskDefinitionList: {
      name: 'taskDefinitionManage',
      path: 'task-definition-manage',
      component: TaskDefinitionListView
    },
    taskStartupList: {
      name: 'taskStartupManage',
      path: 'task-startup-manage',
      component: TaskStartupListView
    }
  },

  taskDefinitionDetail:{
    name: 'taskDefinitionDetail',
    path: '/task-definition/:taskId',
    component: TaskDefinitionDetailView,
    props: true,
  },

  taskStartupDetail:{
    name: 'taskStartupDetail',
    path: '/task-startup/:startupId',
    component: TaskStarupDetailView,
    props: true,
  },
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
            name: namedRoutes.index.clusterNodeList.name
          }
        },
        {
          path: namedRoutes.index.clusterNodeList.path,
          name: namedRoutes.index.clusterNodeList.name,
          component: namedRoutes.index.clusterNodeList.component,
        },
        {
          path: namedRoutes.index.taskDefinitionList.path,
          name: namedRoutes.index.taskDefinitionList.name,
          component: namedRoutes.index.taskDefinitionList.component,
        },
        {
          path: namedRoutes.index.taskStartupList.path,
          name: namedRoutes.index.taskStartupList.name,
          component: namedRoutes.index.taskStartupList.component,
        },
      ]
    },
    {
      name: namedRoutes.taskDefinitionDetail.name,
      path: namedRoutes.taskDefinitionDetail.path,
      component: namedRoutes.taskDefinitionDetail.component,
      props: namedRoutes.taskDefinitionDetail.props
    },
    {
      name: namedRoutes.taskStartupDetail.name,
      path: namedRoutes.taskStartupDetail.path,
      component: namedRoutes.taskStartupDetail.component,
      props: namedRoutes.taskStartupDetail.props
    },
  ]
})

export default router

