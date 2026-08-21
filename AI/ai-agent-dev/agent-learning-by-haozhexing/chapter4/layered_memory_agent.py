import os
import json
from openai import OpenAI


class LayeredMemoryAgent:
    """分层记忆 Agent：Core + Working + Archive 三层。"""

    def __init__(self, model: str = "qwen3.7-flash"):
        self.model = model
        # 始终在 Prompt 中：放用户画像和关键偏好
        self.client = OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )
        self.core_memory = {
            "user_name": "", "preferences": [],
            "key_facts": [], "active_goals": []
        }
        # 当前任务相关的短期信息
        self.working_memory = []
        # 持久化存储，模拟向量数据库
        self.archive_memory = []
        # 对话历史
        self.conversation = []

    def chat(self, user_input: str) -> str:
        """主对话入口：5 步流水线。"""
        # 1. 自动记忆管理：检查是否需要更新记忆
        self._auto_manage_memory(user_input)
        # 2. 构建含记忆的 Prompt
        messages = self._build_messages(user_input) # 这一步在system中新增了一个重要的prompt，就是在chat的过程中，让llm同时提取要记忆哪些内容
        # 3. 调用 LLM（带上记忆工具，让 LLM 在对话中自主决定是否编辑记忆）
        response = self.client.chat.completions.create(
            model=self.model, messages=messages,
            max_tokens=2000, tools=self._get_memory_tools()
        )
        # 4. 处理工具调用（记忆自我编辑）
        reply = self._process_response(response, messages)
        # 5. 保存到对话历史
        self.conversation.append({"role": "user", "content": user_input})
        self.conversation.append({"role": "assistant", "content": reply})
        return reply

    def _build_messages(self, user_input: str) -> list[dict]:
        """System Prompt 中始终注入核心记忆 + 近期工作记忆。"""
        system = f"""你是具备分层记忆能力的 Agent。
## 核心记忆（始终记住）
{json.dumps(self.core_memory, ensure_ascii=False, indent=2)}
## 工作记忆（当前任务相关）
{json.dumps(self.working_memory[-5:], ensure_ascii=False, indent=2)}
## 记忆管理指令
- 核心记忆中的信息是你的"常识"，始终作为回答依据
- 问到归档内容时，用 search_archive 工具检索
- 需要记新信息时，用 update_core_memory 工具
- 当前对话产生需长期保存的内容时，用 archive_content 工具"""
        recent = self.conversation[-20:]  # 滑动窗口：最近 10 轮
        return [{"role": "system", "content": system}] + recent \
            + [{"role": "user", "content": user_input}]

    def _auto_manage_memory(self, user_input: str):
        """每轮对话前，用小提示词自动从用户输入中提取值得记住的信息。

        这是"被动式"记忆管理：不依赖 LLM 主动调工具，
        而是直接要求它返回结构化 JSON，失败也不影响主对话。
        """
        prompt = f"""分析用户输入，判断是否包含需要更新记忆的信息。
用户输入：{user_input}

提取规则：
- 用户姓名/长期偏好/关键事实/当前目标 → layer=core（核心记忆）
- 当前任务相关的临时信息 → layer=working（工作记忆）
- 日常寒暄、没有持久价值的内容 → 不提取

返回JSON：{{"memories": [{{"layer": "core|working", "field": "user_name|preferences|key_facts|active_goals", "content": "..."}}]}}
注意：layer 为 core 时必须给 field；layer 为 working 时不需要 field；无需提取时 memories 为空数组。"""
        try:
            resp = self.client.chat.completions.create(
                model=self.model,
                messages=[{"role": "user", "content": prompt}],
                response_format={"type": "json_object"},
                max_tokens=300,
            )
            data = json.loads(resp.choices[0].message.content)
            for m in data.get("memories", []):
                content = m.get("content")
                if not content:
                    continue
                if m.get("layer") == "core":
                    self._write_core_memory(m.get("field", ""), content)
                else:
                    if content not in self.working_memory:
                        self.working_memory.append(content)
                        print(f"   🧠 [工作记忆] + {content}")
                    # 工作记忆容量有限，只保留最近 10 条
                    self.working_memory = self.working_memory[-10:]
        except Exception:
            pass  # 记忆管理失败不影响主对话

    def _write_core_memory(self, field: str, content: str) -> bool:
        """写入核心记忆，返回是否成功。"""
        if field == "user_name":
            self.core_memory["user_name"] = content
        elif field in ("preferences", "key_facts", "active_goals"):
            if content not in self.core_memory[field]:
                self.core_memory[field].append(content)
            else:
                return False  # 已存在，无需重复写
        else:
            return False
        print(f"   ⭐ [核心记忆] {field} ← {content}")
        return True

    def _get_memory_tools(self) -> list[dict]:
        """定义 Agent 可自主调用的记忆管理工具（MemGPT 的"记忆自我编辑"）。"""
        return [
            {
                "type": "function",
                "function": {
                    "name": "update_core_memory",
                    "description": "将重要信息写入核心记忆（始终记住）。用于用户透露姓名、偏好、关键事实、目标时。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "field": {
                                "type": "string",
                                "enum": ["user_name", "preferences", "key_facts", "active_goals"],
                                "description": "要更新的核心记忆字段",
                            },
                            "content": {"type": "string", "description": "要记住的内容"},
                        },
                        "required": ["field", "content"],
                    },
                },
            },
            {
                "type": "function",
                "function": {
                    "name": "archive_content",
                    "description": "将当前对话中需长期保存的内容归档。用于产生大量细节、结论、长文本时。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "content": {"type": "string", "description": "要归档的内容"},
                        },
                        "required": ["content"],
                    },
                },
            },
            {
                "type": "function",
                "function": {
                    "name": "search_archive",
                    "description": "按关键词检索归档记忆。用于用户问到之前聊过的历史细节、而当前上下文中没有时。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "query": {"type": "string", "description": "检索关键词"},
                        },
                        "required": ["query"],
                    },
                },
            },
        ]

    def _process_response(self, response, messages: list[dict]) -> str:
        """处理 LLM 响应：若请求调用记忆工具，执行后把结果回传并继续生成。

        传入 messages 是为了在工具调用后能把工具结果追加进同一上下文，
        再次调用 LLM 生成最终回复（多轮工具循环）。
        """
        for _ in range(3):  # 最多 3 轮工具循环，防止死循环
            msg = response.choices[0].message
            if not msg.tool_calls:
                return msg.content or ""
            # 把助手的工具调用请求追加进上下文
            messages.append({
                "role": "assistant",
                "content": msg.content or "",
                "tool_calls": [
                    {
                        "id": tc.id,
                        "type": "function",
                        "function": {
                            "name": tc.function.name,
                            "arguments": tc.function.arguments,
                        },
                    }
                    for tc in msg.tool_calls
                ],
            })
            # 逐个执行工具并回传结果
            for tc in msg.tool_calls:
                try:
                    args = json.loads(tc.function.arguments or "{}")
                except json.JSONDecodeError:
                    args = {}
                result = self._dispatch_tool(tc.function.name, args)
                print(f"   🔧 [工具调用] {tc.function.name}({args}) → {result}")
                messages.append({
                    "role": "tool",
                    "tool_call_id": tc.id,
                    "content": result,
                })
            # 带着工具结果继续调用 LLM，直到它给出最终文本回复
            response = self.client.chat.completions.create(
                model=self.model, messages=messages,
                max_tokens=2000, tools=self._get_memory_tools()
            )
        return response.choices[0].message.content or ""

    def _dispatch_tool(self, name: str, args: dict) -> str:
        """根据工具名分发到对应的记忆操作。"""
        if name == "update_core_memory":
            ok = self._write_core_memory(args.get("field", ""), args.get("content", ""))
            return "已更新核心记忆" if ok else "字段不存在或内容已存在"
        if name == "archive_content":
            content = args.get("content", "")
            if content:
                self.archive_memory.append(content)
                print(f"   🗄️ [归档记忆] + {content[:50]}...")
                return "已归档"
            return "归档内容为空"
        if name == "search_archive":
            results = self._search_archive(args.get("query", ""))
            if results:
                return "检索到以下归档内容：\n" + "\n".join(f"- {r}" for r in results)
            return "归档中未找到相关内容"
        return f"未知工具：{name}"

    def _search_archive(self, query: str, top_k: int = 3) -> list[str]:
        """按字符重叠度检索归档记忆（模拟检索；真实生产应使用向量数据库）。"""
        # 中文没有天然分词，这里用字符级重叠作为简易相关性评分
        query_chars = {ch for ch in query if not ch.isspace()}
        scored = []
        for content in self.archive_memory:
            score = sum(1 for ch in query_chars if ch in content)
            if score > 0:
                scored.append((score, content))
        scored.sort(key=lambda x: x[0], reverse=True)
        return [content for _, content in scored[:top_k]]


if __name__ == "__main__":
    agent = LayeredMemoryAgent()

    for msg in [
        # 第一轮：用户介绍自己 → 触发核心记忆提取
        "你好！我叫小明，我是一名数据科学家，平时喜欢用 Python",
        # 第二轮：用户提出偏好
        "我比较喜欢简洁的回答，不要太啰嗦",
        # 第三轮：用户讨论工作
        "我正在做一个客户流失预测项目，使用的是 XGBoost",
        # 第四轮：验证记忆是否保持
        "我之前说我在做什么项目来着？",
    ]:
        print("═" * 60)
        print(f"🙋 用户：{msg}")
        print(f"🤖 助手：{agent.chat(msg)}")

    print("═" * 60)
    print("📦 最终核心记忆：", json.dumps(agent.core_memory, ensure_ascii=False, indent=2))
    print("🗄️ 归档记忆条数：", len(agent.archive_memory))
