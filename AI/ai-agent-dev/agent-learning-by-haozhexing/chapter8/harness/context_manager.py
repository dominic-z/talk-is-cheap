# harness/context_manager.py

import time
from dataclasses import dataclass, field
from typing import Optional,List
import tiktoken
from .client_utils import  client

def count_tokens(text: str, model: str = "gpt-4.1") -> int:
    """统计 token 数量"""
    try:
        resp = client.embeddings.create( model="text-embedding-v3")
        return resp.usage.total_tokens
        # enc = tiktoken.encoding_for_model(model)
        # return len(enc.encode(text))
    except Exception:
        # 粗略估算：平均每 4 个字符 1 个 token
        return len(text) // 4


@dataclass
class ContextStats:
    """上下文统计信息"""
    total_tokens: int
    max_tokens: int
    utilization: float
    message_count: int
    status: str  # "healthy", "warning", "danger"
    recommendation: str


class HarnessContextManager:
    """
    Harness 上下文管理器

    功能：
    1. 实时监控上下文利用率
    2. 自动触发渐进式压缩
    3. 提供健康报告
    """

    COMPRESSION_THRESHOLD = 0.40
    DANGER_THRESHOLD = 0.70

    def __init__(self, max_tokens: int = 128_000):
        self.max_tokens = max_tokens
        self.messages: list[dict] = []
        self._compression_count = 0

    def add_message(self, role: str, content: str,tool_calls:List | None=None,tool_call_id:str | None= None) -> None:
        """添加消息，并在必要时触发压缩"""
        message = {"role": role, "content": content}
        if tool_calls:
            message["tool_calls"] = tool_calls
        if tool_call_id:
            message["tool_call_id"] = tool_call_id
        self.messages.append(message)

        stats = self.get_stats()
        if stats.utilization >= self.COMPRESSION_THRESHOLD:
            self._auto_compress(stats)

    def get_stats(self) -> ContextStats:
        """获取当前上下文统计"""
        total = sum(
            count_tokens(m.get("content", ""))
            for m in self.messages
        )
        util = total / self.max_tokens

        if util < self.COMPRESSION_THRESHOLD:
            status, recommendation = "healthy", "正常，无需操作"
        elif util < self.DANGER_THRESHOLD:
            status, recommendation = "warning", "建议清理旧工具输出"
        else:
            status, recommendation = "danger", "⚠️ 立即触发完整压缩"

        return ContextStats(
            total_tokens=total,
            max_tokens=self.max_tokens,
            utilization=util,
            message_count=len(self.messages),
            status=status,
            recommendation=recommendation,
        )

    def _auto_compress(self, stats: ContextStats) -> None:
        """自动渐进式压缩"""
        # 第一步：轻量压缩——清除旧工具结果
        self._clear_old_tool_results()
        self._compression_count += 1

        # 检查是否仍然超限
        new_stats = self.get_stats()
        if new_stats.utilization >= self.COMPRESSION_THRESHOLD:
            # 第二步：完整压缩（需要外部 LLM 调用，这里用简化实现）
            self._truncate_middle()
            self._compression_count += 1

    def _clear_old_tool_results(self) -> None:
        """轻量压缩：清除较旧的工具输出"""
        cutoff = len(self.messages) - 8
        for i in range(cutoff):
            msg = self.messages[i]
            if msg.get("role") == "tool" and len(msg.get("content", "")) > 200:
                self.messages[i] = {
                    "role": "tool",
                    "content": "[工具输出已归档以节省上下文空间]",
                    "tool_call_id": msg.get("tool_call_id"),
                }

    def _truncate_middle(self) -> None:
        """保留头部（系统消息）和尾部（最近对话），压缩中间部分"""
        system_msgs = [m for m in self.messages if m["role"] == "system"]
        recent_msgs = self.messages[-6:]

        # 用占位符替代中间历史
        self.messages = system_msgs + [
            {
                "role": "system",
                "content": "[早期对话历史已压缩。关键决策：见上方系统消息。]"
            }
        ] + recent_msgs

    def get_messages(self) -> list[dict]:
        """获取当前消息列表（供 LLM API 调用使用）"""
        return self.messages.copy()
