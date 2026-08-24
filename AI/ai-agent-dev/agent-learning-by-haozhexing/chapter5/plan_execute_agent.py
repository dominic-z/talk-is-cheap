from openai import OpenAI
import json
import os
import re

# plan_execute_agent.py 优化这个代码，
# 这是一个plan and execute的agent的demo，
# 有俩问题：
# 1. tool的传递是放在prompt里告知llm的，在openai的api里，应该独立传递到function里，
# 并且llm的调用应该通过message.tool_calls来实际执行，而不应该通过choices[0].message.content来调用。
# 并且应该按照message的规范组织下一轮提问的message，而不是塞进一个message里，对此进行优化。
# 2. 我写了一个mock_search，帮我扩充一下。
client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)

MODEL = "qwen3.7-flash"


class PlanAndExecuteAgent:
    """
    规划-执行模式 Agent
    先制定完整计划，再逐步执行。

    工具通过 OpenAI Function Calling 规范传递（tools 参数），
    LLM 通过 message.tool_calls 发起工具调用，工具结果以
    role="tool" 的消息回填，形成标准的多轮对话。
    """

    def __init__(self, available_tools: dict, tool_schemas: list[dict]):
        self.tools = available_tools
        # OpenAI Function Calling 格式的工具定义
        self.tool_schemas = tool_schemas

    # ------------------------------------------------------------------
    # 规划阶段
    # ------------------------------------------------------------------
    def plan(self, goal: str) -> dict:
        """
        将目标分解为有序的子任务列表

        Returns:
            {"goal": "...", "steps": [{"step": 1, "task": "...", ...}]}
        """
        response = client.chat.completions.create(
            model=MODEL,
            messages=[
                {
                    "role": "system",
                    "content": f"""你是一个任务规划专家。将用户目标分解为可执行的子任务。

可用工具：{list(self.tools.keys())}

返回 JSON 格式的执行计划：
{{
  "goal": "总目标",
  "steps": [
    {{
      "step": 1,
      "task": "任务描述",
      "tool": "工具名（可选，不需要工具时省略该字段）",
      "expected_output": "预期产出",
      "depends_on": []
    }}
  ]
}}""",
                },
                {"role": "user", "content": f"请为以下目标制定执行计划：{goal}"},
            ],
            response_format={"type": "json_object"},
        )

        return json.loads(response.choices[0].message.content)

    # ------------------------------------------------------------------
    # 执行阶段
    # ------------------------------------------------------------------
    def execute_step(self, step: dict, context: dict) -> str:
        """
        执行单个步骤。

        通过 Function Calling 让 LLM 自主决定是否调用工具、调用哪个工具、
        传什么参数；工具结果按 message 规范回填，支持多轮连续调用。
        """
        task = step["task"]

        print(f"\n[步骤 {step['step']}] {task}")

        messages = [
            {
                "role": "system",
                "content": (
                    "你正在按计划执行一个子任务，可以使用工具获取信息。"
                    "已有上下文（之前步骤的结果）：\n"
                    f"{json.dumps(context, ensure_ascii=False, indent=2)}"
                ),
            },
            {
                "role": "user",
                "content": (
                    f"当前子任务：{task}\n"
                    f"预期产出：{step.get('expected_output', '无')}\n"
                    "请完成该子任务，直接给出本步骤的结论。"
                ),
            },
        ]

        # 工具调用循环（防止极端情况死循环，最多 5 轮）
        for _ in range(5):
            response = client.chat.completions.create(
                model=MODEL,
                messages=messages,
                tools=self.tool_schemas,
                tool_choice="auto",
            )
            msg = response.choices[0].message

            # LLM 决定不再调用工具 → 本轮步骤得到最终文本结果
            if not msg.tool_calls:
                result = msg.content or ""
                break

            # 按规范把 assistant 的 tool_calls 消息加入对话历史
            messages.append(
                {
                    "role": "assistant",
                    "content": msg.content,
                    "tool_calls": [
                        {
                            "id": tc.id,
                            "type": "function",
                            "function": {
                                "name": tc.function.name,
                                "arguments": tc.function.arguments,
                            },
                        }
                        for tc in msg.tool_calls
                    ],
                }
            )

            # 逐个执行工具调用，并以 role="tool" 消息回填结果
            for tc in msg.tool_calls:
                name = tc.function.name
                try:
                    args = json.loads(tc.function.arguments or "{}")
                except json.JSONDecodeError:
                    args = {}

                func = self.tools.get(name)
                if func is None:
                    tool_result = f"错误：不存在名为 {name} 的工具"
                else:
                    try:
                        tool_result = str(func(**args))
                    except Exception as e:
                        tool_result = f"工具 {name} 调用失败：{e}"

                print(f"  🔧 调用工具 {name}({tc.function.arguments})")
                print(f"     ↳ {tool_result[:200]}")

                messages.append(
                    {
                        "role": "tool",
                        "tool_call_id": tc.id,
                        "content": tool_result,
                    }
                )
        else:
            result = "（工具调用轮数达到上限，未能得出本步骤结论）"

        print(f"  结果：{result[:200]}")
        return result

    def execute(self, goal: str) -> str:
        """执行完整目标"""
        # 1. 制定计划
        plan = self.plan(goal)
        print(f"\n📋 执行计划：{plan.get('goal', goal)}")
        for step in plan.get("steps", []):
            print(f"  步骤{step['step']}: {step['task']}")

        # 2. 逐步执行
        context = {}  # 各步骤结果的共享上下文

        for step in plan.get("steps", []):
            result = self.execute_step(step, context)
            context[f"step_{step['step']}_result"] = result

        # 3. 汇总结果
        summary_response = client.chat.completions.create(
            model=MODEL,
            messages=[
                {
                    "role": "user",
                    "content": f"""
目标：{goal}

各步骤执行结果：
{json.dumps(context, ensure_ascii=False, indent=2)}

请综合以上结果，给出最终回答：""",
                }
            ],
        )

        final_answer = summary_response.choices[0].message.content
        print(f"\n✅ 最终结果：\n{final_answer}")
        return final_answer


