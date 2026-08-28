# agent.py
import os
import json
import inspect
from pathlib import Path
from openai import OpenAI
from skill_manager import SkillManager

# Function Calling 单轮对话内最多允许的工具调用轮次
MAX_TOOL_ROUNDS = 8

# Python 类型注解 -> JSON Schema 类型
_JSON_TYPES = {str: "string", int: "integer", float: "number", bool: "boolean"}


class SkillAgent:
    """技能驱动的 Agent

    - prompt-based 技能：技能内容注入系统提示，直接对话
    - code-based 技能（带工具）：技能内容注入系统提示 +
      技能工具注册为 Function Calling，由 LLM 实际调用工具代码
    """

    def __init__(self, skill_manager: SkillManager, model: str = "qwen3.7-flash"):
        self.skill_manager = skill_manager
        self.client = OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )
        self.model = model
        self.conversation_history = []

    def chat(self, user_message: str) -> str:
        """处理用户消息"""

        # 1. 技能选择（让 LLM 决定使用哪个技能）
        selected_skill = self._select_skill(user_message)

        # 2. 构建系统提示（加载选中的技能）
        system_prompt = self._build_system_prompt(selected_skill)

        # 3. 若技能带工具，则构建 Function Calling 工具定义
        tool_funcs, tools_schema = self._get_skill_tools(selected_skill)

        # 4. 调用 LLM（带工具的技能进入工具调用循环）
        self.conversation_history.append(
            {"role": "user", "content": user_message}
        )

        messages = [
            {"role": "system", "content": system_prompt},
            *self.conversation_history
        ]

        for _ in range(MAX_TOOL_ROUNDS):
            kwargs = {}
            if tools_schema:
                kwargs["tools"] = tools_schema

            response = self.client.chat.completions.create(
                model=self.model,
                messages=messages,
                **kwargs,
            )

            message = response.choices[0].message

            # 无需调用工具，对话结束
            if not message.tool_calls:
                assistant_message = message.content or ""
                self.conversation_history.append(
                    {"role": "assistant", "content": assistant_message}
                )
                return assistant_message

            # 记录 LLM 的工具调用请求，逐个执行工具并回传结果
            messages.append({
                "role": "assistant",
                "content": message.content or "",
                "tool_calls": message.tool_calls,
            })
            for tool_call in message.tool_calls:
                result = self._call_tool(tool_funcs, tool_call)
                messages.append({
                    "role": "tool",
                    "tool_call_id": tool_call.id,
                    "content": result,
                })

        # 工具调用轮次超限，让 LLM 基于已有结果收尾
        messages.append({
            "role": "user",
            "content": "工具调用轮次已达上限，请基于以上已有结果直接给出最终回答。"
        })
        response = self.client.chat.completions.create(
            model=self.model,
            messages=messages,
        )
        assistant_message = response.choices[0].message.content or ""
        self.conversation_history.append(
            {"role": "assistant", "content": assistant_message}
        )
        return assistant_message

    def _select_skill(self, task: str) -> str:
        """让 LLM 选择最合适的技能"""
        skill_summaries = self.skill_manager.get_skill_summaries_prompt()

        selection_prompt = f"""根据用户的任务，选择最合适的技能。
只需要返回技能名称，不需要其他内容。
如果没有合适的技能，返回 "none"。

{skill_summaries}

用户任务: {task}
选择的技能:"""

        response = self.client.chat.completions.create(
            model=self.model,
            messages=[{"role": "user", "content": selection_prompt}],
            max_tokens=50,
            temperature=0
        )

        skill_name = response.choices[0].message.content.strip().lower()
        # 去掉 LLM 可能附带的引号、反引号、句号等修饰符，只保留名称主体
        skill_name = skill_name.strip('`"\'.').split("\n")[0].strip()

        if skill_name != "none":
            # LLM 可能返回连字符或下划线形式，两种写法都尝试匹配
            for variant in (skill_name, skill_name.replace("-", "_"),
                            skill_name.replace("_", "-")):
                if variant in self.skill_manager.skills:
                    print(f"  🎯 选择技能: {variant}")
                    return variant

        # LLM 选择失败，尝试关键词匹配
        discovered = self.skill_manager.discover(task)
        if discovered:
            print(f"  🔍 发现技能: {discovered[0].name}")
            return discovered[0].name

        print("  ℹ️ 未匹配到特定技能，使用通用模式")
        return None

    def _build_system_prompt(self, skill_name: str = None) -> str:
        """构建系统提示"""
        base_prompt = "你是一个智能 Agent 助手。"

        if skill_name:
            skill = self.skill_manager.get_skill(skill_name)
            prompt = f"""{base_prompt}

当前已激活技能：{skill.name} (v{skill.version})

{skill.content}

请严格按照上述技能指南来完成用户的任务。
"""
            # prompt-based 技能可能附带模板等资源文件，一并注入上下文
            resources = self._load_templates(skill)
            if resources:
                prompt += "\n\n本技能附带的参考资料：\n" + resources

            if getattr(skill, "tool_funcs", None):
                tool_names = ", ".join(skill.tool_funcs.keys())
                prompt += (
                    f"\n该技能提供了以下工具：{tool_names}。"
                    "请通过工具调用来使用它们，不要自行虚构工具的执行结果。\n"
                )
            return prompt

        return base_prompt

    def _load_templates(self, skill) -> str:
        """读取技能目录下 templates/ 中的资源文件内容"""
        if not skill.path:
            return ""
        templates_dir = Path(skill.path).parent / "templates"
        if not templates_dir.exists():
            return ""
        parts = []
        for f in sorted(templates_dir.glob("*.md")):
            parts.append(f"### 文件: templates/{f.name}\n{f.read_text(encoding='utf-8')}")
        return "\n\n".join(parts)

    def _get_skill_tools(self, skill_name: str):
        """获取技能的工具函数及 Function Calling schema"""
        if not skill_name:
            return {}, []
        skill = self.skill_manager.get_skill(skill_name)
        tool_funcs = getattr(skill, "tool_funcs", {}) or {}
        return tool_funcs, [
            self._tool_schema(func) for func in tool_funcs.values()
        ]

    def _tool_schema(self, func) -> dict:
        """根据函数签名与 docstring 生成 OpenAI Function Calling schema"""
        sig = inspect.signature(func)
        doc = inspect.getdoc(func) or ""
        description, *rest = doc.split("Args:", 1)

        properties = {}
        required = []
        for param in sig.parameters.values():
            json_type = _JSON_TYPES.get(param.annotation, "string")
            if param.annotation in (list, tuple):
                json_type = "array"
            properties[param.name] = {"type": json_type}
            if param.default is inspect.Parameter.empty:
                required.append(param.name)

        return {
            "type": "function",
            "function": {
                "name": func.__name__,
                "description": description.strip(),
                "parameters": {
                    "type": "object",
                    "properties": properties,
                    "required": required,
                },
            },
        }

    def _call_tool(self, tool_funcs: dict, tool_call) -> str:
        """执行一次工具调用，返回字符串结果"""
        name = tool_call.function.name
        func = tool_funcs.get(name)
        if func is None:
            return f"[错误] 未知工具: {name}"

        try:
            arguments = json.loads(tool_call.function.arguments or "{}")
        except json.JSONDecodeError:
            return f"[错误] 工具 {name} 的参数不是合法 JSON: {tool_call.function.arguments}"

        print(f"  🔧 调用工具: {name}({arguments})")
        try:
            result = func(**arguments)
        except TypeError as e:
            return f"[错误] 工具 {name} 参数不匹配: {e}"
        except Exception as e:
            return f"[错误] 工具 {name} 执行失败: {e}"

        return result if isinstance(result, str) else json.dumps(
            result, ensure_ascii=False, default=str
        )
