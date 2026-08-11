# ── 工具1：搜索（基于 DuckDuckGo 免费 API，无需 API Key）────────
import requests


def search_web(query: str, num_results: int = 5) -> str:
    """调用 DuckDuckGo Instant Answer API，返回文本摘要。"""
    # 这个搜索引擎很差，比如搜地球月亮距离都搜不到，但是搜lunar distance可以搜到
    try:
        url = "https://api.duckduckgo.com/"
        params = {"q": query, "format": "json", "no_html": 1}
        # DuckDuckGo 在中国大陆被屏蔽，需通过代理访问
        proxies = {"http": "http://192.168.126.1:10808", "https": "http://192.168.126.1:10808"}
        data = requests.get(url, params=params, timeout=30, proxies=proxies).json()

        results = []
        if data.get("AbstractText"):
            results.append(f"即时答案：{data['AbstractText']}")
        for topic in data.get("RelatedTopics", [])[:num_results]:
            if isinstance(topic, dict) and topic.get("Text"):
                results.append(f"• {topic['Text'][:200]}")

        return "\n".join(results) if results \
            else f"未找到结果，建议换更具体的关键词。"
    except Exception as e:
        return f"搜索失败：{e}。请检查网络连接。"


# ── 工具2：精确计算（用受限 eval 兼顾安全与功能）─────────────
import math


def calculate(expression: str) -> str:
    """用受限的 eval 环境执行数学表达式，避免任意代码执行。"""
    safe_dict = {
        "__builtins__": {},  # 关键：禁用所有内置函数
        "sqrt": math.sqrt, "log": math.log, "sin": math.sin, "cos": math.cos,
        "pi": math.pi, "e": math.e, "abs": abs, "round": round,
        # ... 其他数学函数按需添加
    }
    try:
        result = eval(expression, safe_dict)
        return f"{expression} = {result}"
    except ZeroDivisionError:
        return "计算错误：除以零"
    except Exception as e:
        return f"计算错误：{e}。请确认表达式格式正确。"


# ── 工具3：单位换算 ──────────────────────────────────────────
def unit_converter(value: float, from_unit: str, to_unit: str) -> str:
    """支持长度/重量/温度/面积四类单位的双向换算。"""
    conversions = {
        "m": 1.0, "km": 1000.0, "mile": 1609.344,  # 长度
        "kg": 1.0, "pound": 0.453592,  # 重量
        # ... 完整映射在代码仓库
    }
    if from_unit in ["celsius", "fahrenheit", "kelvin"]:
        # 温度需要特殊处理（非线性换算）
        return _convert_temperature(value, from_unit, to_unit)
    if from_unit not in conversions or to_unit not in conversions:
        return f"不支持的单位：{from_unit} 或 {to_unit}"
    return f"{value} {from_unit} = {value * conversions[from_unit] / conversions[to_unit]:.6g} {to_unit}"


def _convert_temperature(value: float, from_unit: str, to_unit: str) -> str:
    """温度单位换算，支持 celsius / fahrenheit / kelvin 三者之间的双向转换。"""
    if from_unit == to_unit:
        return f"{value} {from_unit} = {value} {to_unit}"

    # 先统一转换为摄氏度
    if from_unit == "celsius":
        celsius = value
    elif from_unit == "fahrenheit":
        celsius = (value - 32) * 5 / 9
    elif from_unit == "kelvin":
        celsius = value - 273.15
    else:
        return f"不支持的温度单位：{from_unit}"

    # 再从摄氏度转换为目标单位
    if to_unit == "celsius":
        result = celsius
    elif to_unit == "fahrenheit":
        result = celsius * 9 / 5 + 32
    elif to_unit == "kelvin":
        result = celsius + 273.15
    else:
        return f"不支持的温度单位：{to_unit}"

    return f"{value} {from_unit} = {result:.6g} {to_unit}"


# ── 主函数：测试以上三个工具 ─────────────────────────────────
def test_tool():
    print("=" * 50)
    print("【测试工具1：search_web】")
    print(search_web("lunar distance"))

    print("=" * 50)
    print("【测试工具2：calculate】")
    print(calculate("sqrt(16) + pi"))
    print(calculate("1 / 0"))          # 除以零
    print(calculate("log(e)"))         # 正常计算

    print("=" * 50)
    print("【测试工具3：unit_converter】")
    print(unit_converter(1, "km", "mile"))
    print(unit_converter(100, "celsius", "fahrenheit"))
    print(unit_converter(32, "fahrenheit", "celsius"))
    print(unit_converter(0, "celsius", "kelvin"))
    print(unit_converter(1, "kg", "pound"))


TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "search_web",
            "description": """在互联网上搜索实时信息。

适合用于：
- 查询最新新闻、事件、价格等实时数据
- 获取人物、地点、事件的背景信息
- 查找技术文档和教程

不适合用于：
- 数学计算（使用 calculate 工具）
- 单位换算（使用 unit_converter 工具）
- 你已经知道答案的问题

注意：
这个搜索引擎是免费的，搜索结果较差，复杂搜索可能无法正确获取结果，因此搜索的时候应使用简单的英文单词，单词数目不超过2个，例如查询地月距离，应当仅仅使用lunar distance作为搜索词。
"""
            # 加上最后这段话还会发现模型可能就不调用这个了。。
            ,
            "parameters": {
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string",
                        "description": "搜索关键词，建议简洁精准。"
                    },
                    "num_results": {
                        "type": "integer",
                        "description": "返回结果数量，默认5，最大10",
                        "default": 5
                    }
                },
                "required": ["query"],
                "additionalProperties": False   # 关键：禁止额外字段
            }
        }
    },
    {
        "type": "function",
        "function": {
            "name": "calculate",
            "description": """精确计算数学表达式。

支持：基本运算（+,-,*,/,**）、数学函数（sqrt/sin/cos/log等）、常量（pi/e）

不适合：
- 需要查询的事实（如"光速是多少"）→ 用 search_web
- 单位换算 → 用 unit_converter
- 大段文本处理""",
            "parameters": {
                "type": "object",
                "properties": {
                    "expression": {
                        "type": "string",
                        "description": "数学表达式，使用 Python 语法。乘方用 **，不用 ^"
                    }
                },
                "required": ["expression"],
                "additionalProperties": False
            }
        }
    },
    {
        "type": "function",
        "function": {
            "name": "unit_converter",
            "description": """单位换算，支持长度、重量、温度、面积等常见单位转换。

适合：m/km/mile、kg/pound、°C/°F 等
不适合：货币汇率、时区转换""",
            "parameters": {
                "type": "object",
                "properties": {
                    "value":    {"type": "number", "description": "要换算的数值"},
                    "from_unit": {"type": "string", "description": "原始单位，如 m, km, celsius"},
                    "to_unit":   {"type": "string", "description": "目标单位，如 mile, pound, fahrenheit"}
                },
                "required": ["value", "from_unit", "to_unit"],
                "additionalProperties": False
            }
        }
    }
]

TOOL_FUNCTIONS = {
    "search_web":search_web,
    "unit_converter":unit_converter,
    "calculate":calculate
}

from openai import OpenAI
import  os,json
client = OpenAI(
  api_key=os.getenv("QWEN_API_KEY"),
  base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)
class SearchCalcAgent:
    """搜索 + 计算 Agent"""

    SYSTEM_PROMPT = """你是一个能够搜索和计算的智能助手。
你有三个工具：search_web（搜索）、calculate（计算）、unit_converter（换算）。

使用策略：
- 需要实时数据 → 先 search_web
- 需要精确计算 → 用 calculate，不要手动算
- 复杂问题可组合多个工具
- 给出答案时说明信息来源
- 简洁准确，重点突出"""

    def __init__(self):
        self.messages = [{"role": "system", "content": self.SYSTEM_PROMPT}]

    def chat(self, user_message: str) -> str:
        self.messages.append({"role": "user", "content": user_message})
        MAX_STEPS = 8   # 安全护栏：防无限循环

        for step in range(MAX_STEPS):
            response = client.chat.completions.create(
                model="qwen3.7-flash",
                messages=self.messages,
                tools=TOOLS,
                tool_choice="auto",
                parallel_tool_calls=True
            )
            message = response.choices[0].message
            self.messages.append(message)

            # 模型直接给出最终答案 → 结束
            if response.choices[0].finish_reason == "stop":
                return message.content

            # 模型请求工具调用 → 执行并把结果回传
            if response.choices[0].finish_reason == "tool_calls":
                for tool_call in message.tool_calls:
                    func_name = tool_call.function.name
                    func_args = json.loads(tool_call.function.arguments)
                    result = TOOL_FUNCTIONS[func_name](**func_args)

                    print(f"需要调用tool: {tool_call}，参数{func_args}，调用结果{result}")

                    self.messages.append({
                        "role": "tool",
                        "tool_call_id": tool_call.id,
                        "content": str(result)
                    })

        return "已达到最大步骤数限制，请简化问题重试。"



if __name__ == "__main__":
    # test_tool()

    agent = SearchCalcAgent()
    print(agent.chat("地球到月球多远？光速飞过去要几秒？"))
