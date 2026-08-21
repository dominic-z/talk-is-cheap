from openai import OpenAI
import os

client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)


class MemoryExtractor:
    """分析对话轮次，提取值得长期记忆的内容。"""

    EXTRACT_PROMPT = """分析以下对话，提取值得长期记忆的重要信息。
用户说：{user_msg}
助手回复：{assistant_reply}

提取规则：
- 记录用户的个人信息、偏好、习惯、正在做的项目、重要决策
- 忽略闲聊、问候、临时查询、重复信息
- 用简洁陈述句（第三人称）

返回 JSON 数组（无内容则返回 []）：
[{{"content": "记忆内容", "type": "preference|fact|event|task|skill", "importance": 1-10}}]"""

    def simple_chat(self, message: str) -> str:
        """最基本的单轮对话"""
        response = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[
                {"role": "user", "content": message}
            ]
        )
        return response.choices[0].message.content

    def extract(self, user_msg: str, assistant_reply: str) -> list[dict]:
        import json
        prompt = self.EXTRACT_PROMPT.format(
            user_msg=user_msg, assistant_reply=assistant_reply[:300])
        resp = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[{"role": "user", "content": prompt}],
            response_format={"type": "json_object"},
            max_tokens=300
        )
        try:
            data = json.loads(resp.choices[0].message.content)
            return data if isinstance(data, list) else data.get("memories", [])
        except Exception:
            return []


if __name__ == "__main__":
    separator = "=" * 60
    mem_extractor = MemoryExtractor()
    init_prompt = "我是一名Java开发工程师，大概3年经验左右，现在希望通过Java开发一个旅游攻略生成工具，你推荐我使用什么技术栈？"
    print(f"\n{separator}")
    print(f"📥 init_prompt:\n{init_prompt}")
    print(separator)
    init_replay = mem_extractor.simple_chat(init_prompt)

    print(f"\n{separator}")
    print(f"🤖 init_replay:\n{init_replay}")
    print(separator)
    mem_extract = mem_extractor.extract(init_prompt, init_replay)

    print(f"\n{separator}")
    print(f"🧠 memory_extract:\n{mem_extract}")
    print(f"\n{separator}")
    print("✅ done")
    print(separator)
