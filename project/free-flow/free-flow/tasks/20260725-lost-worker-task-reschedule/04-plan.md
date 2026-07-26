# 修改计划

## 需求分析

**当前行为：** Worker下线后，`nodeLostAndFailTask` 事件监听器将该Worker上运行的任务直接标记为FAILED。

**目标行为：** Worker下线后，管理该Worker的Scheduler应调用 `rescheduleTask` 将任务重新调度到其他可用Worker，跳过已完成的Stage。若管理该Worker的Scheduler也同时下线，其他Scheduler应能检测到孤儿任务并接管重新调度。

---

## 涉及模块

- [x] free-flow-scheduler — 核心调度逻辑修改（重新调度 + 孤儿任务检测）
- [x] free-flow-worker-example — 新增测试Task定义
- [x] free-flow-worker-example2 — 新增测试Task定义（与example保持一致）

---

## 修改文件

### 1. `free-flow-scheduler/.../task/service/WorkerTaskDriverService.java`

**修改 `nodeLostAndFailTask` 方法：**
- 将"标记任务为FAILED"改为"调用 `rescheduleTask` 重新调度"
- 遍历下线Worker上所有RUNNING状态的TaskExecution，逐个调用 `rescheduleTask(taskExecution.getId())`
- 在调用前加分布式锁（复用 `RedissonService.getTaskExecutionLockKey`），避免与 `failStageAndRetry` 并发冲突
- 异常处理：单个任务重新调度失败时记录日志，不影响其他任务

### 2. `free-flow-scheduler/.../cluster/service/WorkerClusterManager.java`

**修改 `watchRunnableWorkers` 方法，新增孤儿任务检测逻辑：**
- 在 `watchRunnableWorkers()` 中，初始化加载runnable节点列表后，启动一个定时检测任务（复用已有的 `scheduledThreadPoolExecutor`）
- 定时逻辑：
  1. 查询DB中所有RUNNING状态的TaskExecution（分页遍历）
  2. 对每条记录，检查 `assigned_worker_addr` 是否仍在 `runnableWorkerAddressPath` 中
  3. 若Worker已不在runnable集合中，发布 `WorkerTerminatedEvent`（复用已有事件）触发重新调度
- 检测间隔：30秒一次（避免频繁DB查询）
- 去重保护：维护一个 `Set<String>` 记录已触发过重新调度的workerAddr，避免重复触发

### 3. `free-flow-scheduler/.../cluster/event/WorkerTerminatedEvent.java`

**无需修改**，复用现有事件。

---

## 新增文件

### 4. `free-flow-worker-example/.../task/RescheduleTestTask.java`

测试用Task，特点：
- `@Task(name = "reschedule-test-task", version = 1, maxRetryCount = 3)`
- 3个Stage串行执行：`stage1` → `stage2` → `stage3`
- 每个Stage sleep 10~15秒（提供足够时间窗口关闭Worker）
- 使用SharedContext记录已完成的Stage数量，验证恢复后不重复执行已完成Stage
- 两个worker-example模块使用相同的Task定义（确保任务可被任一Worker接管）

### 5. `free-flow-worker-example2/.../task/RescheduleTestTask.java`

与上述相同的Task定义（包名相同、类名相同、Task注解相同），确保两个Worker都能执行该任务。

---

## 风险分析

| 风险点 | 应对措施 |
|--------|----------|
| `rescheduleTask` 与 `failStageAndRetry` 并发执行同一任务 | 在 `nodeLostAndFailTask` 中复用 `getTaskExecutionLockKey` 分布式锁 |
| 多个Scheduler同时检测到孤儿任务并重复触发重新调度 | `rescheduleTask` 内部将状态改为RESCHEDULED后，后续调用会因状态不匹配而跳过；额外在孤儿检测中维护已处理集合 |
| Worker短暂网络抖动被误判为下线 | 孤儿检测依赖ZK runnable路径，只有Worker真正从runnable移除后才触发；ping机制已有threshold容错 |
| `rescheduleTask` 中 `assignTaskToWorkerAddress` 找不到可用Worker | 方法内已有null检查并记录错误日志，任务保持RESCHEDULED状态等待后续孤儿检测重试 |
| 定时任务对DB的额外压力 | 30秒间隔 + 分页查询（pageSize=50），影响可控 |

---

## 测试方案

### 验收场景1：Worker下线，任务迁移到另一Worker

1. 启动2个Scheduler + 2个Worker（example和example2）
2. 发送 `reschedule-test-task` 执行请求
3. 在任务执行过程中（如stage2执行时），关闭执行任务的Worker
4. **预期：** 观察到任务被重新调度到另一个Worker，从失败的Stage继续执行，已完成的Stage不重复执行

### 验收场景2：Worker和管理它的Scheduler同时下线

1. 启动2个Scheduler + 2个Worker
2. 发送 `reschedule-test-task` 执行请求
3. 在任务执行过程中，同时关闭执行任务的Worker和管理它的Scheduler
4. **预期：** 另一个Scheduler通过孤儿任务检测机制发现该任务，重新调度到剩余Worker继续执行

### 编译验证

- `mvn compile -q` 通过
