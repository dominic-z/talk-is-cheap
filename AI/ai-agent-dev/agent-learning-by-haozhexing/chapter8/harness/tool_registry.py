# harness/tool_registry.py
from dataclasses import dataclass
from enum import IntEnum
from typing import Callable, Any
from functools import wraps
import json
import subprocess
import os
from pathlib import Path
import time


class PermissionLevel(IntEnum):
    READ_ONLY = 1
    WRITE_SAFE = 2
    WRITE_DESTRUCTIVE = 3


@dataclass
class Tool:
    name: str
    func: Callable
    description: str
    permission: PermissionLevel
    parameters: dict  # JSON Schema 格式
    idempotent: bool = True


class HarnessToolRegistry:
    """
    工具注册表：实现权限分层和强类型约束
    """

    def __init__(self, workspace: str, agent_role: str = "code_writer"):
        self.workspace = Path(workspace).resolve()  # 将 workspace 转为绝对路径，避免后续文件操作时因相对路径产生歧义
        self.agent_role = agent_role
        self.tools: dict[str, Tool] = {}
        self._audit_log: list[dict] = []

        # 注册默认工具集
        self._register_default_tools()

    def _register_default_tools(self) -> None:
        """注册编程 Agent 所需的标准工具集"""

        # === 只读工具 ===
        self.register(
            name="read_file",
            func=self._read_file,
            description="读取文件内容。返回文件的文本内容。",
            permission=PermissionLevel.READ_ONLY,
            parameters={
                "type": "object",
                "properties": {
                    "path": {"type": "string", "description": "文件路径（相对于工作目录）"},
                    "offset": {"type": "integer", "description": "起始行号（可选，默认 0）"},
                    "limit": {"type": "integer", "description": "读取行数（可选，默认全部）"},
                },
                "required": ["path"],
            },
        )

        self.register(
            name="list_files",
            func=self._list_files,
            description="列出目录中的文件和子目录。",
            permission=PermissionLevel.READ_ONLY,
            parameters={
                "type": "object",
                "properties": {
                    "path": {"type": "string", "description": "目录路径（默认为工作目录）"},
                },
                "required": [],
            },
        )

        self.register(
            name="search_content",
            func=self._search_content,
            description="在文件中搜索指定内容（支持正则表达式）。",
            permission=PermissionLevel.READ_ONLY,
            parameters={
                "type": "object",
                "properties": {
                    "pattern": {"type": "string", "description": "搜索模式（正则表达式）"},
                    "path": {"type": "string", "description": "搜索路径（默认为工作目录）"},
                },
                "required": ["pattern"],
            },
        )

        # === 写入工具（安全） ===
        self.register(
            name="write_file",
            func=self._write_file,
            description="写入文件内容（会先备份原文件）。",
            permission=PermissionLevel.WRITE_SAFE,
            parameters={
                "type": "object",
                "properties": {
                    "path": {"type": "string", "description": "文件路径"},
                    "content": {"type": "string", "description": "要写入的内容"},
                },
                "required": ["path", "content"],
            },
            idempotent=False,
        )

        # === 执行工具 ===
        self.register(
            name="run_tests",
            func=self._run_tests,
            description="运行 pytest 测试套件，返回测试结果摘要。",
            permission=PermissionLevel.WRITE_SAFE,
            parameters={
                "type": "object",
                "properties": {
                    "pattern": {"type": "string", "description": "测试模式（默认运行全部）"},
                },
                "required": [],
            },
        )

        self.register(
            name="run_linter",
            func=self._run_linter,
            description="运行 ruff 代码检查，返回 Lint 问题列表。",
            permission=PermissionLevel.WRITE_SAFE,
            parameters={
                "type": "object",
                "properties": {
                    "path": {"type": "string", "description": "lint检查，需传入开发的代码所在的文件夹路径"},
                },
                "required": [],
            },
        )

    def register(
            self,
            name: str,
            func: Callable,
            description: str,
            permission: PermissionLevel,
            parameters: dict,
            idempotent: bool = True,
    ) -> None:
        """注册工具"""
        # 包装函数以添加审计日志
        wrapped_func = self._wrap_with_audit(func, name, permission)

        self.tools[name] = Tool(
            name=name,
            func=wrapped_func,
            description=description,
            permission=permission,
            parameters=parameters,
            idempotent=idempotent,
        )

    def _wrap_with_audit(
            self, func: Callable, tool_name: str, permission: PermissionLevel
    ) -> Callable:
        """包装工具函数，添加审计日志"""

        # @wraps(func) 的作用：
        # wrapper 是一个全新的函数，默认情况下外界看到名字是 "wrapper"，文档也是空的。
        # @wraps(func) 把原函数 func 的 "身份证"（__name__、__doc__、__module__ 等）
        # 贴到 wrapper 上，让外界以为自己在调用的还是原来的 func。
        # 如果去掉这行：tool.__name__ 会变成 "wrapper"，help(tool) 也看不到任何说明。
        @wraps(func)
        def wrapper(*args, **kwargs):
            entry = {
                "timestamp": time.time(),
                "tool": tool_name,
                "permission": permission.name,
                "args": str(args)[:100],
                "kwargs": str(kwargs)[:100],
            }
            try:
                result = func(*args, **kwargs)
                entry["status"] = "success"
                return result
            except Exception as e:
                entry["status"] = "error"
                entry["error"] = str(e)
                raise
            finally:
                self._audit_log.append(entry)

        return wrapper

    def call(self, tool_name: str, **kwargs) -> Any:
        """调用工具"""
        if tool_name not in self.tools:
            raise ValueError(f"工具不存在：{tool_name}。可用工具：{list(self.tools.keys())}")

        tool = self.tools[tool_name]
        return tool.func(**kwargs)

    def get_tool_definitions(self) -> list[dict]:
        """返回 OpenAI API 格式的工具定义列表"""
        return [
            {
                "type": "function",
                "function": {
                    "name": tool.name,
                    "description": tool.description,
                    "parameters": tool.parameters,
                }
            }
            for tool in self.tools.values()
        ]

    # === 工具实现 ===

    def _safe_path(self, path: str) -> Path:
        """确保路径在工作目录内（防止路径遍历）"""
        full_path = (self.workspace / path).resolve()
        if not str(full_path).startswith(str(self.workspace)):
            raise ValueError(f"禁止访问工作目录外的文件：{path}")
        return full_path

    def _read_file(self, path: str, offset: int = 0, limit: int = None) -> str:
        full_path = self._safe_path(path)
        if not full_path.exists():
            return f"错误：文件不存在：{path}"

        lines = full_path.read_text(encoding="utf-8").split("\n")
        if limit:
            lines = lines[offset:offset + limit]
        else:
            lines = lines[offset:]

        return "\n".join(f"{offset + i + 1}:{line}" for i, line in enumerate(lines))

    def _list_files(self, path: str = ".") -> str:
        full_path = self._safe_path(path)
        if not full_path.is_dir():
            return f"错误：不是目录：{path}"

        items = []
        for item in sorted(full_path.iterdir()):
            rel_path = item.relative_to(self.workspace)
            prefix = "📁 " if item.is_dir() else "📄 "
            items.append(f"{prefix}{rel_path}")

        return "\n".join(items) if items else "（空目录）"

    def _search_content(self, pattern: str, path: str = ".") -> str:
        full_path = self._safe_path(path)
        result = subprocess.run(
            ["grep", "-rn", "--include=*.py", pattern, str(full_path)],
            capture_output=True, text=True, timeout=30
        )
        return result.stdout[:3000] if result.stdout else "未找到匹配内容"

    def _write_file(self, path: str, content: str) -> str:
        full_path = self._safe_path(path)

        # 备份原文件（如果存在）
        if full_path.exists():
            backup_path = full_path.with_suffix(full_path.suffix + ".bak")
            backup_path.write_text(full_path.read_text(encoding="utf-8"), encoding="utf-8")

        # 确保父目录存在
        full_path.parent.mkdir(parents=True, exist_ok=True)
        full_path.write_text(content, encoding="utf-8")
        return f"✅ 已写入：{path}（{len(content)} 字节）"

    def _run_tests(self, pattern: str = "") -> str:
        """
        python: 调用 Python 解释器。
        -m: 这是 Python 的一个参数，意思是 "module"（模块）。它告诉 Python 去 sys.path 中查找名为 pytest 的模块，并执行该模块的 __main__.py 入口文件。
        pytest: 要执行的模块名称
        """
        cmd = ["/home/dominiczhu/Programs/miniconda3/envs/agent-dev/bin/python",
               "-m", "pytest", "-v", "--tb=short"]
        if pattern:
            cmd.append(pattern)

        result = subprocess.run(
            cmd, capture_output=True, text=True,
            cwd=str(self.workspace), timeout=120
        )

        # 返回摘要而非完整输出（节省上下文）
        output = result.stdout + result.stderr
        lines = output.split("\n")

        # 提取关键行：失败的测试和最终统计
        key_lines = [l for l in lines if
                     "PASSED" in l or "FAILED" in l or "ERROR" in l or
                     "passed" in l or "failed" in l or "error" in l]

        return "\n".join(key_lines[-20:])  # 最多返回最后 20 行关键信息

    def _run_linter(self, path) -> str:
        result = subprocess.run(
            ["/home/dominiczhu/Programs/miniconda3/envs/agent-dev/bin/python",
             "-m", "ruff", "check", self._safe_path(path)],
            capture_output=True, text=True,
            cwd=str(self.workspace), timeout=60
        )
        output = result.stdout + result.stderr
        if not output.strip():
            return "✅ 无 Lint 问题"
        return output[:2000]  # 限制输出长度

    def get_audit_summary(self) -> str:
        """返回工具调用审计摘要"""
        if not self._audit_log:
            return "无工具调用记录"

        counts = {}
        errors = []
        for entry in self._audit_log:
            counts[entry["tool"]] = counts.get(entry["tool"], 0) + 1
            if entry["status"] == "error":
                errors.append(f"  - {entry['tool']}: {entry.get('error', 'unknown')}")

        lines = ["工具调用统计："]
        for tool, count in sorted(counts.items()):
            lines.append(f"  {tool}: {count} 次")

        if errors:
            lines.append("\n调用错误：")
            lines.extend(errors)

        return "\n".join(lines)
