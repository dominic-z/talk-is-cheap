# pip install letta letta-client
# 运行前先启动本地服务：letta server（默认 http://localhost:8283）

#  没跑通，不想跑个docker了
import os

from letta_client import Letta

# 通义千问（DashScope）的 OpenAI 兼容端点
QWEN_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"
model_name = "qwen3.7-flash"

# 新版 Letta 已移除 create_client，改用独立的 letta-client 连接本地服务
letta_client = Letta(base_url="http://localhost:8283")

qwen_endpoint = dict(
    model_endpoint_type="openai",
    model_endpoint=QWEN_BASE_URL,
    api_key=os.getenv("QWEN_API_KEY"),
)

# 创建一个带分层记忆的 Agent（LLM 与 Embedding 均走千问）
agent = letta_client.agents.create(
    name="memory_assistant",
    memory_blocks=[
        {"label": "persona", "value": "你是一个有帮助的AI助手，善于记住用户信息。"},
        {"label": "human",   "value": "用户信息待填写"},   # Agent 会自动更新
    ],
    llm_config={**qwen_endpoint, "model": model_name, "context_window": 8192},
    embedding_config={**qwen_endpoint, "model": "text-embedding-v3", "embedding_dim": 1024},
)


def chat(text: str) -> None:
    response = letta_client.agents.messages.create(
        agent_id=agent.id,
        messages=[{"role": "user", "content": text}],
    )
    print(response.messages[-1].content)


# 与 Agent 对话
chat("你好！我叫小红，我在做 NLP 研究")

# 验证记忆是否已更新：读取 Agent 当前的记忆块
state = letta_client.agents.retrieve(agent_id=agent.id)
for block in state.memory.blocks:
    print(f"\n【记忆块 {block.label}】\n{block.value}")

# 追问一句，验证 Agent 能从记忆中召回信息
chat("我叫什么名字？我在做什么研究？")
