from openai import OpenAI
import os

client = OpenAI(
  api_key=os.getenv("QWEN_API_KEY"),
  base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)
# 最简单的调用
def simple_chat(message: str) -> str:
    """最基本的单轮对话"""
    response = client.chat.completions.create(
        model="qwen3.7-flash",
        messages=[
            {"role": "user", "content": message}
        ]
    )
    return response.choices[0].message.content

# 测试
answer = simple_chat("用一句话解释什么是 Python 的 GIL？")
print(answer)
