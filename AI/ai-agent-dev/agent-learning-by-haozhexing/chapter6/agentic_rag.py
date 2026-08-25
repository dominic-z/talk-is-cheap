import os
import re
import json
from openai import OpenAI

# 加载 .env 文件中的环境变量（如 QWEN_API_KEY），避免把密钥硬编码在代码里。
# 如果当前环境没有 .env 文件，只要系统环境变量里已配置 QWEN_API_KEY 即可。
from dotenv import load_dotenv
load_dotenv()

client = OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )

# ── 组件 1：检索决策器 ───────────────────────────────────────────
def should_retrieve(question: str, history: list[dict]) -> bool:
    """判断当前问题是否需要检索。"""
    prompt = f"""判断以下问题是否需要查外部文档。
【不需要】简单计算、通用常识、问题已在对话历史中回答过
【需要】涉及特定领域/公司内部/时效性信息、需要精确数据或引用
对话历史：{json.dumps(history[-3:], ensure_ascii=False)}
问题：{question}
只回复 YES/NO。"""
    resp = client.chat.completions.create(
        model="qwen3.7-flash",         # 小模型做判断即可
        messages=[{"role": "user", "content": prompt}],
        max_tokens=5, temperature=0
    )
    return resp.choices[0].message.content.strip().upper() == "YES"


# ── 组件 2：查询改写器 ───────────────────────────────────────────
def rewrite_query(question: str, context: str = "") -> list[str]:
    """改写为 2-3 个更利于检索的查询变体。"""
    prompt = f"""将问题改写为 2-3 个检索查询变体。
要求：去掉口语化、覆盖不同侧面、每行一个、无编号。
背景：{context}
问题：{question}"""
    resp = client.chat.completions.create(
        model="qwen3.7-flash",
        messages=[{"role": "user", "content": prompt}],
        max_tokens=200, temperature=0.3
    )
    return [q.strip() for q in resp.choices[0].message.content.split("\n") if q.strip()]


# ── 组件 3：检索质量评估器 ───────────────────────────────────────
def evaluate_retrieval(question: str, docs: list[str]) -> dict:
    """评估检索结果是否足以回答问题，返回 {relevance, sufficiency, missing}。"""
    prompt = f"""评估检索结果是否足以回答问题。
问题：{question}
文档：{chr(10).join(docs[:5])}
返回 JSON：{{"relevance": 0-10, "sufficiency": bool, "missing": "缺失信息"}}"""
    resp = client.chat.completions.create(
        model="qwen3.7-flash",
        messages=[{"role": "user", "content": prompt}],
        response_format={"type": "json_object"}, max_tokens=200
    )
    return json.loads(resp.choices[0].message.content)


# ── 组件 4：带引用的答案生成器 ───────────────────────────────────
def generate_with_citation(question: str, docs: list[dict]) -> dict:
    """基于检索文档生成带 [数字] 引用的答案。"""
    docs_text = "\n".join(
        f"[{i}] {d['source']} p.{d.get('page','?')}\n{d['content']}"
        for i, d in enumerate(docs, 1)
    )
    prompt = f"""基于以下参考文档回答问题。
要求：引用时用 [数字] 标注；文档不足时明确说明；不凭空添加信息。
{docs_text}
问题：{question}"""
    resp = client.chat.completions.create(
        model="qwen3.7-flash", messages=[{"role": "user", "content": prompt}], max_tokens=1000
    )
    return {
        "answer": resp.choices[0].message.content,
        "sources": [f"{d['source']} p.{d.get('page','?')}" for d in docs]
    }


# ── 组件 5：检索器 ───────────────────────────────────────────────
# 上面的 4 个组件只覆盖了"决策/改写/评估/生成"，还缺一个真正的检索步骤。
# 真实系统里这里会接向量库或混合检索器（参考本章的 vector_store.py /
# hybrid_retriever.py），demo 里用一个简单的关键词打分来模拟。
def retrieve(query: str, kb: list[dict], n: int = 3) -> list[dict]:
    """模拟检索：用二元组（bigram）命中数给文档打分，返回 top-n。

    用二元组而非按空格分词，是因为中文词语之间没有空格，
    按空格切分会导致整个句子变成一个 term 而无法命中。
    """
    text = re.sub(r"[\s？?。、，,]+", "", query.lower())
    terms = {text[i:i + 2] for i in range(len(text) - 1)}
    scored = []
    for doc in kb:
        content_lower = doc["content"].lower()
        score = sum(1 for t in terms if t in content_lower)
        if score > 0:
            scored.append({**doc, "_score": score})
    scored.sort(key=lambda d: d["_score"], reverse=True)
    return scored[:n]


