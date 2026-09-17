# internal_api_mcp.py —— 把公司内部 REST API 封装成 MCP 工具
import os, httpx
from mcp.server import FastMCP

app = FastMCP("internal-company-tools")
API_TOKEN = os.environ["INTERNAL_API_TOKEN"]
HEADERS = {"Authorization": f"Bearer {API_TOKEN}"}

# =============================================================================
# 【核心】@app.tool()：把下面这个「普通函数」注册成一个 MCP 工具
# =============================================================================
# 它不会改变函数的任何执行逻辑，函数体该干嘛还是干嘛。
# 它做的是「自省（introspection）」：读取函数名、参数的类型注解、docstring，
# 自动生成一份「工具说明书」，登记到 app 这个 MCP Server 上。
#
# Host（Claude Code / Cursor 等）把本脚本当子进程启动后，会发 tools/list
# 把这份说明书取走塞给大模型，由模型自己决定「什么时候调、参数怎么填」。
#
# 注册完之后，等价于向外界广播了下面这段 JSON：
#   {
#     "name": "get_deployment_status",                     # ← 来自函数名
#     "description": "查询指定服务在指定环境的部署状态。",    # ← 来自 docstring
#     "inputSchema": {                                     # ← 来自参数类型注解
#       "type": "object",
#       "properties": {
#         "service": {"type": "string"},                            # 无默认值 → 必填
#         "env":     {"type": "string", "default": "production"}     # 有默认值 → 选填
#       },
#       "required": ["service"]
#     }
#   }
#
# ⚠️ 由此带来的三个重要认知：
#   1. docstring 不是「给人看的注释」，而是「给大模型看的 API 文档」，
#      它是协议的一部分。写得含糊 → 模型不知道该不该调你、参数怎么填。
#   2. 类型注解（service: str）也不只是给 IDE 用的，它会被转成 JSON Schema，
#      是模型填参数时的唯一依据。
#   3. 工具函数必须是 async def，内部只能用异步库（httpx.AsyncClient，
#      不能用同步的 requests），否则会阻塞整个 MCP Server 的事件循环。
@app.tool()
async def get_deployment_status(service: str, env: str = "production") -> str:
    """查询指定服务在指定环境的部署状态。"""   # ← 这句会原样进入 description 字段
    # 以下是纯业务逻辑，和写一个普通 Python 函数没有任何区别
    async with httpx.AsyncClient() as client:
        resp = await client.get(
            f"https://api.internal.company.com/deployments/{service}",
            params={"env": env}, headers=HEADERS)
        data = resp.json()
    # 返回值：str 会被 MCP 自动包装成 content:[{"type":"text","text":"..."}]
    # 这段文本就是模型最终能「看到」的 Observation。工具返回值本质就是一段
    # prompt，是直接注入模型上下文的内容，所以这里不 return 原始 data，而是：
    #   1) 过滤掉多余字段，只留 version/health，省 token、避免稀释注意力；
    #   2) 用 v / @ / () 等标记把字段语义补回（裸 JSON 的 key 拼进文本会丢失）；
    #   3) 把入参 service、env 回显进结果——多轮查多个服务时，让每条结果自带
    #      主语，否则模型分不清 "v1.8.2 (healthy)" 到底说的是哪个服务。
    # （下层由 FastMCP 包成 content 块数组，因工具也能返回 image/audio 等资源。）
    #
    # ⚠️ 敏感信息（token、原始响应头...）不要往外抛：工具边界就是权限边界，
    #    模型只能拿到你返回的这段字符串，拿不到 HEADERS 里的 token。
    # ⚠️ 真实项目建议包一层 try/except：出错时把错误作为文本返回，让模型自己
    #    决定重试还是报告用户（MCP 的约定），而不是让异常冒泡把 Server 搞崩。
    return f"{service} @ {env}: v{data['version']} ({data['health']})"

if __name__ == "__main__":
    app.run()
