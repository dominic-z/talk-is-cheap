from dataclasses import dataclass
from enum import Enum

from openai import OpenAI, AsyncOpenAI
import os

QWEN_3_7_FLASH = "qwen3.7-flash"


class ModelProvider(Enum):
    OPENAI = "openai"
    OLLAMA = "ollama"
    QWEN = "qwen"
    DEEPSEEK = "deepseek"
    ZHIPU = "zhipu"


@dataclass
class LLMConfig:
    provider: ModelProvider
    model: str
    temperature: float = 0.7
    max_tokens: int = 2000
    timeout: int = 30


class UnifiedLLMClient:
    """统一的 LLM 调用客户端，支持多个提供商"""

    def __init__(self, config: LLMConfig):
        self.config = config
        self.client = self._create_client()

    def _create_client(self) -> OpenAI:
        """根据配置创建对应的客户端"""
        configs = {
            # ModelProvider.OPENAI: {
            #   "api_key": os.getenv("OPENAI_API_KEY"),
            #   "base_url": None
            # },
            ModelProvider.QWEN: {
                "api_key": os.getenv("QWEN_API_KEY"),
                "base_url": "https://dashscope.aliyuncs.com/compatible-mode/v1"
            },
        }
        provider_config = configs[self.config.provider]
        return OpenAI(**{k: v for k, v in provider_config.items() if v is not None})

    def chat(self, messages: list, stream: bool = False, model: str = None, temperature: float = None,
             max_tokens: int = None,**kwargs):
        """统一的对话接口"""
        kwargs = {
            "model": self.config.model if model is None else model,
            "messages": messages,
            "temperature": self.config.temperature if temperature is None else temperature,
            "max_tokens": self.config.max_tokens if max_tokens is None else max_tokens,
            "stream": stream,
            **kwargs
        }
        return self.client.chat.completions.create(**kwargs)

    def simple_chat(self, message: str, system: str = None) -> str:
        """简单的单轮对话"""
        messages = []
        if system:
            messages.append({"role": "system", "content": system})
        messages.append({"role": "user", "content": message})
        response = self.chat(messages)
        return response.choices[0].message.content


default_qwen_client = UnifiedLLMClient(LLMConfig(provider=ModelProvider.QWEN, model=QWEN_3_7_FLASH, temperature=0.7))

if __name__ == "__main__":
    unified_llm_client = UnifiedLLMClient(LLMConfig(provider=ModelProvider.QWEN, model=QWEN_3_7_FLASH, temperature=0.7))

    # 无论哪个提供商，接口完全一致
    result = unified_llm_client.simple_chat("Python 和 JavaScript 的主要区别是什么？")
    print(result)
