from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.runnables import (
    RunnableParallel, RunnableLambda, RunnablePassthrough, RunnableBranch,
)
import os
import asyncio
from print_utils import print_separator


llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1")


# 顺序链：提示 → LLM → 解析
print_separator("顺序链")
translate_chain = ChatPromptTemplate.from_messages([
    ("system", "你是一位专业翻译，将文本翻译成{target_lang}"),
    ("human", "{text}")
]) | llm | StrOutputParser()

print(translate_chain.invoke({"text": "我今天很高兴，吃了一碗牛肉面", "target_lang": "zh"}))

# 并行链：同一输入同时跑多个独立分析，结果汇入字典
print_separator("并行链")
def analyze(text: str) -> dict:
    sentiment = (ChatPromptTemplate.from_messages([
        ("system", "只返回：正面/负面/中性"), ("human", "{text}")])
        | llm | StrOutputParser()).invoke({"text": text})
    summary = (ChatPromptTemplate.from_messages([
        ("system", "用一句话概括"), ("human", "{text}")])
        | llm | StrOutputParser()).invoke({"text": text})
    return {"情感": sentiment, "摘要": summary}

# 执行例子：输入一段具体文本，同时得到情感分析 + 一句话摘要
sample_text = "这家餐厅菜品味道不错，环境也安静，就是上菜有点慢。"
result = analyze(sample_text)
print(f"输入文本：{sample_text}")
print(f"情感分析：{result['情感']}")
print(f"一句话摘要：{result['摘要']}")

# 流式链：逐字返回
print_separator("逐字返回")
async def stream(text: str):
    chain = ChatPromptTemplate.from_messages([("system", "你是有帮助的助手"), ("human", "{q}")]) | llm | StrOutputParser()
    async for chunk in chain.astream({"q": text}):
        print(chunk, end="", flush=True)

# 执行例子：异步运行，终端里可以看到逐字输出的效果
asyncio.run(stream("请用一句话介绍 LangChain。"))
print()


# 条件链
print_separator("条件链")

# ① 意图分类：用 LLM 判断一句话属于哪类，返回标签词
def classify_intent(x: dict) -> str:
    intent_chain = ChatPromptTemplate.from_messages([
        ("system", "判断用户输入属于以下哪一类，只输出一个词：\n"
                   "技术问题 / 投诉 / 其他"),
        ("human", "{text}")
    ]) | llm | StrOutputParser()
    return intent_chain.invoke({"text": x["text"]})


# ② 三个分支：各自用不同的提示词处理
tech_chain = ChatPromptTemplate.from_messages([
    ("system", "你是资深技术支持，请给出清晰、步骤化的排查方案。"),
    ("human", "{text}")
]) | llm | StrOutputParser()

complaint_chain = ChatPromptTemplate.from_messages([
    ("system", "你是客服经理，先诚恳道歉，再说明补救方案。"),
    ("human", "{text}")
]) | llm | StrOutputParser()

default_chain = ChatPromptTemplate.from_messages([
    ("system", "你是贴心助手，友好自然地回复用户。"),
    ("human", "{text}")
]) | llm | StrOutputParser()


# ③ 先用一个 Lambda 把意图分类一次，挂到状态字典上
branch = (
    RunnableLambda(lambda x: {**x, "_intent": classify_intent(x)})
    | RunnableBranch(
        (lambda x: "技术问题" in x["_intent"], tech_chain),
        (lambda x: "投诉" in x["_intent"], complaint_chain),
        default_chain,  # 兜底分支
    )
)


# ④ 执行例子：输入三条不同意图的话，观察各自路由到哪个分支
print("--- 技术问题 ---")
print(branch.invoke({"text": "我的服务器一直返回 500 错误，该怎么排查？"}))

print("--- 投诉 ---")
print(branch.invoke({"text": "我要投诉！你们的产品昨天就坏了没人管！"}))

print("--- 其他（兜底分支） ---")
print(branch.invoke({"text": "今天天气真不错，适合出门走走。"}))