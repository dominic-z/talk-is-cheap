# AGENTS.md — Free-Flow 项目开发指南

你是本项目唯一的软件开发工程师。

目标：

- 不破坏现有代码
- 优先复用已有Service
- 不新增重复DTO
- 不修改公共接口
- 保证单元测试通过



## 1. 技术栈

### 1.1 核心框架

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言（使用 text blocks、sealed 等新特性） |
| Spring Boot | 3.2.12 | 应用框架 |
| Spring Cloud | 2023.0.6 | 微服务基础设施 |
| Spring Cloud Alibaba | 2023.0.3.3 | 微服务生态 |
| Spring WebFlux | - | Web层（非Spring MVC，因与Gateway不兼容） |
| OpenFeign + OkHttp | - | 服务间HTTP调用 |

### 1.2 中间件

| 技术 | 版本 | 用途 |
|------|------|------|
| ZooKeeper | 3.9.3 | 分布式协调、节点发现、Leader选举 |
| Apache Curator | 5.8.0 | ZooKeeper客户端 |
| MySQL | 8.0.32 | 业务数据持久化 |
| MyBatis | 3.0.5 (starter) | ORM |
| Druid | 1.2.27 | 数据库连接池 |
| Elasticsearch | 8.x | 日志/参数/上下文快照存储 |
| Redis | 8.0.3 | 缓存、分布式锁（Redisson） |

### 1.3 工具库

| 库 | 用途 |
|------|------|
| Lombok | 减少样板代码（@Data, @Builder, @Slf4j等） |
| Hutool 5.8.38 | Java工具集（ConcurrentHashSet等） |
| Guava | 集合工具、哈希算法（Murmur3_128） |
| Vavr 0.10.x | 函数式编程（Tuple2, Option等） |
| ModelMapper 3.2.4 | 对象映射（BO ↔ DTO） |
| Apache Commons Lang3 | 字符串/对象工具 |

### 1.4 构建工具

- **Maven** 多模块项目，父POM统一管理版本
- 通过 `dependencyManagement` + BOM 方式导入 Spring Boot / Spring Cloud / Curator 版本

---

## 2. 代码规范

> 详见 [docs/coding-style.md](docs/coding-style.md)
>
> 包含：包结构约定、命名规范、枚举模式、HTTP响应体、参数校验、异常处理、日志、并发控制、注解使用、URI路由、数据库访问规范

---

## 3. 测试规范

### 3.1 测试框架

- **JUnit 5**（`org.junit.jupiter.api.Test`）
- **Spring Boot Test**（`@SpringBootTest`）
- 断言以 `VerifyUtil` 或 JUnit 原生断言为主

### 3.2 测试分类

| 类型 | 位置 | 说明 |
|------|------|------|
| 纯单元测试 | `src/test/java` | 不依赖Spring容器，测试工具类/编解码器 |
| 集成测试 | `src/test/java` | `@SpringBootTest(classes = XxxApp.class)`，依赖完整环境 |

### 3.3 测试编写规范

```java
// 纯单元测试 — 无需Spring容器
@Slf4j
public class UnitTest {
    @Test
    public void testCodec() throws Exception {
        // 直接测试编解码逻辑
    }
}

// 集成测试 — 需要完整中间件环境
@SpringBootTest(classes = SchedulerApp.class)
@Slf4j
public class TestSpringApp {
    @Autowired
    private SomeService someService;

    @Test
    public void testSomeFeature() {
        // 测试需要MySQL/Redis/ES/ZK等中间件
    }
}
```

### 3.4 测试注意事项

- 集成测试**依赖外部中间件**（MySQL、Redis、ES、ZooKeeper），运行前需确保环境就绪
- 并发安全测试使用多线程 + `CountDownLatch`/`join` 验证
- 测试方法命名：`test` + 功能描述（如 `testConcurrentGenerator`、`testModelMapper`）
- 当前项目测试覆盖较少，新增功能应补充对应测试

### 3.5 运行测试

```bash
# 运行全部测试（需要中间件环境）
mvn test

# 运行指定模块测试
mvn test -pl free-flow-common

# 运行单个测试类
mvn test -pl free-flow-scheduler -Dtest=TestSpringApp
```

---

## 4. 架构约束

### 4.1 模块依赖约束

```
free-flow-common          ← 无外部模块依赖（最底层）
free-flow-repository-starter  ← 依赖 common
free-flow-scheduler       ← 依赖 common + repository-starter
free-flow-worker-starter  ← 依赖 common + repository-starter
free-flow-worker-example  ← 依赖 worker-starter
```

