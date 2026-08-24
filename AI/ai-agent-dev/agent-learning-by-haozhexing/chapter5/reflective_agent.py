from openai import OpenAI
import os
client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)

class ReflectiveAgent:
    """具备反思能力的 Agent"""

    def __init__(self, max_reflection_rounds: int = 3):
        self.max_rounds = max_reflection_rounds

    def generate(self, task: str, context: str = "") -> str:
        """生成初始答案"""
        response = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[
                {"role": "system", "content": context or "你是一个专业助手"},
                {"role": "user", "content": task}
            ]
        )
        return response.choices[0].message.content

    def reflect(self, task: str, output: str, criteria: list[str]) -> dict:
        """
        反思评估：检查输出是否满足标准

        Returns:
            {"score": 0-10, "passed": bool, "feedback": str, "improvements": []}
        """
        criteria_text = "\n".join([f"- {c}" for c in criteria])

        response = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[
                {
                    "role": "user",
                    "content": f"""请评估以下输出是否满足要求，并给出改进建议。

【原始任务】
{task}

【生成输出】
{output}

【评估标准】
{criteria_text}

请返回JSON格式的评估：
{{
  "score": 0-10的评分,
  "passed": true/false（是否通过所有标准）,
  "feedback": "整体反馈",
  "failed_criteria": ["未满足的标准1", "未满足的标准2"],
  "improvements": ["改进建议1", "改进建议2"]
}}"""
                }
            ],
            response_format={"type": "json_object"}
        )

        import json
        return json.loads(response.choices[0].message.content)

    def revise(self, task: str, output: str, feedback: dict) -> str:
        """基于反思反馈修改输出"""
        improvements = "\n".join([f"- {i}" for i in feedback.get("improvements", [])])
        failed = "\n".join([f"- {c}" for c in feedback.get("failed_criteria", [])])

        response = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[
                {
                    "role": "user",
                    "content": f"""请改进以下输出，解决指出的问题。

【原始任务】
{task}

【当前输出】
{output}

【未满足的标准】
{failed}

【改进建议】
{improvements}

请给出改进后的版本："""
                }
            ]
        )
        return response.choices[0].message.content

    def run_with_reflection(self, task: str, criteria: list[str]) -> dict:
        """
        运行反思循环：生成 → 反思 → 改进 → 循环

        Returns:
            {"final_output": str, "rounds": int, "history": list}
        """
        history = []
        current_output = self.generate(task)

        print(f"\n任务：{task}")

        for round_num in range(self.max_rounds):
            print(f"\n=== 第 {round_num + 1} 轮反思 ===")

            # 评估
            evaluation = self.reflect(task, current_output, criteria)
            score = evaluation.get("score", 0)
            passed = evaluation.get("passed", False)

            print(f"评分：{score}/10 | 通过：{passed}")
            if evaluation.get("feedback"):
                print(f"反馈：{evaluation['feedback'][:100]}")

            history.append({
                "round": round_num + 1,
                "output": current_output,
                "score": score,
                "passed": passed
            })

            # 如果通过，停止
            if passed or score >= 8:
                print(f"✅ 输出质量满足要求，停止反思")
                break

            # 改进
            if round_num < self.max_rounds - 1:
                print("🔄 正在改进...")
                current_output = self.revise(task, current_output, evaluation)

        return {
            "final_output": current_output,
            "rounds": len(history),
            "history": history
        }


# 测试
agent = ReflectiveAgent(max_reflection_rounds=3)

result = agent.run_with_reflection(
    task="写一段Python代码，实现二分查找算法",
    criteria=[
        "代码能正确运行",
        "包含详细注释",
        "有类型注解",
        "有边界情况处理",
        "代码简洁易读"
    ]
)

print(f"\n最终输出（第{result['rounds']}轮）：")
print(result["final_output"])
