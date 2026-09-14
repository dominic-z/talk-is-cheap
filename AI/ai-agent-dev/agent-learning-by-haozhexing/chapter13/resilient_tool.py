import time
import math
import os
from langchain_core.messages import HumanMessage, AIMessage, ToolMessage
from langchain_core.tools import tool
from langchain_core.runnables import RunnableConfig

from langgraph.constants import START, END
from langgraph.graph import MessagesState, StateGraph
from langchain_openai import ChatOpenAI


# ======================================================================
# 工具定义
# ======================================================================
# 一个「会失败」的工具：前两次调用故意抛错，第三次起才成功。
# 用它来演示「失败 → 自动重试 → 最终成功」的完整链路。
_flaky_calls = {"n": 0}


@tool
def flaky_calculate(expression: str) -> str:
    """计算数学表达式（演示用：前两次会模拟失败，第三次起成功）"""
    _flaky_calls["n"] += 1
    if _flaky_calls["n"] <= 2:
        raise RuntimeError(f"模拟下游服务抖动（第 {_flaky_calls['n']} 次调用失败）")
    try:
        result = eval(expression, {"__builtins__": {}},
                      {k: getattr(math, k) for k in dir(math) if not k.startswith("_")})
        return str(result)
    except Exception as e:
        return f"计算错误：{e}"


@tool
def always_fail(expression: str) -> str:
    """一个始终失败的工具，用于演示「超过重试上限 → 降级(fallback)」"""
    raise RuntimeError("依赖的下游服务彻底不可用")


tools = [flaky_calculate, always_fail]
tools_by_name = {t.name: t for t in tools}


def execute_tool(tool_call: dict) -> str:
    """按 tool_call 分发到对应工具并真正执行"""
    name = tool_call["name"]
    args = tool_call.get("args") or {}
    print(f"[调用工具] {name}({args})")
    if name in tools_by_name:
        return tools_by_name[name].invoke(args)
    return "未知工具"


# ======================================================================
# 模式 1：节点内部 try-except（最灵活，保证不崩溃）
# ======================================================================
def resilient_tool_node(state: MessagesState) -> dict:
    """带错误处理的工具节点。
    关键：工具抛异常时，不向上抛出（否则整个图崩溃），
    而是把错误信息包成 ToolMessage 返回，让 Agent 有机会自我纠正。"""
    last_msg = state["messages"][-1]
    results = []

    for tool_call in last_msg.tool_calls:
        try:
            result = execute_tool(tool_call)
        except Exception as e:
            # 工具失败时，返回错误信息而非抛出异常，让 Agent 有机会自我纠正
            result = f"工具执行失败：{str(e)}。请尝试其他方法。"

        results.append(ToolMessage(content=result, tool_call_id=tool_call["id"]))

    return {"messages": results}


# ======================================================================
# 模式 2：带重试计数的条件路由（图级别的自动重试）
# ======================================================================
class RetryState(MessagesState):
    # 进一步理解MessagesState的作用，RetryState本质上就是在MessagesState的基础上，新增了两个属性
    # {messages:List[AnyMessage], retry_count: 1, max_retries:3}，本质上就是这个dict
    retry_count: int = 0
    max_retries: int = 3


def should_retry(state: RetryState) -> str:
    """重试决策：依据上一条消息是否「失败」以及重试次数决定走向"""
    last_msg = state["messages"][-1]

    retry_count = state.get("retry_count", 0)
    max_retries = state.get("max_retries", 3)
    if "失败" in last_msg.content and retry_count < max_retries:
        return "retry"
    elif "失败" in last_msg.content:
        return "fallback"          # 超过重试次数，走降级路径
    else:
        return "success"


def retry_node(state: RetryState) -> dict:
    """重试节点：重新发起上一次的工具调用，并把 retry_count + 1。
    注意：上一步产物是「错误 ToolMessage」，已经没有 tool_calls，
    所以要回退找到最近一条带 tool_calls 的 AIMessage 重新发出。"""
    last_ai = next(m for m in reversed(state["messages"])
                   if getattr(m, "tool_calls", None))
    new_calls = []
    for tc in last_ai.tool_calls:
        new_calls.append({
            "name": tc["name"],
            "args": tc["args"],
            "id": f"{tc['id']}_retry{state['retry_count'] + 1}",
        })
    return {
        "messages": [AIMessage(content="", tool_calls=new_calls)],
        "retry_count": state.get("retry_count", 0) + 1,
    }


