from __future__ import annotations
import json
import time
import logging
from enum import Enum
from typing import TypeVar, Generic, Type, Optional, Any
from pydantic import BaseModel, ValidationError
import os

from chapter4.reserve_conversation_history import Message

T = TypeVar("T", bound=BaseModel)
logger = logging.getLogger(__name__)


class OutputBackend(Enum):
    """支持的后端"""
    OPENAI = "openai"  # OpenAI API（原生 Structured Outputs）
    ANTHROPIC = "anthropic"  # Claude API（工具调用）
    VLLM = "vllm"  # 本地 vLLM（约束解码）
    FALLBACK = "fallback"  # 软约束 + 正则提取（兜底）


class StructuredOutputHarness(Generic[T]):
    """
    结构化输出 Harness

    设计原则（Harness Engineering 六大支柱中的"架构约束"）：
    - 用工程机制保证，而非依赖模型自律
    - 从硬约束（API）到软约束（重试），层层防守
    - 所有错误都有明确处理，不静默失败

    使用示例：
        harness = StructuredOutputHarness(MySchema, backend=OutputBackend.OPENAI)
        result = harness.extract("从这段文字中提取信息：...")
    """

    def __init__(
            self,
            schema_class: Type[T],
            backend: OutputBackend = OutputBackend.OPENAI,
            model: str = "qwen3.7-flash",
            max_retries: int = 3,
            retry_delay: float = 0.5,
    ):
        self.schema_class = schema_class
        self.backend = backend
        self.model = model
        self.max_retries = max_retries
        self.retry_delay = retry_delay
        self._client = self._init_client()

    def _init_client(self):
        if self.backend == OutputBackend.OPENAI:
            from openai import OpenAI
            return OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )
        elif self.backend == OutputBackend.ANTHROPIC:
            import anthropic
            return anthropic.Anthropic()
        return None  # vLLM 和 fallback 不需要 client

    def extract(self, prompt: str, system: str = "") -> T:
        """
        核心提取方法：层层防守

        执行顺序：
        1. 尝试硬约束提取（API 原生 / 约束解码）
        2. 若失败，带错误上下文重试（最多 max_retries 次）
        3. 每次结果都经过 Pydantic 完整验证（含业务逻辑）
        4. 全部重试失败后抛出明确错误
        """
        last_error: Optional[str] = None
        last_raw: Optional[str] = None

        for attempt in range(self.max_retries):
            try:
                # ── 构建 Prompt（重试时携带错误上下文）──
                full_prompt = self._build_prompt(prompt, last_error, last_raw, attempt)

                # ── 执行提取 ──
                raw_result = self._call_backend(full_prompt, system)
                last_raw = str(raw_result)

                # ── Pydantic 完整验证 ──
                if isinstance(raw_result, self.schema_class):
                    validated = raw_result  # 原生结构化输出，已经是 Pydantic 对象
                else:
                    validated = self.schema_class.model_validate(raw_result)

                logger.info(
                    "结构化输出成功",
                    extra={"attempt": attempt + 1, "schema": self.schema_class.__name__}
                )
                return validated

            except ValidationError as e:
                last_error = self._format_validation_error(e)
                logger.warning(f"第 {attempt + 1} 次验证失败: {last_error[:100]}")

            except json.JSONDecodeError as e:
                last_error = f"JSON 解析失败：{e}"
                logger.warning(f"第 {attempt + 1} 次 JSON 解析失败: {e}")

            if attempt < self.max_retries - 1:
                time.sleep(self.retry_delay * (2 ** attempt))  # 指数退避

        raise RuntimeError(
            f"结构化输出在 {self.max_retries} 次重试后仍然失败。\n"
            f"Schema: {self.schema_class.__name__}\n"
            f"最后错误: {last_error}"
        )

    def _build_prompt(
            self, original: str, last_error: Optional[str],
            last_raw: Optional[str], attempt: int
    ) -> str:
        if attempt == 0 or not last_error:
            return original
        return f"""
[重试 {attempt}/{self.max_retries - 1}] 上一次输出存在以下问题，请修正：

问题：{last_error}
上一次的输出：{last_raw}

原始请求：
{original}

请重新生成，确保完全符合要求的格式和类型约束。
"""

    def _call_backend(self, prompt: str, system: str) -> Any:
        """根据后端调用对应 API"""
        if self.backend == OutputBackend.OPENAI:
            return self._call_openai(prompt, system)
        elif self.backend == OutputBackend.ANTHROPIC:
            return self._call_anthropic(prompt, system)
        elif self.backend == OutputBackend.FALLBACK:
            return self._call_fallback(prompt, system)
        raise NotImplementedError(f"Backend {self.backend} not implemented")

    def _call_openai(self, prompt: str, system: str) -> T:
        system_msg = system or "你是一个严格按照指定格式输出结构化数据的助手。"
        response = self._client.beta.chat.completions.parse(
            model=self.model,
            messages=[
                {"role": "system", "content": system_msg},
                {"role": "user", "content": prompt},
            ],
            response_format={
                "type": "json_schema",
                "json_schema": {
                    "name": self.schema_class.__name__,
                    "strict": True,
                    "schema": self.schema_class.model_json_schema(),
                },
            },
            # response_format=self.schema_class,
        )

        return json.loads(response.choices[0].message.content)

    def _call_anthropic(self, prompt: str, system: str) -> T:
        response = self._client.messages.create(
            model=self.model,
            max_tokens=2048,
            system=system or "你是一个严格按照指定格式输出结构化数据的助手。",
            tools=[{
                "name": "structured_output",
                "description": "输出结构化数据",
                "input_schema": self.schema_class.model_json_schema(),
            }],
            tool_choice={"type": "tool", "name": "structured_output"},
            messages=[{"role": "user", "content": prompt}],
        )
        for block in response.content:
            if block.type == "tool_use":
                return self.schema_class(**block.input)
        raise ValueError("Anthropic 未返回工具调用")

    def _call_fallback(self, prompt: str, system: str) -> dict:
        """兜底方案：软约束 + 正则提取"""
        import re
        # 这里用简化示例，实际应调用本地模型
        raise NotImplementedError("Fallback 模式需配置本地模型")

    def _format_validation_error(self, e: ValidationError) -> str:
        """将 Pydantic 验证错误格式化为可读信息"""
        errors = e.errors()
        lines = []
        for err in errors:
            field = " → ".join(str(loc) for loc in err["loc"])
            lines.append(f"字段 [{field}]: {err['msg']}（输入值: {err.get('input', '?')}）")
        return "\n".join(lines)


# ── 使用示例 ──

class SentimentAnalysis(BaseModel):
    """情感分析结果"""
    text_summary: str
    sentiment: str  # "positive" | "negative" | "neutral" | "mixed"
    confidence: float  # 0.0 ~ 1.0
    key_phrases: list[str]
    emotion_tags: list[str]  # 如 ["喜悦", "期待"]


# 一行初始化
harness = StructuredOutputHarness(
    SentimentAnalysis,
    backend=OutputBackend.OPENAI,
    model="qwen3.7-flash",
    max_retries=3,
)

result = harness.extract(
    prompt="分析以下评论的情感：'这家餐厅的菜真的太好吃了，服务态度也非常好，下次还会来！'"
)

print(result.sentiment)  # "positive"
print(result.confidence)  # 0.98
print(result.key_phrases)  # ["好吃", "服务态度好", "下次还会来"]