# ----------------------------------------------------------------------
# 测试工具
# ----------------------------------------------------------------------
def mock_search(query: str, top_k: int = 3) -> str:
    """
    模拟搜索引擎（实际项目用真实搜索 API）。

    特性：
    - 多主题知识库（每条知识带关键词与来源）
    - 关键词命中计分排序，支持一条知识匹配多个关键词
    - 返回 top_k 条结果，未命中时给出提示
    """
    knowledge = [
        {
            "keywords": ["python", "创造", "作者", "创建", "历史", "guido"],
            "content": "Python 由 Guido van Rossum 创建，1991 年首次发布，设计哲学强调代码可读性。",
            "source": "Python 官方历史文档",
        },
        {
            "keywords": ["python", "ai", "人工智能", "机器学习", "应用"],
            "content": "Python 是 AI 开发的首选语言，主要应用包括：机器学习、深度学习、自然语言处理、计算机视觉、推荐系统与数据分析。",
            "source": "AI 行业调研报告",
        },
        {
            "keywords": ["机器学习", "框架", "库", "tensorflow", "pytorch", "sklearn"],
            "content": "主流机器学习框架：TensorFlow、PyTorch、scikit-learn；其中 PyTorch 在学术界占主导，TensorFlow 在工业界广泛部署。",
            "source": "开源生态统计报告",
        },
        {
            "keywords": ["深度学习", "神经网络", "gpu", "训练"],
            "content": "深度学习依赖大规模数据与 GPU 并行计算，卷积神经网络（CNN）、循环神经网络（RNN）与 Transformer 是核心架构。",
            "source": "深度学习综述论文",
        },
        {
            "keywords": ["自然语言处理", "nlp", "大模型", "llm", "transformer", "gpt"],
            "content": "自然语言处理（NLP）已从规则方法演进到大语言模型时代，Transformer 架构是现代 LLM 的基础。",
            "source": "NLP 技术演进白皮书",
        },
        {
            "keywords": ["计算机视觉", "图像", "识别", "opencv", "自动驾驶"],
            "content": "计算机视觉广泛用于图像识别、目标检测、自动驾驶感知与医疗影像分析，OpenCV 是最常用的基础库。",
            "source": "计算机视觉应用报告",
        },
        {
            "keywords": ["数据分析", "数据科学", "pandas", "numpy", "可视化"],
            "content": "Python 数据科学生态以 NumPy、Pandas 为核心，配合 Matplotlib/Seaborn 实现数据清洗、分析与可视化。",
            "source": "数据科学工具链指南",
        },
        {
            "keywords": ["推荐系统", "推荐", "协同过滤", "电商"],
            "content": "推荐系统常用算法包括协同过滤、内容推荐与深度推荐模型，广泛应用于电商、短视频与信息流场景。",
            "source": "推荐系统实践手册",
        },
        {
            "keywords": ["java", "jvm", "语言"],
            "content": "Java 由 Sun Microsystems 于 1995 年发布，以『一次编写，到处运行』著称，广泛用于企业级后端开发。",
            "source": "Java 语言简史",
        },
        {
            "keywords": ["javascript", "前端", "web", "node"],
            "content": "JavaScript 是 Web 前端的核心语言，Node.js 使其可以运行在服务端。",
            "source": "Web 技术概览",
        },
    ]

    # 归一化查询词：转小写并拆分
    normalized = query.lower()
    query_terms = [t for t in re.split(r"[\s,，。？?！!、]+", normalized) if t]

    # 对每条知识计分：关键词命中（子串双向匹配）
    scored = []
    for item in knowledge:
        score = 0
        for kw in item["keywords"]:
            if kw in normalized:
                score += 3
            else:
                for term in query_terms:
                    if len(term) >= 2 and (term in kw or kw in term):
                        score += 1
        if score > 0:
            scored.append((score, item))

    if not scored:
        return f"搜索'{query}'：未找到相关信息"

    scored.sort(key=lambda x: x[0], reverse=True)
    top_results = scored[:top_k]

    lines = [f"搜索'{query}'，共找到 {len(scored)} 条相关结果，返回前 {len(top_results)} 条："]
    for i, (_, item) in enumerate(top_results, 1):
        lines.append(f"{i}. {item['content']}（来源：{item['source']}）")
    return "\n".join(lines)