def fallback_node(state: RetryState) -> dict:
    """降级节点：重试耗尽后给出兜底答复，而不是让流程挂掉。"""
    return {"messages": [AIMessage(
        content="⚠️ 工具持续失败，已走降级逻辑：返回缓存结果 / 提示用户稍后重试。"
    )]}


# ======================================================================
# Agent 节点（两种实现）
# ======================================================================
# 演示用的 mock agent：固定发起一次工具调用，不依赖 LLM / 网络，
# 因此下面的「调用例子」可以确定性地跑通（无论有无 API key）。
CURRENT_TOOL = "flaky_calculate"


def mock_agent_node(state: RetryState) -> dict:
    """不依赖 LLM 的演示 agent：固定对某个工具发起一次调用。
    真实场景把本函数替换成 real_agent_node 即可。"""
    # 相当于就定死llm要这样回答，其实本质上就是没有llm参与的。
    return {"messages": [AIMessage(content="", tool_calls=[{
        "name": CURRENT_TOOL,
        "args": {"expression": "sqrt(2) * pi"},
        "id": "call_demo",
    }])]}


def real_agent_node(state: RetryState) -> dict:
    """真实 agent：调用大模型（需要 QWEN_API_KEY）。
    取消下面注释、并把 graph 里的 agent 节点换成本函数即可启用。"""
    llm_with_tools = ChatOpenAI(
        model="qwen3.7-flash",
        api_key=os.getenv("QWEN_API_KEY"),
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    ).bind_tools(tools)
    response = llm_with_tools.invoke(state["messages"])
    return {"messages": [response]}


def _has_tool_calls(state: RetryState) -> str:
    last_msg = state["messages"][-1]
    if getattr(last_msg, "tool_calls", None):
        return "tools"
    return END


# ======================================================================
# 构建图
# ======================================================================
def build_graph():
    graph = StateGraph(RetryState)
    # 演示默认用 mock_agent_node；接入真实 LLM 时换成 real_agent_node
    graph.add_node("agent", mock_agent_node)
    graph.add_node("tools", resilient_tool_node)
    graph.add_node("retry", retry_node)
    graph.add_node("fallback", fallback_node)

    graph.add_edge(START, "agent")
    graph.add_conditional_edges("agent", _has_tool_calls, {"tools": "tools", END: END})
    graph.add_conditional_edges("tools", should_retry,
                                {"retry": "retry", "fallback": "fallback", "success": END})
    graph.add_edge("retry", "tools")
    graph.add_edge("fallback", END)

    return graph.compile()


# ======================================================================
# 调用例子
# ======================================================================
if __name__ == "__main__":
    # 模式 3：通过 RunnableConfig 设置递归上限，防止重试死循环把栈打爆。
    # 重试次数越多，需要的 recursion_limit 越大。
    config = RunnableConfig(recursion_limit=50,
                            configurable={"thread_id": "demo-resilient"})

    app = build_graph()  # 重试上限由输入决定，两个场景共用一个图

    print("===== 场景1：工具前两次失败，第三次成功（自动重试后成功）=====")
    _flaky_calls["n"] = 0  # 重置计数器，保证演示可重复
    result = app.invoke(
        {"messages": [HumanMessage(content="帮我算一下 sqrt(2) * pi")],
         "retry_count": 0, "max_retries": 3},
        config=config,
    )
    for m in result["messages"]:
        print(f"[{type(m).__name__}] {m.content}")
    print(f"\n=> 最终 retry_count = {result['retry_count']}（说明经历了 {result['retry_count']} 次重试后成功）")

    print("\n===== 场景2：工具始终失败 → 超过重试上限 → 降级(fallback) =====")
    CURRENT_TOOL = "always_fail"  # 切换到始终失败的工具，触发降级
    result2 = app.invoke(
        {"messages": [HumanMessage(content="算 sqrt(2) * pi")],
         "retry_count": 0, "max_retries": 1},  # 只给 1 次重试机会，便于触发降级
        config=config,
    )
    for m in result2["messages"]:
        print(f"[{type(m).__name__}] {m.content}")
    print(f"\n=> 最终 retry_count = {result2['retry_count']}（重试耗尽，进入 fallback 而非崩溃）")
