# llm.py
"""统一的 LLM 客户端（通义千问 / DashScope OpenAI 兼容模式）"""

import os

from openai import OpenAI

MODEL = "qwen3.7-flash"


def get_client() -> OpenAI:
    """创建 OpenAI 兼容客户端

    使用前先设置环境变量：
        export QWEN_API_KEY=sk-xxxxxx
    """
    api_key = os.getenv("QWEN_API_KEY")
    if not api_key:
        raise RuntimeError(
            "未检测到环境变量 QWEN_API_KEY，请先执行：export QWEN_API_KEY=sk-xxxxxx"
        )
    return OpenAI(
        api_key=api_key,
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    )
