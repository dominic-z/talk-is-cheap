# Free-Flow 架构设计文档

## 1. 项目概述

Free-Flow 是一个**分布式任务编排与执行引擎**，采用 Scheduler（调度中心）+ Worker（执行节点）的经典架构。支持将复杂业务逻辑拆分为多个 Stage（阶段），以 DAG（有向无环图）形式编排执行，具备失败重试、任务恢复、集群管理等能力。

---

## 2. 技术架构

### 2.1 技术栈

| 层次 | 技术选型 | 版本 |
|------|----------|------|
| 语言 | Java | 17 |
| 应用框架 | Spring Boot | 3.2.12 |
| 微服务 | Spring Cloud / Spring Cloud Alibaba | 2023.0.6 / 2023.0.3.3 |
| Web层 | Spring WebFlux（响应式） | - |
| 服务调用 | OpenFeign + OkHttp | - |
| 分布式协调 | Apache Curator (ZooKeeper) | 5.8.0 |
| 关系数据库 | MySQL + MyBatis + Druid | 8.0.32 |
| 搜索引擎/日志 | Elasticsearch (IK分词器) | 8.x |
| 缓存 | Redis | 8.0.3 |
| 工具库 | Lombok, Hutool, Guava, Vavr, ModelMapper | - |

### 2.2 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        外部调用方 / 前端                          │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTP (启动任务/查询状态)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Scheduler 集群 (可多实例)                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │TaskProcess   │  │ClusterManage │  │ TaskDefinition       │   │
│  │Controller    │  │Controller    │  │ Controller           │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘   │
│         │                  │                      │               │
│  ┌──────▼──────────────────▼──────────────────────▼───────────┐  │
│  │              Service Layer                                  │  │
│  │  TaskScheduler / WorkerTaskDriverService / WorkerClusterMgr │  │
│  │  WorkerTaskResultService / SchedulerClusterManager          │  │
│  └────────────────────────────────────────────────────────────┘  │
└──────────┬──────────────────────────────────┬───────────────────┘
           │                                  │
           │ ZooKeeper (集群协调/节点发现)       │ Feign HTTP (任务下发/状态上报)
           ▼                                  ▼
┌──────────────────┐              ┌──────────────────────────────┐
│   ZooKeeper      │              │      Worker 集群 (多实例)      │
│  - Leader选举    │              │  ┌────────────────────────┐   │
│  - 节点注册      │              │  │ TaskDriverController   │   │
│  - 健康检测      │              │  │ (接收任务/重试/恢复)     │   │
└──────────────────┘              │  └───────────┬────────────┘   │
                                  │  ┌───────────▼────────────┐   │
                                  │  │ TaskRuntimeService     │   │
                                  │  │ (任务运行时管理)         │   │
                                  │  └───────────┬────────────┘   │
                                  │  ┌───────────▼────────────┐   │
                                  │  │ @Task / @RunnableStage │   │
                                  │  │ (用户业务逻辑)          │   │
                                  │  └────────────────────────┘   │
                                  └──────────────────────────────┘
           │                                  │
           ▼                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                       数据存储层                                  │
│  ┌─────────┐    ┌──────────────┐    ┌──────────┐                │
│  │  MySQL  │    │Elasticsearch │    │  Redis   │                │
│  │(业务数据)│    │(日志/参数/快照)│    │(缓存/锁) │                │
│  └─────────┘    └──────────────┘    └──────────┘                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 模块关系

### 3.1 Maven 模块结构

```
free-flow (parent pom)
├── free-flow-common              # 公共基础库
├── free-flow-repository-starter  # 数据访问层 Starter
├── free-flow-scheduler           # 调度中心服务
├── free-flow-worker-starter      # Worker 执行器 Starter
├── free-flow-worker-example      # Worker 示例 1
└── free-flow-worker-example2     # Worker 示例 2
```

### 3.2 模块依赖关系

```
free-flow-scheduler
    ├── free-flow-common
    └── free-flow-repository-starter

free-flow-worker-starter
    ├── free-flow-common
    └── free-flow-repository-starter

free-flow-worker-example / example2
    └── free-flow-worker-starter
```

### 3.3 各模块职责

