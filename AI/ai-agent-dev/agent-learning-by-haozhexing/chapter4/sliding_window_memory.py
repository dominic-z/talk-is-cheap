import os
from openai import OpenAI
client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)
class SlidingWindowMemory:
    """只保留最近 N 轮；system_prompt 始终保留。"""

    def __init__(self, system_prompt: str = "", max_turns: int = 3,
                 max_tokens: int = 8000, model: str = "gpt-4.1-mini"):
        self.system_prompt = system_prompt
        self.max_turns = max_turns
        self.max_tokens = max_tokens
        # self.encoding = tiktoken.encoding_for_model(model)
        self.all_messages: list[dict] = []   # 完整历史（不传给 LLM）

    # def _count(self, msgs: list[dict]) -> int:
    #     return sum(len(self.encoding.encode(m.get("content", ""))) for m in msgs)

    def _window(self) -> list[dict]:
        system_mem = [{"role": "system", "content": self.system_prompt}] if self.system_prompt else []
        # 两倍的，因为是q+a，所以是二倍
        recent = self.all_messages[-(self.max_turns * 2):]
        # 双重保障：轮数 + token 数，任一超限就继续截
        # while recent and self._count(result + recent) > self.max_tokens:
        #     recent = recent[2:]   # 每次移除最早一轮
        return system_mem + recent

    def chat(self, user_message: str) -> str:
        self.all_messages.append({"role": "user", "content": user_message})
        window = self._window()
        resp = client.chat.completions.create(
            model="qwen3.7-flash", messages=window
        )
        reply = resp.choices[0].message.content
        self.all_messages.append({"role": "assistant", "content": reply})
        return reply

def test_sliding_window():

    sliding_window_client = SlidingWindowMemory()
    print("=" * 50)
    print(sliding_window_client.chat("python是什么"))
    print("=" * 50)
    print(sliding_window_client.chat("除了数据计算，python能不能作为后台服务？为什么目前看着python做后台服务的很少"))
    print("=" * 50)
    # 体现记忆
    print(sliding_window_client.chat("能不能再向我详细解释一些"))

    print("=" * 50)
    print(sliding_window_client.chat("java是什么"))
    print("=" * 50)
    print(sliding_window_client.chat("java为什么成为了后台服务的主力？"))
    print("=" * 50)
    print(sliding_window_client.chat("能不能再向我详细解释一些"))
    print("=" * 50)

    print(sliding_window_client.chat("你之前向我解释过python么？"))

# 比如下列回答
"""
### 坦白说，**还没有专门为你详细拆解过 Python 的底层逻辑**。

在我们之前的对话中，我提到 Python 主要是作为“对比参照物”（比如在 `Java vs Python 核心差异` 表格里），或者在现代架构组合中提到它（“Java 做核心业务，Python 做数据与胶水层”）。但我们**没有深入剖析过 Python 自己的运行机制、并发模型和工程特性**。

既然我们已经把 Java 的“黑盒”（JVM/Spring/Loom）拆开了看，为了让你对现代后端技术栈的认知更完整，**我们现在就按同样的深度来拆解 Python**。

"""

if __name__ == "__main__":
    test_sliding_window()