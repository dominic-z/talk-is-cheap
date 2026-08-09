import os
from openai import OpenAI

# 环境变量写在~/.profile里面
# https://www.doubao.com/thread/xMV3ZBXbughXphfPu
print(os.getenv("QWEN_API_KEY"))

client = OpenAI(
  api_key=os.getenv("QWEN_API_KEY"),
  base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)

completion = client.chat.completions.create(
  model="qwen3.7-plus",
  messages=[
    {"role": "user", "content": "Hello! Tell me a fun fact about AI."}
  ]
)

print(completion.choices[0].message.content)