| 模块 | 职责 |
|------|------|
| **free-flow-common** | 枚举定义、异常体系、消息DTO、路由URI、任务定义BO、编解码器、工具类 |
| **free-flow-repository-starter** | MySQL/ES/Redis 数据访问封装，MyBatis Mapper，领域对象 |
| **free-flow-scheduler** | 任务调度、Worker集群管理、任务生命周期管理、Leader选举 |
| **free-flow-worker-starter** | Worker自动注册、任务定义解析校验、任务运行时管理、Stage执行引擎 |
| **free-flow-worker-example** | 演示如何通过注解定义和执行任务 |

---

## 4. 核心流程

### 4.1 Worker 注册与上线流程

```
Worker应用启动
    │
    ▼
WorkerStartingDriver (ApplicationStartedEvent)
    │
    ├── 1. 连接Scheduler集群，获取Leader地址
    │      clusterService.listenAndSetSchedulerLeader()
    │
    ├── 2. 解析本地@Task/@RunnableStage注解
    │      localTaskDefinitionService.prepareAndValidateTaskDefinition()
    │      - 构建DAG图 (pointOutGraph / pointInGraph)
    │      - DFS验证无环 + BFS验证连通性
    │      - 与Scheduler远端定义一致性比对
    │
    └── 3. 注册到ZooKeeper (online路径)
           clusterService.becomeOnline()
                │
                ▼
        Scheduler Leader 监听到新节点
                │
                ├── 写入MySQL (cluster_node表, 状态INITIALIZING)
                ├── 通过一致性哈希分配给某个Scheduler管理
                └── 定时Ping健康检查
                        │
                        ▼ (连续N次Ping成功)
                移入ZK runnable路径 → 所有Scheduler可见
```

### 4.2 任务启动与执行流程

```
外部请求: POST /scheduler/task/process/start
    │
    ▼
TaskProcessController.startTask()
    │
    ├── 1. prepareForTask(): 创建 TaskStartup + TaskExecution + StageStartup 记录
    │      分配Worker地址 (随机选取拥有该Task定义的Worker)
    │
    ├── 2. 通过Feign调用Worker: POST /worker/driver/task/start
    │      携带: taskExecutionId, taskName, version, sharedContext, 各Stage入参
    │
    └── 3. Worker接收并执行
           │
           ▼
    TaskDriverController.startTask()
           │
           ├── TaskRuntimeService.createTaskRuntimeEnv()
           │   - 反序列化SharedContext
           │   - 为Starting Stage创建StageRuntimeEnv
           │
           └── 执行@RunnableStage标注的方法
                  │
                  ├── 成功 → 上报Scheduler (STAGE_COMPLETE)
                  │         → 请求Prepare下一个Stage (STAGE_PREPARE)
                  │         → 继续执行后续Stage (支持并行)
                  │
                  └── 失败 → 上报Scheduler (STAGE_FAIL)
                            → Scheduler判断是否重试
                            → 重试: 调用Worker /worker/driver/stage/retry
```

### 4.3 Stage 状态流转

```
PENDING → RUNNING → SUCCEEDED
                  → FAILING → FAILED (可重试)
                  → TERMINATING → TERMINATED
                  → RESCHEDULING → RESCHEDULED
                  → TIME_OUT
                  → SKIPPED
```

### 4.4 任务失败与重试流程

```
Stage执行抛出异常
    │
    ▼
Worker上报: POST /scheduler/task/process/stage/fail
    │
    ▼
Scheduler: failStageAndRetry()
    │
    ├── 检查Stage级别maxRetryCount
    │   ├── 未超限 → 调用Worker /worker/driver/stage/retry (stageFailedCount+1)
    │   └── 已超限 → 检查Task级别maxRetryCount
    │               ├── 未超限 → 整体Task重试 (rescheduleTask)
    │               └── 已超限 → 标记Task为FAILED
    │
    └── 记录失败信息到ES (stage_execution_result_msg)
```

### 4.5 集群管理流程

```
Scheduler集群:
    - 通过ZooKeeper进行Leader选举
    - Leader负责: Worker健康检测、节点分配、数据库写入
    - 非Leader: 监听ZK runnable路径，执行任务调度

Worker分配策略:
    - 使用虚拟节点一致性哈希 (VNConsistentHash, 200虚拟节点)
    - Worker地址 → Murmur3_128哈希 → 分配到某个Scheduler
    - Scheduler上下线时自动重新分配

Worker优雅下线:
    1. 外部调用: POST /scheduler/cluster/terminate-worker
    2. Scheduler通知Worker: /worker/node/try-terminate
    3. Worker等待当前任务完成
    4. Worker上报: safe-to-terminate
    5. Scheduler更新节点状态
```

