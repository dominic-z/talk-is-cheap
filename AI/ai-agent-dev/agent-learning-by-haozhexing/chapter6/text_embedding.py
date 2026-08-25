from openai import OpenAI
import numpy as np
from typing import List

import os
from openai import  OpenAI

client = OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )


# ============================
# OpenAI Embedding 模型
# ============================




def get_embedding(text: str, model: str = "text-embedding-v3") -> List[float]:
    """获取单个文本的嵌入向量"""
    # 清理文本
    text = text.replace("\n", " ").strip()
    if not text:
        raise ValueError("文本不能为空")

    response = client.embeddings.create(
        input=text,
        model=model
    )
    return response.data[0].embedding


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


def cosine_similarity(vec_a: List[float], vec_b: List[float]) -> float:
    """计算两个嵌入向量的余弦相似度（越接近 1 语义越相似）"""
    a, b = np.array(vec_a), np.array(vec_b)
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))


# ============================
# 执行 Demo（美观输出）
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


def print_vector(text: str, vec: List[float], preview: int = 5) -> None:
    """美观打印单条文本的嵌入向量信息"""
    preview_str = ", ".join(f"{v:+.4f}" for v in vec[:preview])
    print(f"\n✏️  文本: {text}")
    print(f"   📐 维度: {len(vec)}")
    print(f"   🔢 向量预览(前{preview}维): [{preview_str}, ...]")
    print(f"   📏 向量模长: {np.linalg.norm(vec):.4f}")


def similarity_badge(sim: float) -> str:
    """根据相似度返回彩色徽章"""
    if sim >= 0.8:
        return "🟢 高度相似"
    if sim >= 0.5:
        return "🟡 中度相似"
    return "🔴 低度相似"


def demo_single_embedding() -> None:
    """演示单条文本嵌入"""
    print_section("单条文本嵌入 (get_embedding)")
    text = "人工智能正在改变我们的生活方式"
    vec = get_embedding(text)
    print_vector(text, vec)


def demo_batch_embedding() -> None:
    """演示批量文本嵌入"""
    print_section("批量文本嵌入 (get_embeddings_batch)")
    texts = [
        "今天天气真好，适合出门散步",
        "Python 是一门广受欢迎的编程语言",
        "机器学习是人工智能的重要分支",
        "我喜欢在周末读一本好书",
    ]
    vecs = get_embeddings_batch(texts)
    print(f"\n✅ 共 {len(vecs)} 条文本完成嵌入，每条向量维度: {len(vecs[0])}")


def demo_similarity() -> None:
    """演示用嵌入向量计算语义相似度"""
    print_section("语义相似度计算 (cosine_similarity)")
    query = "如何学习机器学习？"
    candidates = [
        "怎样入门机器学习？",
        "今天中午吃什么比较合适？",
        "深度学习和神经网络有什么关系？",
    ]

    print(f"\n❓ 查询: {query}")
    print(LINE)

    query_vec = get_embedding(query)
    cand_vecs = get_embeddings_batch(candidates)

    results = []
    for text, vec in zip(candidates, cand_vecs):
        sim = cosine_similarity(query_vec, vec)
        results.append((sim, text))
        print(f"\n📄 候选: {text}")
        print(f"   📊 相似度: {sim:.4f}  {similarity_badge(sim)}")

    results.sort(reverse=True)
    print(f"\n{LINE}")
    print(f"🏆 最相似: {results[0][1]} (相似度 {results[0][0]:.4f})")


def run_demo() -> None:
    """运行完整演示"""
    print_header("🧬 文本嵌入 (Text Embedding) 演示")
    print(f"  模型: text-embedding-v3 (通义千问)")
    demo_single_embedding()
    demo_batch_embedding()
    demo_similarity()
    print_header("✨ 演示结束")


if __name__ == "__main__":
    run_demo()
