# 1. 定义你的 LangGraph Agent
import ast
import os
from datetime import datetime

from langgraph.graph import StateGraph, MessagesState, START, END
from langgraph.prebuilt import ToolNode
from langchain_openai import ChatOpenAI
from langchain_core.tools import tool


@tool
def search_docs(query: str) -> str:
    """搜索内部文档库。当用户询问某个框架 / 产品 / API 的用法或概念说明时使用。

    Args:
        query: 搜索关键词，尽量使用简短的名词短语，例如 "LangGraph"。
    """
    # 真实项目里这里会去查向量库 / ES，这里用假数据演示
    fake_db = {
        "langgraph": "LangGraph 用 StateGraph 描述节点与边，compile() 之后得到可执行图。",
        "langsmith": "LangSmith 是官方可观测平台，可以追踪每一次 LLM 调用链路。",
    }
    for key, value in fake_db.items():
        if key in query.lower():
            return f"[doc] {value}"
    return f"[doc] 未找到 “{query}” 的精确匹配，返回通用结果：{query} 的相关文档内容..."


@tool
def run_analysis(data: str) -> str:
    """对一段数据做基础统计分析。当用户给出一串数字并希望得到汇总结论时使用。

    Args:
        data: 以逗号分隔的数字字符串，例如 "1,2,3,4"。
    """
    try:
        numbers = [float(x) for x in data.replace("，", ",").split(",") if x.strip()]
    except ValueError:
        return f"[analysis] 输入无法解析为数字：{data}"
    if not numbers:
        return "[analysis] 没有拿到有效数字。"
    return (
        f"[analysis] count={len(numbers)}, sum={sum(numbers):g}, "
        f"mean={sum(numbers) / len(numbers):.4g}, "
        f"min={min(numbers):g}, max={max(numbers):g}"
    )


@tool
def calculator(expression: str) -> str:
    """执行精确的数学计算。当需要四则运算、幂运算、取余等计算时使用。

    Args:
        expression: 数学表达式，例如 "(1+2)*3" 或 "2**10"。
    """
    # 只放行白名单 AST 节点，避免直接用 eval 带来的代码注入风险
    allowed = (
        ast.Expression, ast.BinOp, ast.UnaryOp, ast.Constant,
        ast.Add, ast.Sub, ast.Mult, ast.Div, ast.Pow, ast.Mod,
        ast.FloorDiv, ast.USub, ast.UAdd,
    )
    try:
        tree = ast.parse(expression, mode="eval")
        for node in ast.walk(tree):
            if not isinstance(node, allowed):
                return f"[calculator] 表达式包含不被允许的语法：{type(node).__name__}"
        return f"[calculator] {expression} = {eval(compile(tree, '<calc>', 'eval'))}"
    except Exception as exc:
        return f"[calculator] 计算出错：{exc}"


@tool
def get_weather(city: str) -> str:
    """查询指定城市的当前天气。当用户询问天气、出行建议时使用。

    Args:
        city: 中文城市名，例如 "杭州"。
    """
    # 真实项目里替换成高德 / OpenWeather 的 HTTP 请求
    mock = {"杭州": ("多云", 26), "北京": ("晴", 31), "上海": ("小雨", 24)}
    desc, temp = mock.get(city, ("未知", 25))
    return f"[weather] {city}：{desc}，气温 {temp}℃"


@tool
def get_current_time() -> str:
    """获取服务器当前时间。当用户问“现在几点”“今天几号”时使用。"""
    return f"[time] {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"


# bind_tools 会把上面每个函数的「函数名 + docstring + 参数类型」转成 JSON Schema
# 塞进请求里，模型据此决定要调用哪个工具、传什么参数
tools = [search_docs, run_analysis, calculator, get_weather, get_current_time]
llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1").bind_tools(tools)


def print_state(tag: str, state: MessagesState) -> None:
    """把当前 state 里的 messages 一条条打出来（调试用）。

    这样能直观看到：每跑一轮循环，节点只是往同一个列表的尾部「追加」了新消息，
    并不是各自维护一份历史。所以打印出来的条数是单调递增的。
    """
    msgs = state["messages"]
    print(f"\n===== {tag}｜state['messages'] 共 {len(msgs)} 条 =====")
    for i, msg in enumerate(msgs):
        if getattr(msg, "tool_calls", None):
            # AIMessage 且表示“我想调用某个工具”
            detail = f"tool_calls={[tc['name'] for tc in msg.tool_calls]}"
        elif msg.type == "tool":
            # ToolMessage，工具的执行结果
            detail = f"name={msg.name}, content={str(msg.content)[:60]!r}"
        else:
            # HumanMessage / 普通的 AIMessage
            detail = f"content={str(msg.content)[:60]!r}"
        print(f"  [{i}] {type(msg).__name__:<14} {detail}")


