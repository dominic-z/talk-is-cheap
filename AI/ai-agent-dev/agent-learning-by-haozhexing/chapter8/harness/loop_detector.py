# harness/loop_detector.py

from collections import defaultdict
from dataclasses import dataclass
import time
from typing import Optional


@dataclass
class LoopWarning:
    file_path: str
    edit_count: int
    suggestion: str


class HarnessLoopDetector:
    """
    死循环检测器

    检测：同一文件被反复修改（可能陷入"尝试-失败-再尝试"循环）
    响应：注入"换个思路"的建议，而不是强制中断
    """

    def __init__(self, threshold: int = 3):
        self.threshold = threshold
        self.edit_history: dict[str, list[float]] = defaultdict(list)

    def record_edit(self, file_path: str) -> Optional[LoopWarning]:
        """
        记录一次文件编辑。
        如果超过阈值，返回警告（否则返回 None）。
        """
        now = time.time()
        self.edit_history[file_path].append(now)

        # 只看最近 10 分钟内的编辑
        recent = [t for t in self.edit_history[file_path] if now - t < 600]
        self.edit_history[file_path] = recent

        if len(recent) > self.threshold:
            return LoopWarning(
                file_path=file_path,
                edit_count=len(recent),
                suggestion=self._generate_suggestion(file_path, len(recent)),
            )
        return None

    def _generate_suggestion(self, file_path: str, count: int) -> str:
        return f"""
⚠️ 循环检测警告：你已经对 `{file_path}` 进行了 {count} 次修改。

反复修改同一文件通常表明：
1. 问题的根本原因可能不在这个文件（请检查调用它的地方）
2. 你的修改方向可能有误（请重新阅读任务要求）
3. 存在其他文件的依赖问题（请用 search_content 查找相关代码）

建议：
- 暂停修改此文件
- 运行测试，仔细阅读错误信息
- 用 search_content 搜索与错误相关的代码
- 如果仍然无法解决，请描述遇到的具体问题
"""

    def get_report(self) -> str:
        """返回编辑历史报告"""
        if not self.edit_history:
            return "无编辑记录"

        lines = ["文件编辑历史："]
        for path, timestamps in sorted(self.edit_history.items()):
            recent = [t for t in timestamps if time.time() - t < 600]
            if recent:
                status = "⚠️ 警告" if len(recent) > self.threshold else "✅ 正常"
                lines.append(f"  {status} {path}: 最近10分钟 {len(recent)} 次编辑")

        return "\n".join(lines)
