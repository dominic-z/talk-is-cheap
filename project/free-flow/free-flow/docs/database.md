# Free-Flow 数据库设计文档

## 1. 存储架构总览

Free-Flow 采用多存储引擎分层设计：

| 存储 | 用途 | 数据库名 |
|------|------|----------|
| MySQL | 业务数据持久化（定义、状态、集群） | `free_flow`（公共）、`free_flow_scheduler`（调度专属） |
| Elasticsearch | 大文本数据（日志、参数、上下文快照） | 多索引 |
| Redis | 缓存、分布式锁、Worker地址映射 | - |

---

## 2. MySQL — `free_flow` 库（公共）

### 2.1 ER 关系图

```
task_definition (1) ──── (N) stage_definition
       │                          │
       │                          │
       │              task_graph_definition (from_stage_id → to_stage_id)
       │
       ▼
task_startup (1) ──── (N) task_execution
       │                        │
       │                        ▼
       │              stage_startup (1) ──── (N) stage_execution
       │
       ▼
task_source_target_startup_relation

stage_source_target_startup_relation
```

### 2.2 表结构

#### cluster_node — 集群节点信息表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| node_address | varchar(64) | NOT NULL, UNIQUE | 节点地址（如 `192.168.1.1:8080`） |
| node_zk_path | varchar(256) | NOT NULL | 节点在ZK中的注册路径 |
| node_type | int | NOT NULL, DEFAULT 0 | 节点类型（0=scheduler-leader, 1=scheduler, 2=worker） |
| status | int | NOT NULL, DEFAULT 0 | 节点状态（0=初始化, 1=可运行, 2=终止中, 3=可安全退出, 4=已终止） |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `uni_idx_node_address(node_address)` — UNIQUE
- `idx_node_type(node_type)`

---

#### task_definition — 任务定义主表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| name | varchar(64) | NOT NULL | 任务名称（全局唯一标识） |
| version | int | NOT NULL | 版本号 |
| status | int | - | 任务状态 |
| timeout | int | NOT NULL, DEFAULT 0 | 超时时间（秒），0表示不限制 |
| max_retry_count | int | NOT NULL | 最大重试次数 |
| shared_context_fully_qualified_class_name | varchar(256) | - | 共享上下文类全限定名 |
| shared_context_codec_fully_qualified_class_name | varchar(256) | - | 共享上下文编解码器类全限定名 |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_name_version(name, version)` — UNIQUE
- `idx_status(status)`

---

#### schedule_task_definition — 预约（定时）任务定义表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| name | varchar(64) | NOT NULL | 预约任务名称 |
| version | int | NOT NULL, DEFAULT 0 | 版本 |
| cron | varchar(32) | NOT NULL | Cron表达式 |
| target_task_id | bigint | NOT NULL | 目标task_definition的id |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_name_version(name, version)` — UNIQUE
- `idx_target_task_id(target_task_id)`

---

#### stage_definition — 阶段定义主表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| task_id | bigint | NOT NULL | 所属任务id（FK → task_definition.id） |
| name | varchar(64) | NOT NULL | 阶段名称（同一task内唯一） |
| version | int | NOT NULL, DEFAULT 0 | 版本 |
| stage_type | int | NOT NULL | 阶段类型 |
| input_fully_qualified_class_name | varchar(256) | - | 输入参数类全限定名 |
| input_codec_fully_qualified_class_name | varchar(256) | - | 输入参数编解码器类全限定名 |
| is_starting_stage | bool | NOT NULL | 是否为起始stage |
| timeout | int | NOT NULL, DEFAULT 0 | 超时时间（秒） |
| max_retry_count | int | NOT NULL | 最大重试次数 |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_name_version(task_id, name, version)` — UNIQUE

---

#### task_graph_definition — 任务DAG图结构表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| task_id | bigint | NOT NULL | 所属任务id |
| from_stage_id | bigint | NOT NULL | 源stage id（FK → stage_definition.id） |
| to_stage_id | bigint | NOT NULL | 目标stage id（FK → stage_definition.id） |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_task_id(task_id)`
- `idx_from_to_stage_id(from_stage_id, to_stage_id)`
- `idx_to_stage_id(to_stage_id)`

---

#### task_startup — 任务启动表

> 设计思想：将"启动意图"与"实际执行"分离。一条startup表征执行意图，可唤起多条execution（支持失败重试）。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| task_id | bigint | NOT NULL | 任务id（FK → task_definition.id） |
| source_type | int | NOT NULL | 启动来源类型（调度触发/Stage调起/手动恢复等） |
| source_id | bigint | - | 启动来源id |
| status | int | NOT NULL | 启动状态 |
| fail_count | int | NOT NULL, DEFAULT 0 | 失败次数 |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_task_id(task_id)`
- `idx_source_type_id(source_type, source_id)`
- `idx_source_id(source_id)`

---

#### task_source_target_startup_relation — 任务启动来源关系表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| source_type | int | NOT NULL | 来源类型 |
| source_id | bigint | - | 来源id |
| target_task_startup_id | bigint | NOT NULL | 目标task_startup的id |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_target_id(target_task_startup_id)`
- `idx_source_target(source_type, source_id, target_task_startup_id)`
- `idx_source_id(source_id)`

