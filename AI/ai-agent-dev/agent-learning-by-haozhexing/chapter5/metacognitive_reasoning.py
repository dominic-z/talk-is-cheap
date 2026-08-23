import os
import sys

from openai import OpenAI

def metacognitive_reasoning(problem: str) -> dict:
    """元认知推理：Agent 能评估自己的置信度和局限性"""

    api_key = os.getenv("QWEN_API_KEY")
    client = OpenAI(
        api_key=api_key,
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    )
    model = "qwen3.7-flash"
    response = client.chat.completions.create(
        model=model,
        messages=[
            {
                "role": "system",
                "content": """回答时，始终进行元认知评估：
1. 我对这个问题的知识有多可靠？（置信度 0-10）
2. 哪些方面我可能存在盲区？
3. 是否需要额外工具或信息？
4. 我的回答基于哪些假设？"""
            },
            {"role": "user", "content": problem}
        ]
    )

    return {
        "answer": response.choices[0].message.content,
        "self_assessed_by_llm": True
    }


# 测试元认知
result = metacognitive_reasoning("量子计算机什么时候能超越传统计算机？")
print(result["answer"])
