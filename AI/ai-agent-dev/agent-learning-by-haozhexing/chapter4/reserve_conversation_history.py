from dataclasses import dataclass
from openai import OpenAI
import tiktoken
import os

client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)


@dataclass
class Message:
    role: str  # "user" / "assistant" / "system" / "tool"
    content: str
    token_count: int = 0


class ConversationHistory:
    """全量保留所有消息，token 计数用 tiktoken 精确统计。"""

    def __init__(self, system_prompt: str = "", model: str = "gpt-4.1"):
        # 这个要下载模型，很烦
        # self.encoding = tiktoken.encoding_for_model(model)
        self.messages: list[Message] = []
        if system_prompt:
            self.add_message("system", system_prompt)

    def add_message(self, role: str, content: str) -> None:
        # tokens = len(self.encoding.encode(content))
        self.messages.append(Message(role, content))

    def total_tokens(self) -> int:
        return sum(m.token_count for m in self.messages)

    def chat(self, user_message: str) -> str:
        self.add_message("user", user_message)
        resp = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[{"role": m.role, "content": m.content} for m in self.messages]
        )
        reply = resp.choices[0].message.content
        self.add_message("assistant", reply)
        return reply


def test_conversation_history():
    conversation_history = ConversationHistory()
    print("=" * 50)
    print(conversation_history.chat("python是什么"))
    print("=" * 50)
    print(conversation_history.chat("除了数据计算，python能不能作为后台服务？为什么目前看着python做后台服务的很少"))
    print("=" * 50)
    # 体现记忆
    print(conversation_history.chat("能不能再向我详细解释一些"))


if __name__ == "__main__":
    test_conversation_history()
    print("done")
