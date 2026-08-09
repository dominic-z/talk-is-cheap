from dataclasses import dataclass
from enum import Enum

from openai import OpenAI, AsyncOpenAI
import os

client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)

async_client = AsyncOpenAI(api_key=os.getenv("QWEN_API_KEY"),
                           base_url="https://dashscope.aliyuncs.com/compatible-mode/v1")


QWEN_3_7_FLASH = "qwen3.7-flash"


