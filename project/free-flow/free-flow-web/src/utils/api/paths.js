// src/api/paths.js
export const API_PATHS = {
    // 用户相关
    cluster: {
        nodes: "/scheduler/cluster/nodes",
        schduler: {
            id: "/scheduler/cluster/id",
            leader: "/scheduler/cluster/leader",
        }
    },
    TASK_DEFINITION:{
        DETAILS_QUERY: "/scheduler/task-definition/details/_query"
    },
    TASK_INFO:{
        TASK_STARTUPS: "/scheduler/task-info/task/startups",
        TASK_EXECUTIONS: "/scheduler/task-info/task/executions"
    },
    // 商品相关
    product: {
        detail: '/product/:id', // 带参数的路径
        list: '/product/list',
        create: '/product'
    },
    // 公共路径
    common: {
        upload: '/upload',
        config: '/config'
    }
};