def agent_node(state: MessagesState):
    """Agent 节点：把当前全部消息历史交给 LLM。

    LLM 的回复有两种可能：
    - 普通文本（最终答案）
    - 带 tool_calls 的 AIMessage（表示“我想调用某个工具”）
    两种都会作为一条 AIMessage 追加到 messages 里。
    """
    print_state("agent 节点收到的 state", state)
    response = llm.invoke(state["messages"])
    kind = "要求调用工具" if response.tool_calls else "给出最终回答"
    print(f"  -> LLM 返回 {type(response).__name__}（{kind}）")
    return {"messages": [response]}


def should_continue(state: MessagesState):
    """条件路由函数：只看最后一条消息，决定下一步走向哪个节点。

    注意：这里返回值必须是「节点名」或 END 常量，不是布尔值。
    """
    print_state("should_continue 里看到的 state", state)
    last = state["messages"][-1]
    if last.tool_calls:
        # 模型要求调用工具 -> 去 tools 节点执行
        print("  -> 最后一条带 tool_calls，路由到 tools")
        return "tools"
    # 模型给出了最终回答 -> 结束
    print("  -> 最后一条是最终回答，路由到 END")
    return END


# ============================ 构图三件事 ============================
# ① 声明 state schema（定义「状态长什么样」）
# ② 注册节点（定义「有哪几个执行单元」）
# ③ 连边（定义「执行顺序 / 分支 / 循环」）
# ===================================================================

# ① 传进去的 MessagesState 是「类型」，不是实例，它不携带任何数据，
#    只是给图的一份 state 说明书。LangGraph 会在编译期读它的类型注解，
#    为每个字段建一个 channel，并据此决定「节点返回的增量怎么合并进全局 state」。
#    它等价于（本质就是一个dict，里面有一个“messages”的kv，value是一个列表，value[0]还是一个列表：
#        class MessagesState(TypedDict):
#            messages: Annotated[list[AnyMessage], add_messages]
#    其中 add_messages 是 reducer：让 messages 走「追加合并」而不是「覆盖」。
#    意义就是，让graph在运行的时候，将新加入的消息例如工具执行结果追加到末尾，然后在下一次循环中，自动将历史交互信息和新增的信息带给llm
#    正因如此，agent_node / ToolNode 只 return 新增的那几条消息，历史也不会丢。
#    对比：运行时 app.invoke({...}) 传入的 dict 才是真正的数据。
graph = StateGraph(MessagesState)

# ② 注册节点：第一个参数是节点名，后续连边时用这个名字来引用节点。
graph.add_node("agent", agent_node)       # 调 LLM：产出最终回答，或产出 tool_calls
graph.add_node("tools", ToolNode(tools))  # 官方预置节点：真正执行 tool_calls

# ③ 连边。START / END 是虚拟的入口和出口节点，本身不执行任何逻辑。
# 入口边：图一启动就先跑 agent
graph.add_edge(START, "agent")

# 条件边：不写死下一个节点，而是交给 should_continue 的返回值决定
#   返回 "tools" -> 跳到 tools 节点
#   返回 END     -> 直接结束整个图
graph.add_conditional_edges("agent", should_continue)

# 回边：工具执行完再回到 agent，让它带着工具结果继续推理。
# 正是这条边把 agent -> tools -> agent 串成了循环，直到模型不再要求调用工具。
graph.add_edge("tools", "agent")

# 编译：校验图结构（有没有孤立节点、是否可达等）并生成可执行对象。
# 只有 compile() 之后的 app 才能 invoke。
app = graph.compile()

# 2. 本地测试
# 用 __main__ 保护起来：部署时（langgraph.json 指向 ./graph_agent.py:app）
# API Server 会 import 本模块，若不加保护，这里的 invoke 会在服务启动时被白跑一次。
if __name__ == "__main__":
    # 运行后可以从打印中看到：state["messages"] 的条数只增不减，
    # 而且每轮循环 agent 都能看到之前所有的历史 —— 这正是 add_messages 这个 reducer 的效果。
    result = app.invoke({"messages": [{"role": "user", "content": "搜索 LangGraph 文档"}]})
    print_state("图执行结束后的最终 state", result)
    print("\n最终回答：", result["messages"][-1].content)
