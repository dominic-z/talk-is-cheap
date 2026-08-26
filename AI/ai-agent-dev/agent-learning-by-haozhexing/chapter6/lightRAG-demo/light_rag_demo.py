"""LightRAG 运行 Demo（基于通义千问 Qwen）

LightRAG 是港大开源的"知识图谱 + RAG"框架：
  1. 索引阶段：调用 LLM 从文档中抽取实体/关系，构建知识图谱，
     并生成向量索引与关键词索引；
  2. 查询阶段：支持 naive / local / global / hybrid / mix 等多种检索模式。

运行前准备：
  1. 安装依赖：pip install "lightrag-hku" python-dotenv
  2. 配置环境变量 QWEN_API_KEY（阿里云百炼/DashScope 的 API Key），
     或在项目目录放置 .env 文件（参考 chapter6/agentic_rag.py 的做法）。

运行方式：
  python light_rag_demo.py

首次运行会构建索引（调用多次 LLM 做实体抽取，耗时约几分钟），
索引数据缓存在本目录下的 rag_storage/ 中，重复运行不会重建。
"""

import asyncio
import os
from functools import partial
from pathlib import Path

from dotenv import load_dotenv
from lightrag import LightRAG, QueryParam
from lightrag.llm.openai import openai_complete_if_cache, openai_embed
from lightrag.utils import EmbeddingFunc

# 加载 .env 文件中的环境变量（如 QWEN_API_KEY），与 chapter6 其他脚本保持一致
load_dotenv()

# ── 模型配置（参考 agentic_rag.py 的 DashScope OpenAI 兼容模式） ───────
DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1"
API_KEY = os.getenv("QWEN_API_KEY")

# 实体抽取/答案生成用的对话模型，与 chapter6 其他脚本保持一致；
# DashScope 上也可按需替换为 qwen-plus / qwen-max / qwen-turbo 等
LLM_MODEL = "qwen3.7-flash"

# Embedding 模型与 chapter6/text_embedding.py 保持一致。
# text-embedding-v3 默认输出 1024 维向量，必须与 EmbeddingFunc 的
# embedding_dim 一致，否则向量存储/检索会报错。
EMBED_MODEL = "text-embedding-v3"
EMBED_DIM = 1024

# 当前脚本所在目录（数据文件与索引存储都放在这里）
BASE_DIR = Path(__file__).resolve().parent
DATA_DIR = BASE_DIR / "data"
WORKING_DIR = BASE_DIR/"git_ignore" / "rag_storage"


async def build_rag() -> LightRAG:
    """创建并初始化 LightRAG 实例：接入 Qwen 做 LLM 与 Embedding。

    注意：1.5.x 版本必须先 await rag.initialize_storages() 初始化
    pipeline 状态，否则 insert 会报 PipelineNotInitializedError。
    """
    rag = LightRAG(
        working_dir=str(WORKING_DIR),
        # LLM：用于实体/关系抽取、关键词提取与最终答案生成。
        # openai_complete_if_cache 是 LightRAG 内置的 OpenAI 兼容调用封装，
        # 带缓存与失败重试，支持任意 OpenAI 兼容端点（这里是 DashScope）。
        llm_model_func=partial(
            openai_complete_if_cache,
            LLM_MODEL,
            api_key=API_KEY,
            base_url=DASHSCOPE_BASE_URL,
        ),
        # Embedding：用于实体/关系/文本块的向量化。
        # 注意：openai_embed 本身是带默认 1536 维校验的 EmbeddingFunc 实例，
        # 若直接嵌套会在内层按 1536 维校验而报维度不匹配，
        # 因此这里取它内部的原始异步函数 .func，由外层 EmbeddingFunc 统一控制维度。
        embedding_func=EmbeddingFunc(
            embedding_dim=EMBED_DIM,
            max_token_size=8192,
            func=partial(
                openai_embed.func,
                model=EMBED_MODEL,
                api_key=API_KEY,
                base_url=DASHSCOPE_BASE_URL,
            ),
        ),
    )
    await rag.initialize_storages()
    return rag


