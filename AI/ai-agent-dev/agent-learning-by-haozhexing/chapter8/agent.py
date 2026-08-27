# agent.py

import json
import time
from openai import OpenAI
from harness.context_manager import HarnessContextManager
from harness.tool_registry import HarnessToolRegistry
from harness.validation_gate import HarnessValidationGate
from harness.loop_detector import HarnessLoopDetector
from harness.client_utils import client

SYSTEM_PROMPT = """
你是一个专业的 Python 编程助手，遵循 Harness Engineering 最佳实践。

## 工作流程（强制）
完成每项编码任务时，必须按以下步骤执行：

### 步骤 1：规划
- 分析任务要求
- 列出需要修改的文件
- 制定具体的修改方案

### 步骤 2：实现
- 按计划逐项实现
- 每次修改文件后，简要描述做了什么

### 步骤 3：验证（强制，不可跳过！）
- 调用 run_tests 运行测试
- 调用 run_linter 检查代码风格
- 对照任务要求核查每一项

### 步骤 4：修复（如有问题）
- 修复验证发现的所有问题
- 重新执行步骤 3

只有当步骤 3 完全通过，才能说"任务完成"。

## 项目规范
详见 AGENTS.md（使用 read_file 工具读取）。
"""


class HarnessAgent:
    """
    使用 Harness 系统的编程 Agent
    """

    def __init__(self, workspace: str, api_key: str = None, model: str = "qwen3.7-flash"):
        self.workspace = workspace
        self.model = model
        self.client = client

        # 初始化 Harness 组件
        self.context_manager = HarnessContextManager(max_tokens=128_000)
        self.tool_registry = HarnessToolRegistry(workspace)
        self.validation_gate = HarnessValidationGate(self.tool_registry)
        self.loop_detector = HarnessLoopDetector(threshold=3)

        # 加载系统提示
        self.context_manager.add_message("system", SYSTEM_PROMPT)

        # 注入环境上下文
        env_context = self._build_env_context()
        self.context_manager.add_message("system", env_context)

        # 跟踪修改的文件（用于验证）
        self._modified_files: list[str] = []

        print(f"✅ Harness Agent 初始化完成")
        print(f"   工作目录：{workspace}")
        print(f"   模型：{model}")
        print(f"   可用工具：{len(self.tool_registry.tools)} 个")

    def _build_env_context(self) -> str:
        """构建环境上下文（渐进式披露：给目录而非完整内容）"""
        try:
            # 获取顶层目录结构
            import os
            items = os.listdir(self.workspace)
            structure = "\n".join(f"  - {item}" for item in sorted(items[:20]))
        except Exception:
            structure = "  （无法读取）"

        return f"""
## 工作环境

**工作目录**：{self.workspace}

**顶层目录结构**：
{structure}

**可用工具**：
{chr(10).join(f"  - {name}: {tool.description[:60]}" for name, tool in self.tool_registry.tools.items())}

**提示**：使用 list_files 和 read_file 工具探索项目结构。
使用 read_file 读取 AGENTS.md 了解项目规范。
"""

    def execute(self, task: str, max_iterations: int = 30) -> dict:
        """
        执行任务（带完整 Harness 保护）

        Returns:
            {
                "success": bool,
                "result": str,
                "iterations": int,
                "validation": ValidationResult,
                "context_stats": ContextStats,
            }
        """
        print(f"\n{'=' * 60}")
        print(f"🎯 任务：{task}")
        print(f"{'=' * 60}")


        self.context_manager.add_message("user", task)

        iteration = 0
        last_error = None

        while iteration < max_iterations:
            iteration += 1
            print(f"\n--- 迭代 {iteration}/{max_iterations} ---")

            # 显示上下文健康状态
            stats = self.context_manager.get_stats()
            status_icon = {"healthy": "🟢", "warning": "🟡", "danger": "🔴"}[stats.status]
            print(f"上下文状态：{status_icon} {stats.utilization:.1%} ({stats.total_tokens} tokens)")

            # 调用 LLM
            response = self.client.chat.completions.create(
                model=self.model,
                messages=self.context_manager.get_messages(),
                tools=self.tool_registry.get_tool_definitions(),
                tool_choice="auto",
            )

            message = response.choices[0].message
            print("llm答复:\n"+message.content)
            # 检查是否有工具调用
            if message.tool_calls:
                self.context_manager.add_message("assistant",
                                                 message.content,
                                                 tool_calls=[tc.model_dump() for tc in message.tool_calls])

                # 执行工具调用
                for tool_call in message.tool_calls:
                    tool_name = tool_call.function.name
                    tool_args = json.loads(tool_call.function.arguments)

                    print(f"  🔧 调用工具：{tool_name}({tool_args})")

                    # 死循环检测（针对写文件操作）
                    if tool_name == "write_file":
                        file_path = tool_args.get("path", "")
                        warning = self.loop_detector.record_edit(file_path)

                        if file_path not in self._modified_files:
                            self._modified_files.append(file_path)

                        if warning:
                            # 注入循环警告
                            print(f"  ⚠️ 循环检测：{file_path} 已被编辑 {warning.edit_count} 次")
                            self.context_manager.add_message("system", warning.suggestion)

                    # 执行工具
                    try:
                        result = self.tool_registry.call(tool_name, **tool_args)
                    except Exception as e:
                        result = f"工具执行错误：{str(e)}"

                    # 将结果添加到上下文
                    self.context_manager.add_message("tool", str(result)[:2000],tool_call_id=tool_call.id)

            else:
                # 没有工具调用——Agent 在输出文本响应
                content = message.content or ""
                self.context_manager.add_message("assistant", content)
                print(f"  💬 Agent：{content[:200]}...")

                # 检查是否声称任务完成
                completion_signals = [
                    "任务完成", "task complete", "已完成", "完成了",
                    "successfully completed", "done"
                ]
                # response.choices[0].finish_reason=="stop"  tmd，不能加这个判断，比如，llm做计划的时候，也会finish，例如
                # '`src/` 和 `tests/` 目录还不存在，需要创建。
                #
                # 现在开始**步骤 2：实现**。
                #
                # ### 2.1 创建目录结构和模块'

                if any(signal in content.lower() for signal in completion_signals):
                    print("\n🔍 检测到任务完成信号，执行强制验证...")

                    validation = self.validation_gate.validate(
                        task_description=task,
                        modified_files=self._modified_files,
                    )

                    if validation.passed:
                        print("✅ 验证通过！任务成功完成。")
                        return {
                            "success": True,
                            "result": content,
                            "iterations": iteration,
                            "validation": validation,
                            "context_stats": self.context_manager.get_stats(),
                            "tool_audit": self.tool_registry.get_audit_summary(),
                        }
                    else:
                        # 验证失败：注入失败信息，要求修复
                        print(f"❌ 验证失败，要求修复：")
                        print(validation.failure_report)

                        fix_prompt = f"""
验证失败，任务尚未真正完成！请修复以下问题：

{validation.failure_report}

请：
1. 仔细阅读上面的错误信息
2. 修复所有问题
3. 重新运行测试和 Lint 确认通过
4. 通过所有验证后再声明任务完成
"""
                        self.context_manager.add_message("user", fix_prompt)

        # 超出最大迭代次数
        print(f"\n⚠️ 超出最大迭代次数 ({max_iterations})")
        return {
            "success": False,
            "result": f"任务在 {max_iterations} 次迭代后未完成",
            "iterations": iteration,
            "validation": None,
            "context_stats": self.context_manager.get_stats(),
            "tool_audit": self.tool_registry.get_audit_summary(),
        }
