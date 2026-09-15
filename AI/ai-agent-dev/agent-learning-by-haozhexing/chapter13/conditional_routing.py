from langchain_core.messages import HumanMessage
from langgraph.graph import StateGraph, END, START
from typing import TypedDict, Optional
from langchain_openai import ChatOpenAI
from pydantic import BaseModel, Field
import os

llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1")
# 用 Pydantic + with_structured_output 让模型直接返回结构化对象，
# 而非需要正则去抠的 JSON 文本——字段类型在编码期即可校验。
class CodeReview(BaseModel):
    issues: list[str] = Field(description="发现的问题；无则空列表")
    severity: str = Field(description="high/medium/low")
    approved: bool = Field(description="是否可直接通过")

review_llm = llm.with_structured_output(CodeReview)

# 这些字段都是「裸类型」（没有 Annotated[..., reducer]），
# LangGraph 会给它们建 LastValue 通道 => 新返回值直接「覆盖」旧值，不追加、不累积。
# 所以 issues 每轮会被 analyze 整体替换，旧问题列表不会保留；
# 循环之所以能转起来，靠的是节点手动读旧值再算新值（如 s["iteration"] + 1），而非框架帮忙累积。
# 想让字段「追加累积」就得写 reducer，例如：issues: Annotated[list, operator.add]
class CodeReviewState(TypedDict):
    code: str
    issues: list
    iteration: int
    max_iterations: int
    approved: bool


# 注意这是两个不同的llm
def analyze_code(s: CodeReviewState) -> dict:
    r = review_llm.invoke([HumanMessage(f"审查代码：\n{s['code']}")])
    return {"issues": r.issues, "iteration": s.get("iteration", 0) + 1}

def fix_code(s: CodeReviewState) -> dict:
    # 把问题列表交给模型修复，只回纯代码。
    # 注意：这里「不是」像 MessagesState 那样把历史对话列表整段传给 LLM——
    # 本 state 里没有 messages 字段，也没有 add_messages reducer，
    # 所以每轮给 LLM 的都是「一条全新的 HumanMessage」，上下文不会累积。
    # 历史的延续方式是：手动从当前 CodeReviewState 里取字段（s['code']、s['issues']）
    # 现场拼出一个新 prompt，等价于每轮重建一次「单轮对话」。
    
    return {"code": llm.invoke([HumanMessage(f"修复：\n{s['code']}\n问题：{s['issues']}")]).content}

def route(s: CodeReviewState) -> str:
    """循环控制核心：安全阀 + 达标判定"""
    if s["iteration"] >= s["max_iterations"]:
        return "approve"          # 上限兜底，强制结束
    if not s["issues"]:
        return "approve"          # 无问题直接通过
    return "fix"                  # 否则继续修复循环

graph = StateGraph(CodeReviewState)
graph.add_node("analyze", analyze_code)
graph.add_node("fix", fix_code)
graph.add_node("approve", lambda s: {"approved": True})
graph.add_edge(START, "analyze")
graph.add_conditional_edges("analyze", route, {"fix": "fix", "approve": "approve"})
graph.add_edge("fix", "analyze")     # 修复后重新分析（循环！）
graph.add_edge("approve", END)
app = graph.compile()

# 运行例子：用 stream 逐节点打印，观察「审查→修复」循环过程
if __name__ == "__main__":
    buggy_code = "def div(a, b):\n    return a / b\n"  # 缺除零保护，应被指出问题
    inputs = {
        "code": buggy_code,
        "issues": [],
        "iteration": 0,
        "max_iterations": 3,
        "approved": False,
    }
    for event in app.stream(inputs):
        # 每个state实际上都是一个CodeReviewState对象，之所以是CodeReviewState对象，是因为每个node返回的都是这个类型的dict
        for node, state in event.items():
            if node == "__end__":
                continue
            print(f"[{node}] state: {state}")


# ======================================================================
# 学习笔记：stream 事件语义（通俗版）
# ======================================================================
# app.stream(...) 每「一步（step）」yield 一个 event；event 是字典 {节点名: 该步状态}。
#   - node  = 字典的 key = 刚跑完的节点名（analyze / fix / approve…）
#   - state = 字典的 value = 该步跑完后的状态
# step 的定义：一次「调度波次」。本波所有就绪节点一起跑，等最慢的收齐才发 event。
#   => 并行节点不会各发一条 event，而是合并进「同一个 event 的多个 key」；
#      本图是串行链，每步只有一个就绪节点，所以「一步一节点」只是巧合。
# stream_mode 决定 event 的结构（关键！）：
#   - "updates"（默认）：event = {节点名: 该节点返回的增量字段}，能拿到节点名；
#     但 state 只含该节点 return 的字段（其他字段没有），想看全要靠自己累计快照。
#   - "values"：event 直接就是「完整 state 本身」，不再用节点名做 key。
#     所以此时不能用 for node, state in event.items() 去拆节点名——会遍历到 state 的字段上去。
# 本文件用 updates 模式 + 快照(snapshot)累计，从而同时拿到「节点名 + 每步完整状态」。
# 条件边（如 route）本身不是一步，不出现在 event 里；图跑完会多一个 {"__end__": ...} 事件，通常跳过。
# ======================================================================
