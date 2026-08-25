from openai import OpenAI
import chromadb
from rank_bm25 import BM25Okapi  # pip install rank-bm25
import jieba  # pip install jieba (中文分词)
import numpy as np
from typing import List

import os
from openai import  OpenAI
client = OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )

class HybridRetriever:
    """混合检索：向量相似度 + BM25 关键词匹配"""

    def __init__(self, collection, documents: List[str]):
        self.collection = collection
        self.documents = documents

        # 初始化 BM25（基于词频的关键词检索）
        tokenized_docs = [list(jieba.cut(doc)) for doc in documents]
        self.bm25 = BM25Okapi(tokenized_docs)

    def _vector_search(self, query: str, n: int = 10) -> dict:
        """向量语义搜索"""
        from openai import OpenAI
        response = client.embeddings.create(
            input=query,
            model="text-embedding-v3"
        )
        query_embedding = response.data[0].embedding

        results = self.collection.query(
            query_embeddings=[query_embedding],
            n_results=min(n, self.collection.count()),
            include=["documents", "distances"]
        )

        scores = {}
        if results["documents"] and results["documents"][0]:
            for doc_id, doc, dist in zip(
                    results["ids"][0],
                    results["documents"][0],
                    results["distances"][0]
            ):
                scores[doc_id] = {
                    "document": doc,
                    "vector_score": 1 - dist
                }
        return scores

    def _keyword_search(self, query: str, n: int = 10) -> dict:
        """BM25 关键词搜索"""
        tokens = list(jieba.cut(query))
        scores = self.bm25.get_scores(tokens)

        # 标准化分数
        max_score = max(scores) if max(scores) > 0 else 1

        results = {}
        top_indices = np.argsort(scores)[::-1][:n]

        for idx in top_indices:
            if scores[idx] > 0:
                results[f"doc_{idx}"] = {
                    "document": self.documents[idx],
                    "keyword_score": scores[idx] / max_score,
                    "doc_idx": idx
                }

        return results

    def retrieve(self, query: str, n: int = 5,
                 vector_weight: float = 0.7, keyword_weight: float = 0.3) -> List[dict]:
        """
        混合检索，融合两种检索结果

        Args:
            vector_weight: 向量分数权重（0-1）
            keyword_weight: 关键词分数权重（0-1）
        """
        vector_results = self._vector_search(query, n=n * 2)
        keyword_results = self._keyword_search(query, n=n * 2)

        # 融合分数
        combined = {}

        for doc_id, data in vector_results.items():
            combined[doc_id] = {
                "document": data["document"],
                "vector_score": data["vector_score"],
                "keyword_score": 0,
                "combined_score": data["vector_score"] * vector_weight
            }

        for doc_id, data in keyword_results.items():
            if doc_id in combined:
                combined[doc_id]["keyword_score"] = data["keyword_score"]
                combined[doc_id]["combined_score"] += data["keyword_score"] * keyword_weight
            else:
                combined[doc_id] = {
                    "document": data["document"],
                    "vector_score": 0,
                    "keyword_score": data["keyword_score"],
                    "combined_score": data["keyword_score"] * keyword_weight
                }

        # 按综合分数排序
        sorted_results = sorted(
            combined.values(),
            key=lambda x: x["combined_score"],
            reverse=True
        )

        return sorted_results[:n]



# ============================
# 美观输出工具
# ============================

LINE = "─" * 60
DOUBLE_LINE = "═" * 60


def print_header(title: str) -> None:
    """打印双线框标题"""
    print(f"\n{DOUBLE_LINE}")
    print(f"  {title}")
    print(DOUBLE_LINE)


def print_section(title: str) -> None:
    """打印小节标题"""
    print(f"\n{LINE}")
    print(f"📌 {title}")
    print(LINE)


def print_question(query: str) -> None:
    """打印查询问题"""
    print(f"\n{LINE}")
    print(f"❓ 查询: {query}")
    print(LINE)


def score_badge(score: float) -> str:
    """根据综合分数返回彩色徽章"""
    if score >= 0.6:
        return "🟢 高相关"
    if score >= 0.3:
        return "🟡 中相关"
    return "🔴 低相关"


def print_results(results: list) -> None:
    """美观打印检索结果"""
    if not results:
        print("\n😶 未检索到相关文档")
        return

    for rank, item in enumerate(results, 1):
        medal = "🏆" if rank == 1 else f" {rank}."
        print(f"\n{medal} [{score_badge(item['combined_score'])}] {item['document']}")
        print(f"    🧮 综合分: {item['combined_score']:.4f}")
        print(f"    🧬 向量分: {item['vector_score']:.4f}  |  🔑 关键词分: {item['keyword_score']:.4f}")


