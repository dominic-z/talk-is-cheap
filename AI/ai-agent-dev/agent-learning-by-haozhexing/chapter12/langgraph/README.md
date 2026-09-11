# 用 `langgraph dev` 在本地跑一个最小 Agent 服务

目标：把 `graph_agent.py` 里 `compile()` 出来的图变成一个本地 HTTP 服务，用 `curl` 就能调用。

特点：**不需要 Docker，不需要 LangSmith，不需要 License Key**。只有两个配置文件 + 一条启动命令。

---

## 1. 环境准备

Python 依赖装在 miniconda 的独立环境里，不要用系统 Python。

```bash
# 激活环境（路径按本机情况调整）
conda activate agent-dev

# 安装 LangGraph CLI；[inmem] 是本地开发用的进程内运行时
python -m pip install -U "langgraph-cli[inmem]"

# 校验
langgraph --version
which python          # 必须落在 conda 环境目录内
```

本次实测安装到的版本：

| 包 | 版本 |
| --- | --- |
| langgraph-cli | 0.4.31 |
| langgraph-api | 0.14.0 |
| langgraph-runtime-inmem | 0.34.0 |
| langgraph（此前已装） | 1.2.11 |

> 注意：安装 CLI 时会把 `opentelemetry-api` 升到 1.42.1，与本环境里的 `letta 0.16.8`（要求 `==1.30.0`）冲突。如果之后还要用 letta，建议单独开一个环境，别和这个混用。

---

## 2. 目录里的三个文件

```
chapter12/langgraph/
├── graph_agent.py     # 图定义，末尾是 app = graph.compile()
├── langgraph.json     # LangGraph CLI 读取的项目描述
└── .env               # 只有一个 QWEN_API_KEY（已被 .gitignore 忽略）
```

### 2.1 `langgraph.json`

```json
{
  "dependencies": ["langgraph", "langchain-openai"],
  "graphs": {
    "agent": "./graph_agent.py:app"
  },
  "env": ".env"
}
```

| 字段 | 作用 |
| --- | --- |
| `dependencies` | 启动前要确保存在的依赖。这里直接写包名，是因为本目录没有 `pyproject.toml` / `requirements.txt`；若写成 `["."]`，CLI 会去读当前目录的依赖清单，读不到会报错。 |
| `graphs` | `逻辑名 -> "文件路径:模块级变量名"`。CLI 会 import 这个文件并取出 `app` 对象挂到 HTTP 接口上。**必须是真实文件名**，本仓库是 `graph_agent.py`，不是原文示例里的 `agent.py`。 |
| `env` | 启动时加载的环境变量文件。 |

### 2.2 `.env`

```bash
QWEN_API_KEY=sk-xxxxxxxx
```

**这一行不能省。** `graph_agent.py` 是在模块顶层构造 LLM 的：

```python
llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1").bind_tools(tools)
```

也就是说服务 `import` 这个模块的那一刻就要拿到 key，拿不到会直接以 `api_key=None` 失败。

---

## 3. 为什么这里完全不会连 LangSmith

只要 `.env` 里**不出现** `LANGCHAIN_API_KEY` / `LANGSMITH_API_KEY` / `LANGSMITH_TRACING`，就不会有任何 tracing，也不会有上报。

依据在 `langgraph_api/metadata.py` 里（安装包内第 145 行）：

```python
async def metadata_loop() -> None:
    ...
    if not LANGGRAPH_CLOUD_LICENSE_KEY and not LANGSMITH_CONTROL_PLANE_API_KEY:
        logger.info(
            "No license key or control plane API key set, skipping metadata loop"
        )
        return
```

而 `LANGSMITH_CONTROL_PLANE_API_KEY` 默认回落到 `LANGSMITH_API_KEY`，后者又回落到 `LANGCHAIN_API_KEY`（见 `langgraph_api/config/__init__.py`）。三个都不设 → 上报循环直接 `return`。

实测启动日志：

```
No license key or control plane API key set, skipping metadata loop
```

启动横幅里还会打印一行 `🎨 Studio UI: https://smith.langchain.com/studio/?baseUrl=http://127.0.0.1:2024`。那只是**一条提示文本**，不会主动连接，本项目用不到，忽略即可。

---

## 4. 启动

```bash
cd AI/ai-agent-dev/agent-learning-by-haozhexing/chapter12/langgraph
langgraph dev --no-browser --allow-blocking
```

启动后输出：

```
- 🚀 API:      http://127.0.0.1:2024
- 📚 API Docs: http://127.0.0.1:2024/docs
```

**两个参数是必需的（对这个 demo 而言）：**

| 参数 | 原因 |
| --- | --- |
| `--no-browser` | 默认会尝试拉起浏览器，纯命令行环境里没必要。 |
| `--allow-blocking` | `agent_node` 里是同步的 `llm.invoke()` 和 `print()`。dev 模式默认会检测同步阻塞 I/O 并直接抛错，加这个参数放行。不加的话一调用就报 blocking 错误。 |

后台常驻启动（可选）：

```bash
setsid nohup langgraph dev --no-browser --allow-blocking > /tmp/langgraph_dev.log 2>&1 < /dev/null &
```

---

## 5. 实验方式

以下命令在服务运行时执行，全部可以直接复制粘贴。

### 5.1 健康检查

```bash
curl -s http://127.0.0.1:2024/ok
# {"ok":true}
```

