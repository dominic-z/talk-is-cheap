from datetime import datetime

from openai import OpenAI
import os
import json

client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)


class Scratchpad:
    """草稿纸：键值存储 + 格式化输出为 Prompt 文本。"""

    def __init__(self):
        self._notes: dict[str, dict] = {}
        self._log: list[dict] = []

    @staticmethod
    def _print_call(method: str, detail: str = ""):
        """在终端醒目地打印工具方法调用信息。"""
        print(f"\n{'─' * 50}")
        print(f"  🔧 [Scratchpad] {method} 被调用")
        if detail:
            print(f"     {detail}")
        print(f"{'─' * 50}")

    def save_to_scratchpad(self, key: str, value, description: str = ""):
        """写入一条笔记（覆盖同 key）。"""
        self._print_call("save_to_scratchpad", f"key={key!r}, value={value!r}, description={description!r}")
        self._notes[key] = {"value": value, "description": description}
        self._log.append({"action": "write", "key": key, "time": datetime.now().strftime("%Y-%m-%d %H:%M:%S")})
        return f"已保存: {key}"

    def read_from_scratchpad(self, key: str):
        value = self._notes.get(key, {}).get("value")
        self._print_call("read_from_scratchpad", f"key={key!r} -> {value!r}")
        return value

    def list_scratchpad_keys(self) -> list[str]:
        keys = list(self._notes.keys())
        self._print_call("list_scratchpad_keys", f"当前所有键: {keys}")
        return keys

    def to_prompt_text(self) -> str:
        """将当前内容序列化为可注入 system prompt 的文本。"""
        if not self._notes:
            return "工作记忆：（空）"
        lines = ["【工作记忆 - 已知信息】"]
        for k, e in self._notes.items():
            desc = f"（{e['description']}）" if e["description"] else ""
            lines.append(f"- {k}{desc}: {json.dumps(e['value'], ensure_ascii=False)}")
        return "\n".join(lines)

    def clear(self):
        self._notes.clear()

    TOOL_FUNCTIONS = {
        "save_to_scratchpad": save_to_scratchpad,
        "read": read_from_scratchpad,
        "list_scratchpad_keys": list_scratchpad_keys
    }


class ScratchpadAgent:
    """将 Scratchpad 暴露为工具集，让 LLM 自己决定何时读写。"""

    def __init__(self):
        self.scratchpad = Scratchpad()

    def _tools(self) -> list[dict]:
        return [
            {"type": "function", "function": {
                "name": "save_to_scratchpad",
                "description": "将中间计算结果保存到工作记忆，供后续步骤使用",
                "parameters": {"type": "object", "properties": {
                    "key": {"type": "string", "description": "键名，英文蛇形命名"},
                    "value": {"description": "要保存的值"},
                    "description": {"type": "string"}
                }, "required": ["key", "value"]}}},
            {"type": "function", "function": {
                "name": "read_from_scratchpad",
                "description": "读取之前保存的中间结果",
                "parameters": {"type": "object", "properties": {
                    "key": {"type": "string"}
                }, "required": ["key"]}}},
            {"type": "function", "function": {
                "name": "list_scratchpad_keys",
                "description": "列出工作记忆中的所有键名",
                "parameters": {"type": "object", "properties": {}}}}
        ]

    def solve(self, problem: str) -> str:
        self.scratchpad.clear()
        messages = [
            {"role": "system", "content": (
                    "你是复杂多步骤问题助手。请将问题分解为多个步骤，"
                    "每步完成后用 save_to_scratchpad 保存中间结果，"
                    "后续步骤可读取前面结果。\n\n"
                    + self.scratchpad.to_prompt_text()
            )},
            {"role": "user", "content": problem}
        ]
        for step in range(10):  # MAX_STEPS 硬上限
            # 关键：每次都重新构建 system_prompt，让模型看到最新 scratchpad
            messages[0]["content"] = self.scratchpad.to_prompt_text()
            resp = client.chat.completions.create(
                model="qwen3.7-flash", messages=messages,
                tools=self._tools(), tool_choice="auto"
            )
            msg = resp.choices[0].message
            messages.append(msg)
            if resp.choices[0].finish_reason == "stop":
                return msg.content
            # 执行工具调用...
            for tc in msg.tool_calls or []:
                result = self._execute_tool(tc.function.name, json.loads(tc.function.arguments))
                messages.append({"role": "tool", "tool_call_id": tc.id, "content": result})
        return "超过最大步骤数"

    def _execute_tool(self, tool_name: str, arguments: dict) -> dict:
        return getattr(self.scratchpad, tool_name)(**arguments)


if __name__ == "__main__":
    print("\n" + "=" * 60)
    print("          📝 工作记忆（Scratchpad）功能测试")
    print("=" * 60)

    scratch_pad_agent = ScratchpadAgent()

    # 测试 1：写入工作记忆
    print("\n【测试 1】写入工作记忆 save_to_scratchpad")
    scratch_pad_agent._execute_tool(
        "save_to_scratchpad",
        {"key": "user_name", "value": "Dominic", "description": "用户姓名"},
    )

    # 测试 2：列出所有键
    print("\n【测试 2】列出所有键 list_scratchpad_keys")
    keys = scratch_pad_agent._execute_tool("list_scratchpad_keys", {})
    print(f"  返回结果: {keys}")
    assert "user_name" in keys, "❌ list_scratchpad_keys 未返回已写入的键"

    # 测试 3：读取已存在的键
    print("\n【测试 3】读取已存在的键 read_from_scratchpad")
    value = scratch_pad_agent._execute_tool("read_from_scratchpad", {"key": "user_name"})
    print(f"  返回结果: {value}")
    assert value == "Dominic", "❌ read_from_scratchpad 读取的值不正确"

    # 测试 4：读取不存在的键（应返回 None）
    print("\n【测试 4】读取不存在的键 read_from_scratchpad")
    missing = scratch_pad_agent._execute_tool("read_from_scratchpad", {"key": "not_exist"})
    print(f"  返回结果: {missing}")
    assert missing is None, "❌ 读取不存在的键应返回 None"

    # 测试 5：查看工作记忆序列化内容（注入 Prompt 的文本）
    print("\n【测试 5】查看工作记忆序列化内容 to_prompt_text")
    print(scratch_pad_agent.scratchpad.to_prompt_text())

    print("\n" + "=" * 60)
    print("          ✅ 所有工作记忆测试通过")
    print("=" * 60 + "\n")
