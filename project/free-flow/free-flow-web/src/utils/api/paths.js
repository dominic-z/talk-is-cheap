// src/api/paths.js
export const API_PATHS = Object.freeze({
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
        TASK_EXECUTIONS: "/scheduler/task-info/task/executions",
        STAGE_STARTUPS: "/scheduler/task-info/stage/startups",
        STAGE_STARTUP_PARAMS: "/scheduler/task-info/stage/startups_params",
        STAGE_EXECUTIONS: "/scheduler/task-info/stage/executions",
        STAGE_EXECUTION_LOGS: "/scheduler/task-info/stage/execution_logs",
    },
});