# 背景

当前worker节点下线之后，scheduler会将节点上的运行的任务设置为失败。

# 目标

当前worker节点下线之后，管理这个worker的scheduler应当会将这个节点上的运行的任务重新调度，并且尽可能不重复运行已经完成的任务阶段。

# 功能要求

1. 在`free-flow/free-flow-scheduler/src/main/java/org/talk/is/cheap/project/free/flow/scheduler/task/service/WorkerTaskDriverService.java`中`public void rescheduleTask(long pausedTaskExecutionId)`方法具备重新调度一个暂停的TaskExecution的能力，请复用这个方法。
2. 你需要考虑某个worker下线的时候，管理他的scheduler如果也同时下线，通过一致性hash重新分配worker的时候，理应管理这个下线的worker的scheduler需要继续执行这个下线的worker的重新调度任务，这个功能你应该添加到`free-flow/free-flow-scheduler/src/main/java/org/talk/is/cheap/project/free/flow/scheduler/cluster/service/WorkerClusterManager.java`的`public void watchRunnableWorkers()`方法中。

