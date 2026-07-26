# Free-Flow 编码规范

## 1. 包结构约定

```
org.talk.is.cheap.project.free.flow
├── common/                    # free-flow-common 模块
│   ├── enums/                 # 枚举定义
│   ├── exception/             # 异常体系
│   ├── message/               # HTTP消息体（Req/Resp/DTO）
│   │   └── impl/             # 具体消息实现
│   │       ├── dto/          # 数据传输对象
│   │       ├── scheduler/    # Scheduler相关消息
│   │       └── worker/       # Worker相关消息
│   ├── router/                # URI路由常量
│   ├── task/                  # 任务定义相关
│   │   ├── codec/            # 编解码器
│   │   └── definition/bo/    # 业务对象
│   └── utils/                 # 工具类
├── scheduler/                 # free-flow-scheduler 模块
│   ├── cluster/              # 集群管理（client/controller/event/service）
│   ├── config/               # 配置类
│   ├── task/                 # 任务调度（client/controller/service）
│   └── utils/                # 工具类
└── starter/
    ├── worker/               # free-flow-worker-starter 模块
    │   ├── client/           # 调用Scheduler的Feign客户端
    │   ├── cluster/          # Worker集群注册
    │   ├── config/           # 自动配置类
    │   ├── task/
    │   │   ├── definition/   # 任务定义（annotation/controller/service）
    │   │   └── driver/       # 任务驱动（controller/runtime/service）
    │   └── listener/         # 启动监听器
    └── repository/           # free-flow-repository-starter 模块
        ├── config/           # 数据源/ES/Redis配置
        ├── dao/              # MyBatis Mapper
        ├── domain/           # 领域对象（pojo/es）
        └── service/          # 数据访问服务
```

---

## 2. 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 枚举类 | 大驼峰，表达状态/类型语义 | `TaskStageStatus`, `NodeType` |
| 枚举值 | 全大写下划线 | `FAILED_TO_START`, `SAFE_TO_TERMINATE` |
| Controller | `XxxController` | `TaskProcessController` |
| Service | `XxxService` / `XxxManager` | `WorkerClusterManager`, `TaskRuntimeService` |
| Feign Client | `XxxClient` | `WorkerTaskDriverClient`, `SchedulerTaskProcessClient` |
| DTO/BO | `XxxDTO` / `XxxBO` | `TaskDefinitionDTO`, `StageDefinitionBO` |
| 请求/响应 | `XxxReq` / `XxxResp` | `WorkerStartTaskReq`, `PrepareStageResp` |
| 配置类 | `XxxAutoConfig` / `XxxProperty` | `CuratorAutoConfig`, `ZKPathProperty` |
| 编解码器 | `XxxCodec` / `XxxInputCodec` | `JsonInputCodec`, `SimpleStringInputCodec` |
| 工具类 | `XxxUtil` / `XxxUtils` | `VerifyUtil`, `ReflectUtil`, `IPUtil` |

---

## 3. 枚举定义模式

所有枚举必须遵循统一模式：

```java
@AllArgsConstructor
public enum XxxStatus {
    VALUE_A(0, "描述A"),
    VALUE_B(1, "描述B");

    @Getter
    private final Integer status;  // 或 code/type
    @Getter
    private final String desc;

    private final static Map<Integer, XxxStatus> STATUS_MAP = new HashMap<>();

    static {
        for (XxxStatus value : XxxStatus.values()) {
            STATUS_MAP.put(value.getStatus(), value);
        }
    }

    public static XxxStatus getByStatus(Integer status) {
        return STATUS_MAP.get(status);
    }
}
```

---

## 4. HTTP 响应体规范

统一使用 `HttpBody<T>` 或专用 Resp 对象：

```java
// 通用响应
HttpBody<String> resp = new HttpBody<>();
resp.success(data);
resp.fail(ResultCode.FAIL, "错误信息");

// 专用响应（继承HttpBody）
WorkerStartTaskResp resp = new WorkerStartTaskResp();
resp.success(null);
resp.fail(ResultCode.NO_TASK_DEFINITION, "描述");
```

---

## 5. 参数校验规范