---

## 5. 数据模型

> 完整的表结构、ES索引、Redis设计详见 [database.md](database.md)

**核心设计思想：**

- **Startup/Execution 分离**：将“启动意图”(Startup)与“实际执行”(Execution)分离，一个 Startup 可对应多个 Execution，支持失败重试而不丢失启动上下文
- **多存储引擎分层**：MySQL（业务状态）+ Elasticsearch（大文本/日志）+ Redis（缓存/锁）

---

## 6. 通信协议

### 6.1 API 路由总览

| 前缀 | 提供方 | 用途 |
|------|--------|------|
| `/scheduler/task/process/*` | Scheduler | 任务启动、Stage完成/失败上报、重调度 |
| `/scheduler/task-definition/*` | Scheduler | 任务定义查询 |
| `/scheduler/cluster/internal/*` | Scheduler | 集群内部通信（地址、Leader、注册） |
| `/scheduler/cluster/*` | Scheduler | 集群管理（节点列表、终止Worker） |
| `/scheduler/task-info/*` | Scheduler | 任务执行信息查询 |
| `/worker/driver/*` | Worker | 任务启动、Stage重试、任务恢复、清理 |
| `/worker/node/*` | Worker | 节点Ping、运行许可、优雅终止 |
| `/worker/definition/*` | Worker | 获取Worker的任务定义 |

### 6.2 通信方式

- **Scheduler → Worker**: OpenFeign HTTP 调用（直接指定Worker地址）
- **Worker → Scheduler**: OpenFeign HTTP 调用（调用当前Scheduler Leader）
- **节点发现/协调**: ZooKeeper Watch 机制
- **Scheduler集群内部**: ZooKeeper + Feign

---

## 7. 任务定义模型（注解驱动）

### 7.1 声明式任务定义

```java
@Task(name = "task1", version = 3, 
      sharedContextCodecClass = MyContextCodec.class,
      maxRetryCount = 2, timeout = 3600)
public class MyTask {

    @RunnableStage(name = "step1", version = 1, 
                   isStartingStage = true,
                   toStageName = {"step2", "step3"},
                   inputCodecClass = Step1InputCodec.class,
                   maxRetryCount = 3, timeoutInSecond = 300)
    public void step1(StageRuntimeEnv<Step1Input> env) {
        Step1Input input = env.getInput();          // 获取输入参数
        MyContext ctx = env.getSharedContext();      // 读写共享上下文
        env.log("step1 executed");                   // 写入业务日志(ES)
    }
}
```

### 7.2 DAG 图验证规则

启动时自动校验：
1. **无环检测** — DFS遍历，检测回边
2. **连通性验证** — 从Starting Stage出发，所有Stage必须可达
3. **引用完整性** — toStageName引用的Stage必须存在
4. **远端一致性** — 与Scheduler存储的定义进行BFS逐节点比对

---

## 8. 关键设计决策

| 决策 | 说明 |
|------|------|
| Startup/Execution分离 | 支持失败重试，一个启动意图可产生多次执行 |
| 共享上下文存Worker内存 | 避免并发Stage读写ES的一致性问题 |
| ES存储日志和参数 | 大文本数据与MySQL业务数据分离，支持全文检索 |
| 一致性哈希分配Worker | Scheduler扩缩容时最小化Worker重分配 |
| ZK双路径(online/runnable) | 区分"已注册"和"健康可用"，防止不稳定节点接收任务 |
| 懒加载StageRuntimeEnv | 仅在Stage实际执行前创建，节省内存 |
| 乐观锁(revision字段) | 所有表使用revision字段做并发控制 |

---

## 9. 部署架构

```
┌─────────────┐  ┌─────────────┐
│ Scheduler-1 │  │ Scheduler-2 │  ... (可水平扩展)
│  (Leader)   │  │ (Follower)  │
└──────┬──────┘  └──────┬──────┘
       │                 │
       └────────┬────────┘
                │ ZooKeeper集群
       ┌────────┴────────┐
       │                 │
┌──────┴──────┐  ┌──────┴──────┐
│  Worker-1   │  │  Worker-2   │  ... (可水平扩展)
└─────────────┘  └─────────────┘

基础设施: MySQL + Redis + Elasticsearch + ZooKeeper
```

---

