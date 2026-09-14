from langchain_core.messages import HumanMessage
from langchain_core.tools import tool
import math
from langchain_openai import ChatOpenAI
import os

from langgraph.constants import END, START
from langgraph.graph import MessagesState, StateGraph


@tool
def calculate(expression: str) -> str:
    """计算数学表达式"""
    print("执行工具calculate")
    try:
        result = eval(expression, {"__builtins__": {}},
                      {k: getattr(math, k) for k in dir(math)})
        return str(result)
    except Exception as e:
        return f"错误：{e}"


tools = [calculate]
llm_with_tools = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1").bind_tools(tools)


# 这里可以理解一下MessagesState到底是啥，他本质是一个dict字典，他有一个属性叫作messages
# {"messages": [response]}就可以看作一个MessagesState，
# 当一个node返回了一个{"messages": [response]}，框架会将这个新的MessagesState和历史的MessagesState合并成一个新的MessagesState
# 简单说，就是把新旧两个MessagesState的messages进行reduce，
# 如何实现的？MessagesState里对于messages的定义，除了类型，还多给了一个元数据，用来指示框架如何进行reduce
def agent_node(state: MessagesState) -> dict:
    response = llm_with_tools.invoke(state["messages"])
    return {"messages": [response]}


def tool_executor(state: MessagesState) -> dict:
    from langchain_core.messages import ToolMessage
    import json

    last_msg = state["messages"][-1]
    results = []

    for tool_call in last_msg.tool_calls:
        if tool_call["name"] == "calculate":
            result = calculate.invoke(tool_call["args"])
        else:
            result = "未知工具"

        results.append(ToolMessage(
            content=str(result),
            tool_call_id=tool_call["id"]
        ))

    return {"messages": results}


def should_use_tools(state: MessagesState) -> str:
    last_msg = state["messages"][-1]
    if hasattr(last_msg, "tool_calls") and last_msg.tool_calls:
        return "tools"
    return END


# 构建图
graph = StateGraph(MessagesState)
graph.add_node("agent", agent_node)
graph.add_node("tools", tool_executor)
graph.add_edge(START, "agent")
graph.add_conditional_edges("agent", should_use_tools)
graph.add_edge("tools", "agent")  # 工具执行后回到 agent

# 编译时挂上 checkpointer（这里用内存版 MemorySaver，进程内持久化；
# 生产环境可换成 SqliteSaver / PostgresSaver 等做跨进程持久化）。
# 它的作用：每次 invoke 结束后把完整 state 保存起来，
# 下一次同 thread_id 的 invoke 启动时再 load 回来，从而让记忆跨调用留存。
from langgraph.checkpoint.memory import MemorySaver
# checkpointer的作用是跨多轮对话的记忆能力，
# 而MessagesState的reduce功能，是在一轮对话中的循环中记录之前的步骤的执行结果或者历史的prompt，本质是服务一次对话
app = graph.compile(checkpointer=MemorySaver())

# thread_id 用来区分不同的会话；同一个 thread_id 的多次调用会共享同一份 state。
config = {"configurable": {"thread_id": "demo-1"}}

print("===== 第一轮对话 =====")
result = app.invoke(
    {"messages": [HumanMessage(content="计算 sqrt(2) * pi")]},
    config=config,
)
for m in result["messages"]:
    print(f"[{type(m).__name__}] {m.content}")
print("最终回答：", result["messages"][-1].content)

print("\n===== 第二轮对话（只传入新消息，不重复传历史）=====")
# 注意：这里只给了「把上一步的结果乘以 2」这一句，并没有把第一轮的消息再传一遍。
# 能接上上下文，是因为 checkpointer 在第一轮结束后保存了完整 state，
# 第二轮开始时按 thread_id 把它 load 回来，再和本轮新消息合并（reducer 负责合并）。
result2 = app.invoke(
    {"messages": [HumanMessage(content="把上一步的结果乘以 2")]},
    config=config,
)
for m in result2["messages"]:
    print(f"[{type(m).__name__}] {m.content}")
print("最终回答：", result2["messages"][-1].content)

# 佐证：同一个 app 换一个全新的 thread_id，就完全不认识上一轮的内容，
# 说明记忆是「按 thread_id 隔离 + 由 checkpointer 持久化」的，而不是 MessagesState 自带。
print("\n===== 对照：换个 thread_id，记忆清零 =====")
result3 = app.invoke(
    {"messages": [HumanMessage(content="把上一步的结果乘以 2")]},
    config={"configurable": {"thread_id": "demo-2"}},
)
print("新会话的最终回答：", result3["messages"][-1].content)

# 逐节点查看状态：用 stream 边跑边打印每个节点产出的 state，
# 能直观看到 agent / tools 各自往 messages 里追加了什么。
print("\n===== 逐节点输出状态（stream）=====")
for event in app.stream(
    {"messages": [HumanMessage(content="计算 sqrt(2) * pi 再乘以 2")]},
    config={"configurable": {"thread_id": "demo-stream"}},
):
    for node, state in event.items():
        if node == "__end__":
            continue
        last = state.get("messages", [])[-1]
        if hasattr(last, "tool_calls") and last.tool_calls:
            print(f"[{node}] 调用：{[tc['name'] for tc in last.tool_calls]}")
        else:
            print(f"[{node}] 回复：{last.content}")
