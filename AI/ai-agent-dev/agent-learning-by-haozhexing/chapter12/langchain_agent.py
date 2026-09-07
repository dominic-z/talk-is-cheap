import math
import os
from typing import Type

from pydantic import BaseModel, Field

from langchain_core.tools import tool, BaseTool
from langchain_openai import ChatOpenAI
# 注意：本机环境是 LangChain 1.x（1.3.18），旧的 AgentExecutor /
# create_openai_tools_agent 已从 langchain.agents 移除，改用兼容包引入
from langchain_classic.agents import AgentExecutor, create_openai_tools_agent
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.callbacks import BaseCallbackHandler
from print_utils import print_separator

# 方式1：@tool —— 最简单，docstring 即描述
@tool
def calculate(expression: str) -> str:
    """计算数学表达式，如 calculate("sqrt(144) + 2 * 3")。"""
    print("调用了calculate")
    try:
        safe_env = {k: getattr(math, k) for k in dir(math) if not k.startswith('_')}
        return str(eval(expression, {"__builtins__": {}}, safe_env))
    except Exception as e:
        return f"计算错误：{e}"

# 方式2：BaseTool —— 需要自定义校验/状态时
class WeatherInput(BaseModel):
    """get_weather 工具的结构化入参。"""
    city: str = Field(description="城市名称，例如：北京、上海")


class WeatherTool(BaseTool):
    name: str = "get_weather"
    description: str = "获取指定城市的当前天气，需要提供城市名称"
    args_schema: Type[BaseModel] = WeatherInput

    def _run(self, city: str) -> str:
        """同步执行：这里用 mock 数据模拟一次天气查询。"""
        # 真实场景可替换为 requests.get("https://api.xxx/weather", params={"city": city})
        print("调用了weather tool")
        weather_map = {
            "北京": "晴，25℃，空气质量优",
            "上海": "多云，28℃，空气质量良",
            "广州": "小雨，30℃，空气质量优",
            "深圳": "晴转多云，31℃，空气质量良",
        }
        return weather_map.get(city, f"抱歉，没有 {city} 的天气数据。")

    async def _arun(self, city: str) -> str:
        """异步执行：直接复用同步逻辑即可（真实场景可改为 aiohttp 请求）。"""
        return self._run(city)


tools = [calculate, WeatherTool()]
llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
                 temperature=0)
prompt = ChatPromptTemplate.from_messages([
    ("system", "你是智能助手，遇到需要工具的问题先调用工具。"),
    # chat_history：多轮对话的"历史消息"（Human / AI 交替的消息列表）。
    #   由调用方在 invoke 时传入（如 {"chat_history": [...]}），代表"过去的对话"；
    #   名字是社区约定俗成（官方默认模板、RunnableWithMessageHistory 都用它），
    #   理论上可改名，只要和 invoke 传入的 key 一致即可。
    #   optional=True：允许不传，单轮对话场景就不用手动塞空列表。
    MessagesPlaceholder("chat_history", optional=True),
    ("human", "{input}"),
    # agent_scratchpad：Agent 的"推理草稿纸"，代表"本轮的推理过程"，
    #   名字是框架强制的，不能改。它由框架自动填充（无需调用方传入）：
    #   把本轮 intermediate_steps（Agent 想调哪个工具 + 工具返回了什么）
    #   转成消息塞进来，LLM 才能看到自己上一步干了什么，从而决定下一步动作。
    #   create_openai_tools_agent 会校验 prompt 必须包含它，缺了直接报错。
    MessagesPlaceholder("agent_scratchpad"),
])
agent = create_openai_tools_agent(llm, tools, prompt)
agent_executor = AgentExecutor(agent=agent, tools=tools,
                               max_iterations=5, return_intermediate_steps=True)


# ─────────────────── 执行：让 Agent 跑起来 ───────────────────
print_separator("执行 1：数学计算（应调用 calculate 工具）")
q1 = "帮我算一下 144 的平方根加上 2 乘 3 等于多少？"
ans1 = agent_executor.invoke({"input": q1})
print(f"问题：{q1}")
print(f"回答：{ans1['output']}")

class PromptCapture(BaseCallbackHandler):
    """记录每一轮真正发给 LLM 的完整消息列表（含两个 Placeholder 展开后的内容）。"""
    def __init__(self):
        self.rounds = []

    def on_chat_model_start(self, serialized, messages, **kwargs):
        self.rounds.append(messages[0])  # messages 是 list[list[BaseMessage]]


print_separator("执行 2：查询天气（应调用 get_weather 工具）")
q2 = "北京今天天气怎么样？"
capture = PromptCapture()
ans2 = agent_executor.invoke({"input": q2}, config={"callbacks": [capture]})
print(f"问题：{q2}")
print(f"回答：{ans2['output']}")

print_separator("查看中间步骤（Agent 的思考 / 调用轨迹）")
for i, step in enumerate(ans2["intermediate_steps"], 1):
    action, result = step
    # action 是 ToolAgentAction（继承自 AgentAction），字段名是 tool / tool_input，
    # 不是 OpenAI tool_call 的 name / args
    print(f"第 {i} 步：调用工具 {action.tool}，入参 {action.tool_input}，返回 {result}")

print_separator("两个 Placeholder 里到底装了什么（真实发给 LLM 的消息）")
for round_no, msgs in enumerate(capture.rounds, 1):
    print(f"\n—— 第 {round_no} 轮发给 LLM，共 {len(msgs)} 条消息 ——")
    for m in msgs:
        extra = f"\n      tool_calls={m.tool_calls}" if getattr(m, "tool_calls", None) else ""
        print(f"  [{type(m).__name__}] {m.content!r}{extra}")
print("\n对照说明：")
print("  · SystemMessage / HumanMessage   → system 提示词 + {input}")
print("  · chat_history                   → 本次没传（optional=True），所以中间没有历史消息")
print("  · 第 2 轮起新增的 AIMessage(tool_calls) + ToolMessage")
print("    → 就是 agent_scratchpad：上一步的工具调用 + 工具返回结果")