async def insert_documents(rag: LightRAG) -> None:
    """把 data/ 目录下的所有 .txt 文件插入知识库（增量插入，已插入的会跳过）。"""
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    files = sorted(DATA_DIR.glob("*.txt"))
    if not files:
        raise FileNotFoundError(f"{DATA_DIR} 下没有 .txt 文件，请先创建文本数据")

    for fp in files:
        text = fp.read_text(encoding="utf-8")
        print(f"[insert] 正在索引：{fp.name}（{len(text)} 字）...")
        # file_paths 用于记录文档来源，便于查询结果溯源；
        # ainsert 是异步版插入（同步版 insert 在未初始化 pipeline 时不可用）
        await rag.ainsert(text, file_paths=[str(fp)])
        print(f"[insert] {fp.name} 索引完成")


def print_graph_stats(rag: LightRAG, node_samples: int = 3, edge_samples: int = 3) -> None:
    """打印知识图谱的规模（实体数/关系数）及节点/边样例。

    通过样例可以直观看到 LightRAG 具体抽取了哪些结构化信息：
      - 节点（实体）：名称、类型、LLM 生成的描述、来源文档；
      - 边（关系）：头尾实体、关系关键词、关系描述。
    """
    double_line = "═" * 60
    line = "─" * 60
    try:
        graph = rag.chunk_entity_relation_graph._graph  # NetworkXStorage 内部图对象
    except Exception as e:  # 图存储未就绪时不影响主流程
        print(f"[graph] 暂无法读取图谱统计信息：{e}")
        return

    print(f"\n{double_line}")
    print("  🕸️  知识图谱抽取结果一览")
    print(double_line)
    print(f"  📊 图谱规模：{graph.number_of_nodes()} 个实体节点，"
          f"{graph.number_of_edges()} 条关系边")

    # 节点 = 实体：LLM 从原文中抽取出的实体名、类型与描述，
    # source_id/file_path 记录了它来自哪个文档块，便于溯源
    print(f"\n{line}")
    print(f"  🔵 实体节点样例（前 {node_samples} 个）：")
    for name, attr in list(graph.nodes(data=True))[:node_samples]:
        print(f"\n  📌 实体：{name}")
        print(f"     🏷️  类型：{attr.get('entity_type', '?')}")
        print(f"     📝 描述：{attr.get('description', '')}")
        print(f"     📄 来源：{attr.get('file_path', '?')}")

    # 边 = 关系：连接两个实体，附带 LLM 总结的关系描述与关键词，
    # 查询时 local/global 模式就是在这张图上做检索与扩展的
    print(f"\n{line}")
    print(f"  🔗 关系边样例（前 {edge_samples} 条）：")
    for src, dst, attr in list(graph.edges(data=True))[:edge_samples]:
        print(f"\n  📌 关系：{src} ——> {dst}")
        print(f"     🏷️  关键词：{attr.get('keywords', '?')}")
        print(f"     📝 描述：{attr.get('description', '')}")
        print(f"     ⚖️  权重：{attr.get('weight', '?')}")

    print(double_line)


async def query_demo(rag: LightRAG) -> None:
    """用同一个问题对比不同检索模式的答案。

    mode 说明：
      naive  - 传统向量块检索（对照基线）
      local  - 围绕具体实体的细粒度检索
      global - 基于高层关键词的全局主题检索
      hybrid - local + global 混合检索（推荐）
    """
    questions = [
        # 虚构内容：模型不可能凭空知道，答案必然来自知识库检索
        "星尘旅行社的'极光号'行程定价是多少？包含哪些服务？",
        # 宏观总结类问题，适合考察 global 检索路径
        "LightRAG 支持哪些检索模式？各自适合什么场景？",
    ]
    modes = ["naive", "local", "global", "hybrid"]

    for question in questions:
        print("=" * 70)
        print(f"问题：{question}")
        for mode in modes:
            print("-" * 70)
            print(f"[{mode}] 模式：")
            answer = await rag.aquery(question, param=QueryParam(mode=mode))
            print(answer)


async def main():
    if not API_KEY:
        raise EnvironmentError(
            "未找到环境变量 QWEN_API_KEY，请先配置（参考 agentic_rag.py）"
        )

    rag = await build_rag()

    # 第 1 步：插入文本数据，构建知识图谱索引（二次运行会增量跳过）
    await insert_documents(rag)
    print_graph_stats(rag)

    # 第 2 步：多模式查询演示
    await query_demo(rag)


if __name__ == "__main__":
    asyncio.run(main())
