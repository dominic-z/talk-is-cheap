from openai import OpenAI
import numpy as np
import os

client = OpenAI(
  api_key=os.getenv("QWEN_API_KEY"),
  base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)

# https://platform.qianwenai.com/docs/api-reference/text-embedding/openai-embedding
def get_embedding(text: str, model: str = "text-embedding-v4") -> list[float]:
    """获取文本的向量嵌入（1536 维）"""
    response = client.embeddings.create(input=text, model=model)
    return response.data[0].embedding

def cosine_sim(v1, v2) -> float:
    return float(np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2)))

# 验证语义相似性
texts = [
    "Python 是一种编程语言",        # 原始句
    "Python 是用于编程的语言",      # 语义相似
    "今天天气很好",                 # 语义不相关
]
embeddings = [get_embedding(t) for t in texts]
print(f"相似度（语义相似）：{cosine_sim(embeddings[0], embeddings[1]):.4f}")  # > 0.9
print(f"相似度（语义不同）：{cosine_sim(embeddings[0], embeddings[2]):.4f}")  # < 0.5
