# 验收报告

## 修改文件列表

- `free-flow-scheduler/.../task/service/WorkerTaskDriverService.java` — 将 `nodeLostAndFailTask` 重构为 `nodeLostAndRescheduleTask`，Worker下线时不再标记任务FAILED，改为加分布式锁后逐个调用 `rescheduleTask` 重新调度
- `free-flow-scheduler/.../cluster/service/WorkerClusterManager.java` — 在 `watchRunnableWorkers()` 末尾新增 `scheduleOrphanTaskDetection()` 定时任务（30秒间隔），检测RUNNING任务的Worker是否仍在runnable集合，不在则发布 `WorkerTerminatedEvent` 触发重新调度
- `free-flow-repository-starter/.../service/derived/TaskExecutionServiceWrapper.java` — 新增 `selectByStatus(page, pageSize, taskStatus)` 方法，支持不限Worker地址的状态分页查询

## 新增文件列表

- `free-flow-worker-example/.../task/RescheduleTestTask.java` — 测试Task，3个Stage串行（stage1→stage2→stage3），每个sleep 10秒，SharedContext记录完成进度
- `free-flow-worker-example2/.../task/RescheduleTestTask.java` — 同上，确保两个Worker都能执行该任务

## 删除文件列表

- 无

## 测试结果

- 编译：**通过**（`mvn compile -q` EXIT_CODE=0）
- 单元测试：**1个预存失败**（与本次修改无关）
  - 失败用例：`worker.UnitTest.testModelMapper`（`free-flow-worker-starter` 模块）
  - 失败原因：ModelMapper转换 `Class→String` 时NPE，属于预存问题，非本次修改引入
  - 本次修改涉及的模块（scheduler、repository-starter、worker-example、worker-example2）均编译通过且无测试失败

## 验收项完成情况

- [x] Worker下线后，任务重新调度到其他Worker（复用 `rescheduleTask`，跳过已完成Stage）
- [x] Worker和管理它的Scheduler同时下线后，孤儿任务检测机制兜底（30秒定时扫描 + 事件触发）
- [x] 开发测试代码（`RescheduleTestTask`，两个worker-example模块均部署）
- [ ] 验收场景1实际运行验证 — 需要手动启动2 Scheduler + 2 Worker进行集成测试
- [ ] 验收场景2实际运行验证 — 需要手动启动2 Scheduler + 2 Worker并模拟双故障

## 备注

- 验收场景1和2需要手动集成测试：启动2个Scheduler与2个Worker，发送 `reschedule-test-task` 执行请求，在Stage执行过程中关闭Worker（场景1）或同时关闭Worker+Scheduler（场景2），观察任务是否被成功重新调度到另一个Worker继续执行。
- 测试任务启动参数示例（POST `/scheduler/task/process/start`）：
  ```json
  {
    "data": {
      "taskName": "reschedule-test-task",
      "taskVersion": 1,
      "initialEncodedSharedContext": "{\"completedStageCount\":0,\"lastCompletedStage\":\"\"}",
      "stageEncodedInputs": {}
    }
  }
  ```
- 孤儿任务检测依赖 `scheduledThreadPoolExecutor`（与ping共用），检测间隔30秒，首次延迟30秒。
- `rescheduledOfflineWorkerAddrs` 集合记录已处理的下线Worker地址，避免重复触发事件。