def mock_calculate(expression: str) -> str:
    import math

    try:
        result = eval(expression, {"__builtins__": {}, "math": math})
        return f"{expression} = {result}"
    except Exception:
        return "计算失败"


def mock_write_file(filename: str, content: str) -> str:
    print(content)
    return f"文件 {filename} 已创建"


# 工具的 OpenAI Function Calling 定义（独立于 prompt 传递给模型）
TOOL_SCHEMAS = [
    {
        "type": "function",
        "function": {
            "name": "search",
            "description": "在知识库中搜索与查询相关的信息，支持中英文关键词",
            "parameters": {
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string",
                        "description": "搜索关键词或问题",
                    },
                    "top_k": {
                        "type": "integer",
                        "description": "返回结果条数，默认 3",
                    },
                },
                "required": ["query"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "calculate",
            "description": "计算数学表达式，如 '2 ** 10 + 5'",
            "parameters": {
                "type": "object",
                "properties": {
                    "expression": {
                        "type": "string",
                        "description": "合法的 Python 数学表达式",
                    }
                },
                "required": ["expression"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "write_file",
            "description": "将内容写入指定文件（演示环境仅打印内容）",
            "parameters": {
                "type": "object",
                "properties": {
                    "filename": {
                        "type": "string",
                        "description": "文件名",
                    },
                    "content": {
                        "type": "string",
                        "description": "文件内容",
                    },
                },
                "required": ["filename", "content"],
            },
        },
    },
]

# 执行示例
if __name__ == "__main__":
    agent = PlanAndExecuteAgent(
        available_tools={
            "search": mock_search,
            "calculate": mock_calculate,
            "write_file": mock_write_file,
        },
        tool_schemas=TOOL_SCHEMAS,
    )

    result = agent.execute(
        "研究Python在AI开发中的主要应用场景，并写一份300字的总结报告"
    )