**规则：**
- `common` 不得依赖任何业务模块
- `scheduler` 和 `worker-starter` 之间**不得直接依赖**，通过 HTTP (Feign) 通信
- 所有共享的 DTO/枚举/URI 必须放在 `common` 模块

### 4.2 通信约束

| 通信方向 | 方式 | 说明 |
|----------|------|------|
| Scheduler → Worker | Feign HTTP（指定地址） | 任务下发、重试、恢复、终止 |
| Worker → Scheduler | Feign HTTP（Leader地址） | 状态上报、Stage准备请求 |
| 节点发现/协调 | ZooKeeper Watch | 上下线、Leader选举 |
| Scheduler集群内部 | ZooKeeper + Feign | 状态同步 |

**规则：**
- Scheduler 和 Worker 之间**禁止直接方法调用**，必须通过 HTTP API
- 所有 API URI 必须在 `URIs.java` 中统一定义
- Worker 通过 ZooKeeper 发现 Scheduler Leader，不硬编码地址

### 4.3 数据存储约束

> 详细表结构与存储设计见 [docs/database.md](docs/database.md)

**规则：**
- Worker 设计上**不应直接访问数据库**（当前有历史依赖，后续需解耦）
- 大文本数据（日志、参数、快照）存 ES，不存 MySQL
- 所有 MySQL 表必须有 `revision` 字段用于乐观锁
- 所有 MySQL 表必须有 `create_time` 和 `update_time` 字段

### 4.4 任务定义约束

- 任务以 DAG（有向无环图）形式编排，**禁止出现环**
- 每个 Task 必须有至少一个 `isStartingStage = true` 的起始 Stage
- 图必须是**连通的**，不允许存在不可达的悬挂节点
- `toStageName` 引用的 Stage 必须在同一个 Task 内存在
- 任务定义变更必须**升级版本号**（name + version 唯一约束）
- Worker 启动时会与 Scheduler 远端定义做**一致性校验**，不一致则拒绝启动

### 4.5 集群管理约束

- Scheduler 集群通过 ZooKeeper 进行 **Leader 选举**
- 只有 **Leader** 可以：写数据库、管理 Worker 上下线、分配节点
- 非 Leader 节点：监听 ZK runnable 路径，执行任务调度
- Worker 通过**虚拟节点一致性哈希**（200个虚拟节点，Murmur3_128）分配给 Scheduler
- Worker 上线需经过：`online路径注册 → Leader Ping健康检测 → 移入runnable路径`
- Worker 下线需经过：`try-terminate → 等待任务完成 → safe-to-terminate → 移除ZK节点`

### 4.6 状态机约束

**Stage 状态流转（不可逆）：**
```
PENDING → RUNNING → SUCCEEDED
                  → FAILING → FAILED
                  → TERMINATING → TERMINATED
                  → RESCHEDULING → RESCHEDULED
                  → TIME_OUT
                  → SKIPPED
```

**Node 状态流转：**
```
INITIALIZING → RUNNABLE → TERMINATING → SAFE_TO_TERMINATE → TERMINATED
```

**规则：**
- 状态更新必须通过 `revision` 乐观锁防止并发冲突
- 状态流转必须校验当前状态是否允许转换

### 4.7 编解码器约束

- 所有 `InputCodec` 必须继承抽象类（非接口），以便通过反射获取泛型类型
- 自定义 Codec 必须继承 `JsonInputCodec<T>` 或 `InputCodec<T>`
- Codec 类必须提供**无参构造方法**（通过反射实例化）
- 共享上下文（SharedContext）存储在 Worker 内存中，不经过网络传输

### 4.8 Spring Boot Starter 约束

- Worker Starter 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置
- 自动配置类命名：`XxxAutoConfig`
- 配置属性通过 YAML 文件加载（`free-flow-worker-config.yaml`）
- `@Task` 注解的 Bean Scope 必须为 `prototype`（每次任务执行创建新实例）

### 4.9 并发安全约束

- 任务运行时对象（`TaskRuntimeEnv`）使用 `ConcurrentHashMap` 管理
- Stage 集合（succeed/failed/dispatched）使用 `ConcurrentHashSet`
- Worker 健康检测使用 `ScheduledThreadPoolExecutor` 串行调度
- ZK 监听事件处理使用独立线程池（4线程）
- 涉及多步状态更新的操作使用 `@Transactional`

### 4.10 版本兼容性约束

- 相同 `name + version` 的任务定义，其图结构、类名、参数必须完全一致
- 任务定义变更必须升级 version，不允许修改已发布版本的定义
- Worker 与 Scheduler 的任务定义通过 BFS 逐节点比对验证一致性