---

#### task_execution — 任务执行主表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| task_startup_id | bigint | NOT NULL | 所属task_startup的id |
| assigned_worker_addr | varchar(64) | NOT NULL | 被分配的Worker地址 |
| status | int | NOT NULL | 执行状态 |
| completion_time | datetime | - | 完成时间 |
| start_time | datetime | NOT NULL, DEFAULT now() | 开始时间 |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_task_startup_id(task_startup_id)`
- `idx_worker(assigned_worker_addr)`

---

#### stage_startup — 阶段启动表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| task_execution_id | bigint | NOT NULL | 所属task_execution的id |
| stage_id | bigint | NOT NULL | 阶段定义id（FK → stage_definition.id） |
| status | int | NOT NULL | 启动状态 |
| fail_count | int | NOT NULL, DEFAULT 0 | 失败次数 |
| completion_time | datetime | - | 完成时间 |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_task_execution_id_status(task_execution_id, status)`
- `idx_stage_id_status(stage_id, status)`

---

#### stage_source_target_startup_relation — 阶段启动来源关系表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| source_type | int | NOT NULL | 来源类型（父Stage完成/手动启动等） |
| source_id | bigint | - | 来源id |
| target_stage_startup_id | bigint | NOT NULL | 目标stage_startup的id |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_target_id(target_stage_startup_id)`
- `idx_source_target(source_type, source_id, target_stage_startup_id)`
- `idx_source_id(source_id)`

---

#### stage_execution — 阶段执行主表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| stage_startup_id | bigint | NOT NULL | 所属stage_startup的id |
| worker_address | varchar(64) | NOT NULL | 执行该阶段的Worker地址 |
| status | int | NOT NULL | 执行状态 |
| start_time | datetime | NOT NULL, DEFAULT now() | 开始时间 |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**索引：**
- `idx_stage_startup_id(stage_startup_id)`
- `idx_worker(worker_address)`

---

#### seq_generator — 分布式ID序列表（号段模式）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| seq_name | varchar(64) | NOT NULL, UNIQUE | 序列名称 |
| next | varchar(64) | NOT NULL, DEFAULT '0' | 下一段的起始id |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() ON UPDATE | 更新时间 |

**预置序列：**
- `task_shared_context`
- `stage_startup_param`
- `stage_execution_biz_log`
- `stage_execution_result_msg`

---

## 3. MySQL — `free_flow_scheduler` 库（调度专属）

#### scheduler_log — Scheduler节点日志表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | bigint | PK, AUTO_INCREMENT | 主键 |
| node_id | varchar(64) | NOT NULL | 节点id |
| log | varchar(64) | NOT NULL, DEFAULT '0' | 日志内容 |
| revision | bigint | NOT NULL, DEFAULT 0 | 乐观锁版本号 |
| create_time | datetime | NOT NULL, DEFAULT now() | 创建时间 |
| update_time | datetime | NOT NULL, DEFAULT now() | 更新时间 |

---

## 4. Elasticsearch 索引

> 所有ES文档的id由 `seq_generator` 表生成（号段模式），确保全局唯一。
> 日期格式统一为 `yyyy-MM-dd HH:mm:ss`，使用 IK 分词器。

### 4.1 task_shared_context — 任务共享上下文快照

用于任务重试/恢复时还原共享上下文。

| 字段 | ES类型 | 说明 |
|------|--------|------|
| task_startup_id | keyword | 关联的task_startup id |
| encoded_task_shared_context | text (ik_max_word) | 编码后的共享上下文JSON |
| update_time | date | 更新时间 |

**对应Java类：** `TaskSharedContext`

---

### 4.2 stage_startup_param — Stage启动参数

记录每个Stage的输入参数及上下文快照。

| 字段 | ES类型 | 说明 |
|------|--------|------|
| stage_startup_id | keyword | 关联的stage_startup id |
| encoded_input | text (ik_max_word) | 编码后的Stage输入参数 |
| encoded_shared_context_snapshot_at_startup | text (ik_max_word) | 启动时共享上下文快照 |
| encoded_shared_context_snapshot_at_completion | text (ik_max_word) | 完成时共享上下文快照 |
| update_time | date | 更新时间 |

**对应Java类：** `StageStartupParam`

---

### 4.3 stage_execution_biz_log — Stage业务执行日志

| 字段 | ES类型 | 说明 |
|------|--------|------|
| stage_execution_id | keyword | 关联的stage_execution id |
| task_execution_id | keyword | 关联的task_execution id |
| log | text (ik_max_word) | 日志内容 |
| create_time | date | 创建时间 |

**索引设置：** 按 `create_time` 升序排序（index.sort）

**对应Java类：** `StageExecutionBizLog`

**查询方式：** 使用 `search_after` 分页

---

### 4.4 stage_execution_result_msg — Stage执行结果消息

| 字段 | ES类型 | 说明 |
|------|--------|------|
| stage_execution_id | keyword | 关联的stage_execution id |
| msg | text (ik_max_word) | 结果消息（成功/失败描述） |
| create_time | date | 创建时间 |

**对应Java类：** `StageExecutionResultMsg`

---

## 5. Redis 数据结构

### 5.1 Key 设计

| Key模式 | 类型 | 说明 | 示例 |
|---------|------|------|------|
| `T_W_ADDR-{taskName}-{version}` | Set | 拥有指定任务定义的Worker地址集合 | `T_W_ADDR-task1-3` → {"192.168.1.1:8080", "192.168.1.2:8080"} |
| `free-flow-test-redisson-lock` | Lock | Redisson分布式锁 | - |

### 5.2 使用场景

- **Worker地址映射**：Worker上线注册任务定义时，将自身地址 `SADD` 到对应key；下线时 `SREM`
- **任务调度**：Scheduler通过 `SMEMBERS` 获取可执行某任务的Worker列表，随机选取
- **脏数据清理**：读取时校验Worker是否仍在runnable状态，无效地址自动移除
- **空Set清理**：通过Lua脚本原子性地删除空Set（`SCARD == 0` 时 `DEL`）

---

## 6. 状态枚举值参考

### 6.1 TaskStageStatus（Task/Stage通用状态）

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | PENDING | 待执行 |
| 1 | FAILED_TO_START | 启动失败 |
| 2 | RUNNING | 运行中 |
| 3 | SUCCEEDED | 成功 |
| 4 | FAILING | 失败中 |
| 5 | FAILED | 已失败 |
| 6 | TERMINATING | 终止中 |
| 7 | TERMINATED | 已终止 |
| 8 | SKIPPED | 已跳过 |
| 9 | RESCHEDULING | 重调度中 |
| 10 | RESCHEDULED | 已重调度 |
| 11 | TIME_OUT | 超时 |

### 6.2 NodeStatus（集群节点状态）

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | INITIALIZING | 初始化中 |
| 1 | RUNNABLE | 可运行任务 |
| 2 | TERMINATING | 终止中 |
| 3 | SAFE_TO_TERMINATE | 可安全退出 |
| 4 | TERMINATED | 已终止 |

### 6.3 NodeType（节点类型）

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | SCHEDULER_LEADER | 调度中心Leader |
| 1 | SCHEDULER | 调度中心Follower |
| 2 | WORKER | 执行节点 |

---

## 7. 设计约束与规范

### 7.1 通用字段规范

所有MySQL表必须包含：
- `revision` (bigint) — 乐观锁，每次更新 +1
- `create_time` (datetime) — 创建时间
- `update_time` (datetime) — 更新时间（ON UPDATE now()）

### 7.2 并发控制

- **MySQL**：通过 `revision` 字段实现乐观锁，UPDATE时 `WHERE revision = ?`
- **ES**：通过 `seqNo + primaryTerm` 实现CAS更新（见 `TaskSharedContextService.safeUpdate`）
- **Redis**：通过 Lua 脚本保证原子性操作

### 7.3 ID生成策略

- **MySQL表**：使用 `AUTO_INCREMENT`
- **ES文档**：使用 `seq_generator` 表的号段模式生成分布式唯一ID

### 7.4 数据分层原则

| 数据特征 | 存储选择 | 原因 |
|----------|----------|------|
| 结构化、需事务、需状态流转 | MySQL | ACID保证 |
| 大文本、全文检索、按时间排序 | Elasticsearch | 适合日志/参数类数据 |
| 高频读取、集合操作、分布式锁 | Redis | 低延迟 |

**具体数据类型映射：**

| 数据类型 | 存储位置 | 原因 |
|----------|----------|------|
| 任务/阶段定义 | MySQL | 结构化、需要事务 |
| 启动/执行记录 | MySQL | 需要状态流转、乐观锁 |
| 集群节点信息 | MySQL | 需要持久化、查询 |
| 业务执行日志 | Elasticsearch | 大文本、全文检索 |
| 启动参数/上下文快照 | Elasticsearch | 大文本、按ID查询 |
| 执行结果消息 | Elasticsearch | 大文本 |
| Worker地址缓存 | Redis (Set) | 高频读取 |
| 分布式锁 | Redis (Redisson) | 并发控制 |
| 分布式ID | MySQL (seq_generator) | 号段模式 |

### 7.5 Startup/Execution 分离设计

```
task_startup (意图)  →  task_execution (执行1)
                    →  task_execution (执行2, 重试)

stage_startup (意图) →  stage_execution (执行1)
                    →  stage_execution (执行2, 重试)
```

一个 Startup 可对应多个 Execution，支持失败重试而不丢失启动上下文。