# ── Agentic RAG 主流程 ──────────────────────────────────────────
#
# Agentic RAG 的核心思路（对比朴素 RAG）：
#   朴素 RAG：检索 → 直接生成。检索结果烂不烂，模型照单全收。
#   Agentic RAG：让 LLM 以"智能体"的身份参与 RAG 的每个环节——
#     1. 先判断"要不要检索"（闲聊/常识题直接答，省一次检索）；
#     2. 检索前把问题改写成多个查询变体，提高召回；
#     3. 检索后**先评估检索结果的质量**（相关性/充分性），
#        不够就带着"缺了什么"的反馈改写查询、重新检索；
#     4. 只有质量过关后才基于文档生成带引用的答案。
#   即用户说的：回答之前先让 LLM 看看 RAG 检索回来的信息质量怎样，
#   不合格就打回重检索，形成"检索 → 评估 → 重试"的闭环。
def agentic_rag_pipeline(question: str, kb: list[dict],
                         history: list[dict] = None, max_retries: int = 2) -> dict:
    """完整的 Agentic RAG 流程，返回 {answer, sources, trace}。"""
    history = history or []
    trace = {"question": question}

    # 第 1 步：检索决策——不需要检索就直接生成答案，跳过整个检索链路
    if not should_retrieve(question, history):
        print("[Agentic RAG] 判定无需检索，直接生成答案")
        result = generate_with_citation(question, [])
        return {**result, "trace": {**trace, "retrieval": "skipped"}}

    # 第 2 步：查询改写——得到多个检索变体，扩大召回面
    variants = rewrite_query(question)
    print(f"[Agentic RAG] 查询改写为 {len(variants)} 个变体：{variants}")
    trace["rewritten_queries"] = variants

    # 第 3 步：检索 + 质量评估 + 重试 的闭环（最多重试 max_retries 次）
    docs, evaluation = [], None
    for attempt in range(max_retries + 1):
        docs = retrieve(" ".join(variants), kb)
        print(f"[Agentic RAG] 第 {attempt + 1} 轮检索命中 {len(docs)} 篇文档")
        evaluation = evaluate_retrieval(question, [d["content"] for d in docs])
        print(f"[Agentic RAG] 质量评估：{evaluation}")

        if evaluation.get("sufficiency"):
            break  # 质量过关，进入生成阶段
        if attempt < max_retries:
            # 质量不足：把"缺失信息"作为背景反馈，改写查询再检索一轮
            feedback = f"上一轮检索缺失：{evaluation.get('missing', '未知')}"
            variants = rewrite_query(question, context=feedback)
            print(f"[Agentic RAG] 质量不足，带反馈重新改写：{variants}")
    trace["retrieval_evaluation"] = evaluation

    # 第 4 步：基于检索文档生成带引用的最终答案（即使文档不足也生成，
    # generate_with_citation 的 prompt 已要求模型在文档不足时明确说明）
    result = generate_with_citation(question, docs)
    return {**result, "trace": trace}


# ── 测试 demo ────────────────────────────────────────────────────
def main():
    # 模拟知识库：真实场景中应替换为向量库/混合检索器的检索结果，
    # 每条文档包含 source（来源）、page（页码）、content（内容）
    kb = [
        {"source": "rag_intro.md", "page": 1,
         "content": "RAG 即检索增强生成：先从知识库检索相关内容，再交给大模型生成回答。"},
        {"source": "rag_intro.md", "page": 2,
         "content": "朴素 RAG 的检索结果无论质量如何都会直接喂给模型，容易产生幻觉。"},
        {"source": "agentic_rag.md", "page": 3,
         "content": "Agentic RAG 让 LLM 作为智能体参与检索决策、查询改写、质量评估与重试。"},
        {"source": "agentic_rag.md", "page": 4,
         "content": "检索质量评估器会对检索结果打分，分数不足时改写查询并重新检索。"},
        # 公司内部文档：模型不可能凭空知道，能确保触发"需要检索"分支，
        # 完整走一遍 检索 → 评估 → 生成 的链路，适合演示流程。
        {"source": "company_policy.md", "page": 1,
         "content": "公司 2026 年差旅报销制度：市内出租车每日报销上限为 120 元，超出部分自理。"},
        {"source": "company_policy.md", "page": 2,
         "content": "出差住宿标准：一线城市每晚不超过 600 元，其他城市不超过 450 元。"},
    ]

    # 测试用例 1：公司内部制度问题，模型无法凭空回答，会被判定为需要检索，
    # 完整走 检索 → 质量评估 → 生成 的流程（检索质量够好时一轮通过）
    q1 = "公司差旅出租车每日报销上限是多少？"
    print("=" * 60)
    print(f"问题 1：{q1}")
    result1 = agentic_rag_pipeline(q1, kb)
    print(f"答案 1：{result1['answer']}")
    print(f"引用来源：{result1['sources']}")

    # 测试用例 2：简单计算题，应在"检索决策"阶段就被判定为无需检索，
    # 直接生成答案，跳过后续所有检索环节（省时间也省 token）
    q2 = "1+1 等于几？"
    print("=" * 60)
    print(f"问题 2：{q2}")
    result2 = agentic_rag_pipeline(q2, kb, history=[
        {"role": "user", "content": q2}
    ])
    print(f"答案 2：{result2['answer']}")
    print("=" * 60)


if __name__ == "__main__":
    main()
