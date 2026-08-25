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