# ============================
# 知识库准备
# ============================

KNOWLEDGE_BASE = [
    "Python 是由 Guido van Rossum 创建，1991年首次发布的通用编程语言。",
    "FastAPI 是一个现代、高性能的 Python Web 框架，基于类型注解自动校验请求参数。",
    "LangChain 是一个用于构建大模型应用的框架，提供工具链、Agent 和 RAG 等组件。",
    "向量数据库通过嵌入向量存储和检索语义相似的文档，是 RAG 系统的核心组件。",
    "BM25 是一种经典的关键词检索算法，基于词频和逆文档频率衡量文档相关性。",
    "混合检索结合语义检索与关键词检索的优势，能显著提升 RAG 的召回质量。",
    "ChromaDB 是一个轻量级的开源向量数据库，支持内存模式和持久化存储。",
    "RAG 即检索增强生成，先从知识库检索相关内容，再交给大模型生成回答。",
]


def get_embeddings_batch(texts: List[str], model: str = "text-embedding-v3",
                         batch_size: int = 100) -> List[List[float]]:
    """批量获取嵌入（减少 API 调用次数）"""
    all_embeddings = []

    for i in range(0, len(texts), batch_size):
        batch = texts[i:i + batch_size]
        # 清理文本
        batch = [t.replace("\n", " ").strip() for t in batch]

        response = client.embeddings.create(
            input=batch,
            model=model
        )

        embeddings = [item.embedding for item in response.data]
        all_embeddings.extend(embeddings)

        print(f"  批次 {i // batch_size + 1}: {len(batch)} 个文本已嵌入")

    return all_embeddings


def build_retriever() -> HybridRetriever:
    """构建知识库并初始化混合检索器"""
    print_section("构建知识库")
    print(f"\n📚 共 {len(KNOWLEDGE_BASE)} 篇文档：")
    for i, doc in enumerate(KNOWLEDGE_BASE):
        print(f"   {i}. {doc}")

    # 内存版 ChromaDB，测试用、不落盘
    client = chromadb.EphemeralClient()
    collection = client.create_collection(
        name="hybrid_demo",
        metadata={"hnsw:space": "cosine"}
    )

    print("\n⏳ 正在生成文档向量...")
    embeddings = get_embeddings_batch(KNOWLEDGE_BASE)

    # 使用 doc_{idx} 作为 ID，与 BM25 侧的索引 ID 保持一致，便于分数融合
    ids = [f"doc_{i}" for i in range(len(KNOWLEDGE_BASE))]
    collection.add(
        ids=ids,
        documents=KNOWLEDGE_BASE,
        embeddings=embeddings,
    )
    print(f"✅ 向量库构建完成，共 {collection.count()} 篇文档")

    return HybridRetriever(collection, KNOWLEDGE_BASE)


# ============================
# 测试用例
# ============================

def demo_keyword_query(retriever: HybridRetriever) -> None:
    """关键词命中型查询：BM25 应发挥较大作用"""
    print_section("测试一：关键词精确命中")
    query = "BM25 算法是什么？"
    print_question(query)
    results = retriever.retrieve(query, n=3)
    print_results(results)


def demo_semantic_query(retriever: HybridRetriever) -> None:
    """语义型查询：查询词与文档字面不重合，依赖向量语义召回"""
    print_section("测试二：语义相似（字面不重合）")
    query = "怎么让大模型回答时参考外部资料？"
    print_question(query)
    results = retriever.retrieve(query, n=3)
    print_results(results)


def demo_weight_comparison(retriever: HybridRetriever) -> None:
    """同一查询在不同权重配比下的结果对比"""
    print_section("测试三：权重配比对比")
    query = "轻量级开源向量数据库有哪些"

    weight_plans = [
        ("纯向量", 1.0, 0.0),
        ("向量为主", 0.7, 0.3),
        ("关键词为主", 0.3, 0.7),
    ]

    for name, vw, kw in weight_plans:
        print(f"\n⚖️  策略 [{name}]  vector_weight={vw}, keyword_weight={kw}")
        results = retriever.retrieve(query, n=2, vector_weight=vw, keyword_weight=kw)
        for rank, item in enumerate(results, 1):
            print(f"   {rank}. [{item['combined_score']:.4f}] {item['document'][:50]}")


def run_demo() -> None:
    """运行完整演示"""
    print_header("🔀 混合检索 (Hybrid Retrieval) 演示")
    retriever = build_retriever()
    demo_keyword_query(retriever)
    demo_semantic_query(retriever)
    demo_weight_comparison(retriever)
    print_header("✨ 演示结束")


if __name__ == "__main__":
    run_demo()
