import json, uuid, datetime, chromadb, os
from openai import OpenAI

client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)


# ─────────── 打印工具 ───────────
def print_user(msg: str):
    print(f"\n🙋 用户：{msg}")


def print_assistant(name: str, msg: str):
    print(f"🤖 {name}：{msg}")


def print_recalled(memories: list[dict]):
    """打印本轮检索出的长期记忆。"""
    if not memories:
        print("   📖 [读取记忆] 无相关记忆")
        return
    print(f"   📖 [读取记忆] 共命中 {len(memories)} 条：")
    for i, m in enumerate(memories, 1):
        print(f"      {i}. [{m['type']}] {m['content']} "
              f"(重要度={m['importance']}, 相关度={m['relevance']:.3f})")


def print_stored(memories: list[dict]):
    """打印本轮新存入的长期记忆。"""
    if not memories:
        print("   💾 [存储记忆] 本轮无值得长期记忆的内容")
        return
    print(f"   💾 [存储记忆] 新写入 {len(memories)} 条：")
    for i, m in enumerate(memories, 1):
        print(f"      {i}. [{m.get('type', 'general')}] {m['content']} "
              f"(重要度={m.get('importance', 5)})")


# 这个例子里没有工作记忆
class PersonalAssistant:
    def __init__(self, user_id: str, assistant_name: str = "小助"):
        self.user_id = user_id
        self.assistant_name = assistant_name
        # 长期记忆：ChromaDB
        self.memory = chromadb.PersistentClient(
            path=f"/tmp/memory_{user_id}"
        ).get_or_create_collection(
            name="long_term_memory",
            metadata={"hnsw:space": "cosine"}
        )
        # 短期记忆：滑动窗口（最近 10 轮 = 20 条消息）
        self.history: list[dict] = []
        self.max_history_turns = 10

    def _embed(self, text: str) -> list[float]:
        return client.embeddings.create(
            input=text, model="text-embedding-v3",dimensions=1024,
        ).data[0].embedding

    def recall(self, query: str, n: int = 5) -> list[dict]:
        """按 query 检索最相关的长期记忆，过滤低相关度（< 0.4）。"""
        if self.memory.count() == 0:
            return []
        results = self.memory.query(
            query_embeddings=[self._embed(query)],
            n_results=min(n, self.memory.count()),
            where={"user_id": self.user_id},
            include=["documents", "metadatas", "distances"]
        )
        memories = []
        #  results的数据结构里，results["documents"][0]本身是一个列表，相当于recall了一些文档
        for doc, meta, dist in zip(
            results["documents"][0], results["metadatas"][0],
            results["distances"][0]
        ):
            relevance = 1 - dist
            if relevance > 0.4:        # ← 关键阈值
                memories.append({
                    "content": doc, "type": meta.get("type", "general"),
                    "importance": meta.get("importance", 5),
                    "relevance": relevance
                })
        return sorted(memories, key=lambda x: x["relevance"], reverse=True)

    def _auto_extract(self, user_msg: str, assistant_reply: str):
        """每轮对话后用小模型提取值得长期记忆的内容。"""
        prompt = f"""从以下对话中提取值得长期记忆的用户信息。
用户说：{user_msg}
助手回复：{assistant_reply[:200]}

提取规则：
✅ 要提取：用户的个人信息、偏好、工作、技能、正在做的项目、明确表达的需求
❌ 不提取：日常问候、临时查询、没有持久价值的内容

返回JSON数组（无则返回 []）：[{{"content": "...", "type": "fact|preference|task|skill", "importance": 1-10}}]"""
        try:
            resp = client.chat.completions.create(
                model="qwen3.7-flash",
                messages=[{"role": "user", "content": prompt}],
                response_format={"type": "json_object"},
                max_tokens=300
            )
            data = json.loads(resp.choices[0].message.content) # resp的content的返回格式已经标准的json格式了，在prompt中有明确
            memories = data if isinstance(data, list) else data.get("memories", [])
            stored = []
            for m in memories:
                if isinstance(m, dict) and m.get("content"):
                    self.memory.add(
                        ids=[str(uuid.uuid4())],
                        embeddings=[self._embed(m["content"])],
                        documents=[m["content"]],
                        metadatas=[{
                            "type": m.get("type", "general"),
                            "importance": m.get("importance", 5),
                            "user_id": self.user_id,
                            "created_at": datetime.datetime.now().isoformat()
                        }]
                    )
                    stored.append(m)
            print_stored(stored)
        except Exception:
            pass     # 提取失败不影响主对话

    def chat(self, user_message: str) -> str:
        """核心流水线：检索 → 调用 → 更新 → 提取。"""
        # ① 检索相关长期记忆
        memories = self.recall(user_message, n=5)
        print_recalled(memories)

        # ② 拼装 system prompt
        system = f"你是 {self.assistant_name}，用户 {self.user_id} 的专属助理。"
        if memories:
            memory_text = "\n".join(
                f"- [{m['type']}] {m['content']}" for m in memories[:3]
            )
            system += f"\n\n【关于用户的记忆】\n{memory_text}"

        # ③ 滑动窗口内的近期消息 + 当前用户消息
        window = self.history[-(self.max_history_turns * 2):]
        messages = [{"role": "system", "content": system}] + window \
                 + [{"role": "user", "content": user_message}]

        # ④ 调用 LLM
        resp = client.chat.completions.create(
            model="qwen3.7-flash", messages=messages, max_tokens=800
        )
        reply = resp.choices[0].message.content

        # ⑤ 更新短期记忆（滑动窗口）
        self.history.append({"role": "user", "content": user_message})
        self.history.append({"role": "assistant", "content": reply})

        # ⑥ 自动提取新记忆（不影响本次响应）
        self._auto_extract(user_message, reply)

        return reply


def test_assistant_first():
    sep = "═" * 60

    personal_assistant = PersonalAssistant("zhangwei")
    for msg in [
        "我叫张伟，是一名 Python 工程师，正在做一个 AI 项目",
        "帮我写一个 Python 函数来计算斐波那契数列",
    ]:
        print(f"\n{sep}\n💬 第 1 个会话实例\n{sep}")
        print_user(msg)
        print_assistant(personal_assistant.assistant_name,
                        personal_assistant.chat(msg))

    print(f"\n{sep}\n🔄 新建会话实例（短期记忆重置，长期记忆共享）\n{sep}")

    another_personal_assistant = PersonalAssistant("zhangwei")
    for msg in [
        "我是谁？",
        "昨天那个斐波那契函数能优化吗？",
    ]:
        print(f"\n{sep}\n💬 第 2 个会话实例\n{sep}")
        print_user(msg)
        print_assistant(another_personal_assistant.assistant_name,
                        another_personal_assistant.chat(msg))


if __name__ == "__main__":
    test_assistant_first()
    print("done")
