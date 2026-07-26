# 需要阅读的文档

- docs/architecture.md
- docs/coding-style.md
- docs/database.md

---

# scheduler执行集群节点管理

`free-flow/free-flow-scheduler/src/main/java/org/talk/is/cheap/project/free/flow/scheduler/cluster/service`包含集群管理代码：
1. `free-flow/free-flow-scheduler/src/main/java/org/talk/is/cheap/project/free/flow/scheduler/cluster/service/ClusterInfoService.java`，暂时没用
2. `free-flow/free-flow-scheduler/src/main/java/org/talk/is/cheap/project/free/flow/scheduler/cluster/service/SchedulerClusterManager.java`：scheduler节点自身的集群管理
3. `free-flow/free-flow-scheduler/src/main/java/org/talk/is/cheap/project/free/flow/scheduler/cluster/service/WorkerClusterManager.java`：scheduler节点如何分配、管理worker节点。


# scheduler执行任务调度执行管理
`free-flow-scheduler/src/main/java/org/talk/is/cheap/project/free/flow/scheduler/task/service`包含了任务调度运行代码：
1. `free-flow/free-flow-scheduler/src/main/java/org/talk/is/cheap/project/free/flow/scheduler/task/service/WorkerTaskDriverService.java`包含了scheduler如何执行任务、调度任务


