import chromadb
from datetime import datetime

from openai import OpenAI
import os

client = OpenAI(
  api_key=os.getenv("QWEN_API_KEY"),
  base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)

# https://platform.qianwenai.com/docs/api-reference/text-embedding/openai-embedding
def get_embedding(text: str, model: str = "text-embedding-v4") -> list[float]:
    """获取文本的向量嵌入（1536 维）"""
    response = client.embeddings.create(input=text, model=model)
    return response.data[0].embedding


class LongTermMemory:
    """基于 ChromaDB 的长期记忆系统：每用户独立 collection。"""

    # 这玩意本地就能跑
    def __init__(self, user_id: str, persist_dir: str = "/tmp/chroma_memory_db"):
        self.user_id = user_id
        self.client = chromadb.PersistentClient(path=persist_dir)
        # 每个用户独立 collection：避免不同用户的记忆互相干扰
        self.collection = self.client.get_or_create_collection(
            name=f"user_{user_id}_memory",
            metadata={"hnsw:space": "cosine"}     # 用余弦相似度
        )

    def add(self, content: str, type: str = "general",
            importance: int = 5, source: str = "conversation") -> str:
        """把一段文本向量化后写入数据库，返回记忆 ID。"""
        import uuid
        memory_id = str(uuid.uuid4())
        self.collection.add(
            ids=[memory_id],
            embeddings=[get_embedding(content)],
            documents=[content],
            metadatas=[{
                "type": type, "importance": importance,
                "source": source, "user_id": self.user_id,
                "created_at": datetime.now().isoformat()
            }]
        )
        return memory_id

    def search(self, query: str, n: int = 5,
               type: str | None = None, min_importance: int = 1) -> list[dict]:
        """按查询语义找最相关的记忆。"""
        where = {"user_id": self.user_id}
        if type:
            where["type"] = type
        if min_importance > 1:
            where["importance"] = {"$gte": min_importance}

        results = self.collection.query(
            query_embeddings=[get_embedding(query)],
            n_results=min(n, self.collection.count()),
            where=where,
            include=["documents", "metadatas", "distances"]
        )
        return [
            {"content": d, "type": m.get("type"),
             "importance": m.get("importance"), "relevance": 1 - dist}
            for d, m, dist in zip(
                results["documents"][0], results["metadatas"][0],
                results["distances"][0])
        ]


if __name__ == "__main__":
    long_term_memory = LongTermMemory("uid1")

    texts = [
        "Python 是一种编程语言",  # 原始句
        "Python 是用于编程的语言",  # 语义相似
        "Python 主要用于科学计算",  # 语义相似
        "Java 主要用于后台服务",  # 语义相似
        "用户很喜欢周五",  # 语义相似
        "今天天气很好",  # 语义不相关
        "用户喜欢在周末出去玩",  # 语义不相关
        "用户希望每周能去山上转转",  # 语义不相关
    ]
    for text in texts:
        long_term_memory.add(text, type="general")

    print(long_term_memory.search(texts[0]))
    print(long_term_memory.search(texts[-1]))