使用 `VerifyUtil` 进行前置条件校验（抛出 `VerifyException`）：

```java
// 非空校验
VerifyUtil.requireNotNull(data, "请求数据不能为空");
VerifyUtil.requireAllNotNull("存在入参为空", data, data.getStageName(), data.getTaskExecutionId());

// 条件校验
VerifyUtil.requireTrue(condition, "条件不满足时的错误信息");
VerifyUtil.requireFalse(duplicateExists, String.format("发现重复: %s", name));

// 字符串校验
VerifyUtil.requireNotBlank(taskName, "taskName不能为空");
VerifyUtil.requireEqual(expected, actual, "值不一致");
```

---

## 6. 异常处理规范

- **Controller层**：统一 try-catch，捕获异常后设置 `resp.fail()`，不向上抛出
- **Service层**：使用 `VerifyUtil` 做前置校验，业务异常直接抛出
- **自定义异常**：
  - `IllegalTaskDefinitionException` — 任务定义非法
  - `TaskExecutionException` — 任务执行异常（携带errorCode）
  - `VerifyException`（Guava）— 前置条件不满足

```java
// Controller 标准模式
@RequestMapping(path = URIs.Xxx, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
@ResponseBody
public HttpBody<String> someApi(@RequestBody SomeReq req) {
    HttpBody<String> resp = new HttpBody<>();
    try {
        // 业务逻辑
        resp.success("");
    } catch (Exception e) {
        log.error("操作失败", e);
        resp.fail(ResultCode.FAIL, e.getMessage());
    }
    return resp;
}
```

---

## 7. 日志规范

- 使用 `@Slf4j` 注解（Lombok）
- 关键操作使用 `log.info`，异常使用 `log.error`
- 日志中包含关键业务ID便于追踪

```java
log.info("new online worker, path: {}, workerNodeAddress: {}", zkPath, workerNodeAddress);
log.error("启动任务（stageExeId:{}）失败", stageExeId, e);
```

---

## 8. 并发控制规范

- 数据库层面：所有表使用 `revision` 字段做乐观锁
- 内存层面：使用 `ConcurrentHashMap`、`ConcurrentHashSet`
- 分布式层面：Redisson 分布式锁
- 需要同步的代码块使用 `synchronized(this)` 并注释说明不能与哪些操作并发

---

## 9. 注解使用规范

- `@Task` 标注的类自动成为 Spring Bean（组合了 `@Component`），Scope 为 `prototype`
- `@RunnableStage` 标注方法，方法签名只能是：
  - 无参数：`public void methodName()`
  - 单参数：`public void methodName(StageRuntimeEnv<T> env)`

---

## 10. URI 路由规范

所有 URI 统一定义在 `URIs.java` 中，按角色分组：

```java
public static class SchedulerTaskProcessURIs {
    private static final String PREFIX = "/scheduler/task/process";
    public static final String START = PREFIX + "/start";
    public static final String STAGE_COMPLETE = PREFIX + "/stage/complete";
}
```

命名规则：`/{角色}/{领域}/{操作}`

---

## 11. 数据库访问规范

MySQL、ES、Redis 数据库的访问均通过 `free-flow-repository-starter` 模块完成。

### 11.1 DAO 层

项目提供两种 DAO 层访问数据库：

1. **MyBatis-Plus 自动生成层（禁止修改）**
   - 接口：`free-flow-repository-starter/.../dao/mbg/`
   - XML：`free-flow-repository-starter/.../mappers/mbg/`
   - 这些代码均为 MyBatis-Plus 生成，**禁止对其中的文件做任何修改**

2. **自定义 DAO 层（允许开发）**
   - 接口：`free-flow-repository-starter/.../dao/customized/`
   - XML：`free-flow-repository-starter/.../mappers/customized/`
   - 在 MyBatis-Plus 提供的访问层无法满足需求时，允许在该路径下自行开发 SQL 代码

### 11.2 Service 层

- 上述两种 DAO 层通过 `free-flow-repository-starter/.../service/` 中的 Service 层汇总对外提供
- 允许对 Service 做进一步包装
- 高频访问的 Service 封装放置在 `free-flow-repository-starter/.../service/derived/` 中
