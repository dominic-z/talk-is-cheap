import os
import sys

from openai import OpenAI


class OODAAgent:
    """基于 OODA 循环的 Agent 框架"""

    def __init__(self):
        api_key = os.getenv("QWEN_API_KEY")
        if not api_key:
            raise RuntimeError(
                "未检测到环境变量 QWEN_API_KEY，请先设置后再运行：\n"
                "  export QWEN_API_KEY=<你的阿里云百炼 API Key>"
            )
        self.context = {}  # 当前情境理解
        self.client = OpenAI(
            api_key=api_key,
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )
        self.model = "qwen3.7-flash"

    def _chat(self, messages: list) -> str:
        """调用大模型并返回文本内容"""
        response = self.client.chat.completions.create(
            model=self.model,
            messages=messages,
        )
        return response.choices[0].message.content

    def observe(self, input_data: str) -> str:
        """观察：收集和整理当前环境信息"""
        prompt = f"""
分析以下输入，提取关键信息：
{input_data}

请识别：
1. 用户的明确需求
2. 隐含的期望
3. 可能的障碍
"""
        observation = self._chat([{"role": "user", "content": prompt}])
        self.context["observation"] = observation
        return observation

    def orient(self, observation: str) -> str:
        """定位：在已知知识框架中理解当前情况"""
        prompt = f"""
基于以下观察，进行情境评估：
{observation}

请分析：
1. 这个任务属于哪类问题？
2. 有哪些可用的方法和工具？
3. 主要的风险和挑战是什么？
"""
        orientation = self._chat([{"role": "user", "content": prompt}])
        self.context["orientation"] = orientation
        return orientation

    def decide(self, orientation: str) -> str:
        """决策：制定行动计划"""
        prompt = f"""
基于情境评估，制定具体行动计划：
{orientation}

请给出：
1. 推荐的行动方案（第一选择）
2. 备选方案
3. 执行步骤（按优先级排序）
"""
        decision = self._chat([{"role": "user", "content": prompt}])
        self.context["decision"] = decision
        return decision

    def act(self, plan: str, user_input: str) -> str:
        """行动：执行计划并生成最终响应"""
        return self._chat([
            {
                "role": "system",
                "content": f"执行计划：\n{plan}\n\n用自然语言给用户一个清晰的回答。"
            },
            {"role": "user", "content": user_input}
        ])

    def process(self, user_input: str) -> str:
        """完整的 OODA 循环"""
        steps = [
            ("1/4 观察 Observe", self.observe, (user_input,)),
            ("2/4 定位 Orient", self.orient, None),
            ("3/4 决策 Decide", self.decide, None),
        ]
        prev = None
        for title, func, args in steps:
            print(f"\n[{title}] 进行中...")
            prev = func(*args) if args else func(prev)
            print(f"\n===== {title} =====")
            print(prev)

        print("\n[4/4 行动 Act] 进行中...")
        result = self.act(prev, user_input)
        print("\n===== 4/4 行动 Act · 最终结果 =====")
        print(result)
        return result




def main():
    """测试主函数：运行一次完整的 OODA 循环"""
    print("=" * 60)
    print("OODA Agent 测试 | 模型：qwen3.7-flash")
    print("=" * 60)

    user_input = "我下周要去杭州出差两天，帮我规划一下行程，重点想吃西湖醋鱼和龙井虾仁。"
    print(f"\n用户输入：{user_input}")

    try:
        agent = OODAAgent()
        agent.process(user_input)
    except RuntimeError as e:
        print(f"\n初始化失败：{e}")
        sys.exit(1)
    except Exception as e:
        print(f"\n执行出错：{type(e).__name__}: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
