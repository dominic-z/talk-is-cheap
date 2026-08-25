import os
from openai import  OpenAI
client = OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )

class AdaptiveReasoningAgent:
    """按难度分级：简单直接答，中等用 CoT，复杂多路径搜索。"""

    THRESHOLDS = {
        "simple": {"max_tokens": 500, "strategy": "direct"},
        "medium": {"max_tokens": 2000, "strategy": "cot"},
        "hard":   {"max_tokens": 8000, "strategy": "search"},
    }

    def run(self, question: str) -> dict:
        # 1. 快速评估难度
        difficulty = self._assess_difficulty(question)
        cfg = self.THRESHOLDS[difficulty]
        # 2. 按难度选策略
        if cfg["strategy"] == "direct":
            answer = self._direct(question)
        elif cfg["strategy"] == "cot":
            answer = self._cot(question, cfg["max_tokens"])
        else:    # search
            answer = self._multi_path_search(question, cfg["max_tokens"])
        return {"question": question, "difficulty": difficulty, "answer": answer}

    def _assess_difficulty(self, question: str) -> str:
        resp = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[{"role": "user", "content": f"""评估问题难度。
问题：{question}
标准：simple=事实问答/简单计算；medium=多步推理/综合分析；hard=创新思维/复杂证明
只回答 simple/medium/hard。"""}],
            max_tokens=10
        )
        result = resp.choices[0].message.content.strip().lower()
        return result if result in self.THRESHOLDS else "medium"

    def _direct(self, q: str) -> str:
        return client.chat.completions.create(
            model="qwen3.7-flash", messages=[{"role": "user", "content": q}], max_tokens=500
        ).choices[0].message.content

    def _cot(self, q: str, max_tokens: int) -> str:
        return client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[{"role": "user", "content": f"{q}\n请一步步思考。"}],
            max_tokens=max_tokens
        ).choices[0].message.content

    def _multi_path_search(self, q: str, max_tokens: int) -> str:
        # 生成 3 种解法 → LLM 选最优
        paths = [
            client.chat.completions.create(
                model="qwen3.7-flash",
                messages=[{"role": "user", "content": f"{q}\n用方法 {i+1} 解答"}],
                max_tokens=max_tokens // 3
            ).choices[0].message.content
            for i in range(3)
        ]
        # 综合评判：让 LLM 对比多条路径，选出最正确/最完整的答案
        candidates = "\n\n".join(
            f"【解法 {i+1}】\n{p}" for i, p in enumerate(paths)
        )
        best = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[{
                "role": "user",
                "content": f"""原问题：{q}

以下是 3 条独立求解路径：
{candidates}

请综合评判以上各条路径的正确性与完整性，选出最优解法，
并输出最终答案。只输出最终答案本身，不要提及解法编号或评判过程。"""
            }],
            max_tokens=max_tokens // 3
        ).choices[0].message.content
        return best


# ─────────────────────────── 测试 Demo ───────────────────────────

# 难度标签与策略的可视化映射（仅用于展示）
DIFFICULTY_STYLE = {
    "simple": ("🟢 简单", "直接作答 (direct)"),
    "medium": ("🟡 中等", "思维链 (CoT)"),
    "hard":   ("🔴 困难", "多路径搜索 (multi-path search)"),
}


def print_question(idx: int, question: str) -> None:
    print(f"\n{'─' * 60}")
    print(f"❓ 问题 {idx}：{question}")
    print(f"{'─' * 60}")


def print_result(result: dict) -> None:
    difficulty = result["difficulty"]
    badge, strategy = DIFFICULTY_STYLE.get(difficulty, (difficulty, "未知"))
    print(f"📊 难度评估：{badge}")
    print(f"🧭 推理策略：{strategy}")
    print(f"\n💡 回答：")
    print(result["answer"])


def test_adaptive_reasoning() -> None:
    """覆盖三个难度档位的端到端演示。"""
    questions = [
        "中国的首都是哪里？",                       # simple → direct
        "一个水池有进水管和出水管，单独开进水管 6 小时注满，"
        "单独开排水管 8 小时排空。两管同时打开，几小时注满？",  # medium → cot
        "请设计一个分布式限流方案，要求支持集群部署下的精确限流，"
        "并分析各方案的优缺点与适用场景。",          # hard → search
    ]

    print("═" * 60)
    print("🤖 自适应推理 Agent 演示（按难度自动选择推理策略）")
    print("═" * 60)

    agent = AdaptiveReasoningAgent()
    for i, q in enumerate(questions, start=1):
        print_question(i, q)
        result = agent.run(q)
        print_result(result)

    print(f"\n{'═' * 60}")
    print("✅ 演示完成")
    print("═" * 60)


if __name__ == "__main__":
    test_adaptive_reasoning()
