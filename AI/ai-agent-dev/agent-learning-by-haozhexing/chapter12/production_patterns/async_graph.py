"""异步工具 + LangGraph Agent 示例（纯本地模拟，脚本自身不访问外部 HTTP）。

教学要点
--------
1. 工具函数声明成 `async def`，图用 `ainvoke` 驱动，等待 I/O 时会让出事件循环；
2. 原来那两个工具是真的去请求 `api.example.com`，既跑不通也没必要。
   这里改成「本地假数据 + asyncio.sleep 模拟网络延迟」，离线可跑，
   而且延迟是可控的，方便观察 `ToolNode` 对多个工具调用是**并发**执行的；
3. 将来要接真实服务，只需把工具里的 `_simulate_network_io(...)` 换成
   aiohttp / httpx 的 await 调用，图结构、ToolNode、并发行为都不用改。
"""
import asyncio
import os
import sys
import time

from langchain_core.tools import tool
from langchain_openai import ChatOpenAI
from langgraph.graph import START, MessagesState, StateGraph
from langgraph.prebuilt import ToolNode, tools_condition

# ---------------------------------------------------------------------------
# 本地假数据：分别充当「搜索索引」和「本地缓存的网页」
# ---------------------------------------------------------------------------
FAKE_SEARCH_INDEX = {
    "langchain": (
        "LangChain 1.x 把高层 Agent API 收敛到 langchain.agents.create_agent；"
        "AgentExecutor / create_openai_tools_agent 移到了兼容包 langchain_classic.agents。"
    ),
    "langgraph": (
        "LangGraph 用 StateGraph + MessagesState 搭有状态 Agent 循环："
        "ToolNode 负责执行工具，tools_condition 负责判断「还要不要再调工具」。"
    ),
    "async": (
        "异步 Agent 的价值在于等待 I/O 时让出事件循环：一次对话里多个工具调用可以并发跑，"
        "而不是串行占着线程干等。"
    ),
}

FAKE_PAGES = {
    "langchain-docs": (
        "LangChain 官方文档：核心抽象是 Runnable（invoke / batch / stream 及其 async 版本），"
        "LCEL 用 | 把组件串成链，链天然支持 ainvoke / astream。"
    ),
    "langgraph-docs": (
        "LangGraph 官方文档：以图的方式描述 Agent 循环，节点是普通函数或 async 函数，"
        "编译出的 graph 支持 invoke / stream 以及 ainvoke / astream。"
    ),
}


async def _simulate_network_io(label: str, seconds: float) -> None:
    """模拟一次网络往返：真实场景下这段时间就是卡在 socket 上等数据。"""
    started = time.perf_counter()
    await asyncio.sleep(seconds)
    print(f"    [本地模拟 I/O] {label} 用时 {time.perf_counter() - started:.2f}s")


@tool
async def async_search(query: str) -> str:
    """异步搜索资料（本地模拟远程搜索 API，不发真实请求）"""
    await _simulate_network_io(f"search({query!r})", 0.6)

    text = query.lower()
    hits = [doc for key, doc in FAKE_SEARCH_INDEX.items() if key in text]
    if not hits:
        return (
            f"[本地模拟] 没有命中 {query!r}；可检索的关键词："
            + "、".join(FAKE_SEARCH_INDEX)
        )
    return "\n".join(f"- {doc}" for doc in hits)


@tool
async def async_fetch_url(url: str) -> str:
    """异步获取网页内容（本地模拟，不发真实请求）"""
    await _simulate_network_io(f"fetch({url})", 0.8)

    key = url.rstrip("/").rsplit("/", 1)[-1].lower()
    page = FAKE_PAGES.get(key)
    if page is None:
        return (
            f"[本地模拟] 未收录 {url}；可用页面："
            + "、".join(f"local://{k}" for k in FAKE_PAGES)
        )
    return page[:500]


# ---------------------------------------------------------------------------
# 在 Agent 中使用异步工具
# ---------------------------------------------------------------------------
tools = [async_search, async_fetch_url]

llm = ChatOpenAI(
    model="qwen3.7-flash",
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    streaming=True,
).bind_tools(tools)


async def agent_node(state: MessagesState):
    response = await llm.ainvoke(state["messages"])
    return {"messages": [response]}


graph = StateGraph(MessagesState)
graph.add_node("agent", agent_node)
graph.add_node("tools", ToolNode(tools))
graph.add_edge(START, "agent")
graph.add_conditional_edges("agent", tools_condition)
graph.add_edge("tools", "agent")

app = graph.compile()


async def main():
    """完整链路：真实调用 LLM，工具部分走本地模拟。"""
    question = "帮我查一下 LangChain 的最新情况，以及 langgraph 怎么用工具"
    started = time.perf_counter()
    result = await app.ainvoke(
        {
            "messages": [
                {
                    "role": "system",
                    "content": "先调用 async_search 工具查资料，再基于工具结果回答。",
                },
                {"role": "user", "content": question},
            ]
        }
    )
    print("\n=== 最终回答 ===")
    print(result["messages"][-1].content)
    print(f"\n总耗时 {time.perf_counter() - started:.2f}s")


async def demo_tools_only():
    """完全不调 LLM，只本地跑工具，用来直观感受「并发 I/O 省时间」。

    3 个调用总共只需等待 max(0.6, 0.6, 0.8) ≈ 0.8s，而不是 0.6+0.6+0.8 = 2.0s。
    """
    started = time.perf_counter()
    results = await asyncio.gather(
        async_search.ainvoke({"query": "langchain 最新版本"}),
        async_search.ainvoke({"query": "langgraph 怎么用工具"}),
        async_fetch_url.ainvoke({"url": "local://langchain-docs"}),
    )
    print("=== 并发调用结果 ===")
    for text in results:
        print(f"- {text.splitlines()[0]}")
    print(f"\n3 次调用总耗时 {time.perf_counter() - started:.2f}s")


if __name__ == "__main__":
    if "--tools-only" in sys.argv:
        asyncio.run(demo_tools_only())
    else:
        asyncio.run(main())