### 5.2 确认图已注册

```bash
curl -s -X POST http://127.0.0.1:2024/assistants/search \
  -H 'Content-Type: application/json' -d '{"limit":5}'
```

预期能看到 `"graph_id":"agent"`：

```json
[{"assistant_id":"fe096781-...","graph_id":"agent","name":"agent","version":1}]
```

### 5.3 端到端调用（会真实调用 Qwen）

```bash
curl -s -X POST http://127.0.0.1:2024/runs/wait \
  -H 'Content-Type: application/json' \
  -d '{"assistant_id":"agent","input":{"messages":[{"role":"user","content":"现在几点了"}]}}'
```

返回的是图执行结束后的完整 state。实测拿到的 4 条消息，正是 `agent → tools → agent` 这一圈的完整痕迹：

| # | type | 说明 | 内容 |
| --- | --- | --- | --- |
| 0 | `human` | 用户输入 | 现在几点了 |
| 1 | `ai` | 模型要求调工具 | `tool_calls=[get_current_time]` |
| 2 | `tool` | 工具执行结果 | `[time] 2026-09-11 15:27:08` |
| 3 | `ai` | 最终回答 | 现在是2026年9月11日 15点27分。 |

换个会用到 `search_docs` 的问题（`搜索 LangGraph 文档`）走的是同一条路径，只是工具名不同。这也顺带验证了 `add_messages` reducer 的行为：节点只 return 新增消息，历史靠追加合并，所以消息条数只增不减。

### 5.4 多轮对话（用 thread 保留上下文）

```bash
# 1) 建 thread
curl -s -X POST http://127.0.0.1:2024/threads \
  -H 'Content-Type: application/json' -d '{}'

# 2) 把上一步返回的 thread_id 填进去，同一 thread 内多轮之间共享历史
curl -s -X POST http://127.0.0.1:2024/threads/<thread_id>/runs/wait \
  -H 'Content-Type: application/json' \
  -d '{"assistant_id":"agent","input":{"messages":[{"role":"user","content":"我叫小明"}]}}'
```

> `langgraph dev` 用的是**进程内内存**运行时，thread 和状态都在内存里，服务重启就全没了。要持久化就得换 `langgraph up`（需要 Docker + License Key），本 demo 不需要。

### 5.5 热重载

改完 `graph_agent.py` 不用重启，日志里会出现 `N changes detected`，服务自动 reload。

---

## 6. 停止服务与清理

```bash
# 普通前台启动的，直接 Ctrl-C

# 后台启动的
pkill -f "langgraph[ ]dev"
```

用 `langgraph[ ]dev` 而不是 `langgraph dev` 是为了避免 pkill 匹配到正在执行这条命令的 shell 自身。

运行期间会生成一个 `.langgraph_api/` 目录，存放 checkpoint / queue 等临时文件，可以随时删除，已在根 `.gitignore` 里加了忽略规则：

```
/**/.langgraph_api/
```

---

## 7. 常见问题

| 现象 | 原因 / 解法 |
| --- | --- |
| `api_key=None` 或启动即报鉴权错 | `.env` 没被加载，或 `QWEN_API_KEY` 为空。确认 `langgraph.json` 的 `env` 指向 `.env`。 |
| 一调用就报 blocking 相关错误 | 启动时漏了 `--allow-blocking`。 |
| 端口被占用 | 加 `--port 2025` 换端口。 |
| `No such file or directory: agent.py` | `langgraph.json` 的 `graphs` 路径写错了，本仓库文件名是 `graph_agent.py`。 |
| 调用卡住或超时 | 服务所在环境访问不到 `dashscope.aliyuncs.com`。这个和 Docker 无关，纯网络问题。 |
| 消息在两边都打印了一份 | 正常。服务端 worker 进程执行 `print_state`，日志里能看到输出。 |

---

## 8. 和「部署到 LangGraph Platform」的区别

原文给的是云端 / 容器部署路线：

```bash
langgraph deploy                                    # 托管云，需要登录 + Platform 账号
langgraph build -t my-agent:latest                  # 需要 Docker
docker run -p 8000:8000 my-agent:latest             # 需要 License Key
```

对比本方案：

|  | `langgraph dev`（本方案） | `langgraph build` + `docker run` |
| --- | --- | --- |
| Docker | 不需要 | 需要 |
| License Key | 不需要 | 自托管镜像需要 |
| 持久化 | 无（内存） | 有 |
| 用途 | 本地开发 / 学习 / 调试 | 接近生产形态的自托管 |
| 端口 | 2024 | 容器内 8000 |

所以本 demo 用 `langgraph dev` 就够了，`graph_agent.py` 里的 `app` 对象两边是同一个，切换部署形态不用改图代码。

---

## 附：一页速查

```bash
# 装（conda 环境内）
python -m pip install -U "langgraph-cli[inmem]"

# 文件：langgraph.json + .env（只有 QWEN_API_KEY）

# 跑
langgraph dev --no-browser --allow-blocking

# 验
curl -s http://127.0.0.1:2024/ok
curl -s -X POST http://127.0.0.1:2024/runs/wait \
  -H 'Content-Type: application/json' \
  -d '{"assistant_id":"agent","input":{"messages":[{"role":"user","content":"现在几点了"}]}}'

# 停
pkill -f "langgraph[ ]dev"
```
