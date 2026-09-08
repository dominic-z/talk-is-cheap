from langchain_core.runnables import RunnablePassthrough, RunnableParallel, RunnableLambda
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

import json
import os

from print_utils import print_separator
from langchain_openai import ChatOpenAI

llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
                 temperature=0)

# 基础链：提示 → 模型 → 解析
print_separator("# 基础链：提示 → 模型 → 解析")
chain = ChatPromptTemplate.from_messages([("human", "{question}")]) | llm | StrOutputParser()
print(chain.invoke({"question": "请用一句话解释什么是 LCEL？"}))

# RunnableParallel：同时保留"检索到的上下文"和"原问题"
print_separator(""" 
# RunnableParallel：同时保留"检索到的上下文"和"原问题" 
""")

# 模拟一个"文档库" + 简易检索函数（真实项目通常替换成向量数据库 / 搜索 API）
DOC_STORE = {
    "langchain": "LangChain 是一个用于构建大模型应用的开发框架，"
                  "核心抽象包括 LCEL、提示模板、输出解析器、工具、记忆等。",
    "lcel": "LCEL（LangChain Expression Language）用 | 符号把各 Runnable 拼成管道，"
            "天然支持并行、流式、批处理与异步调用。",
    "runnable": "Runnable 是 LCEL 中最基本的可调用单元，"
                "可用 .invoke() 同步调用、.ainvoke() 异步调用、.batch() 批量调用、.stream() 流式调用。",
}


def retrieve(query: str) -> str:
    """按关键词在模拟文档库中做一次检索，返回命中的上下文；无命中则返回全部文档。"""
    query = query.lower()
    hits = [text for keyword, text in DOC_STORE.items() if keyword in query]
    return "；".join(hits) if hits else "；".join(DOC_STORE.values())


rag_chain = (
        RunnableParallel({"context": lambda x: retrieve(x["question"]),
                          "question": lambda x: x["question"]})
        | ChatPromptTemplate.from_messages([("system", "基于上下文回答：{context}"),
                                            ("human", "{question}")])
        | llm | StrOutputParser()
)
print(rag_chain.invoke({"question": "什么是 LCEL？它支持哪些调用方式？"}))

# RunnableLambda：把普通函数嵌入管道
print_separator("# RunnableLambda：把普通函数嵌入管道")
json_chain = (ChatPromptTemplate.from_messages([("system", "转JSON，含title和priority"),
                                                ("human", "{description}")])
              | llm | StrOutputParser()
              | RunnableLambda(lambda text: json.loads(text[text.find("{"):text.rfind("}") + 1])))

# 执行例子：把一句自然语言任务描述解析成 {title, priority} 的字典
description = "明天下午三点，在三楼会议室和算法组评审召回率优化方案"
task = json_chain.invoke({"description": description})
print(f"输入描述：{description}")
print(f"解析出的任务：{task}")

# RunnablePassthrough：把输入原样透传给下游；也常用作 dict 的值，
# 让"原始输入"（如 question）和加工结果（如 context）一起传给 prompt
print_separator("# RunnablePassthrough：原样透传 / .assign() 在透传时附加新字段")

passthrough_chain = (
    # 输入是普通字符串时，dict 的每个值都会收到这份完整输入：
    #   context ← retrieve(输入) 加工，question ← RunnablePassthrough() 原样透传
    # 小知识点：普通 dict 也能出现在 | 左边，是因为 dict.__or__ 只认另一个 dict、
    # 遇到 Runnable 会返回 NotImplemented，Python 便转去调用右侧 Runnable 的
    # 反向运算符 __ror__，LangChain 借此把该 dict 强转成 RunnableParallel 再接进管道。
    {"context": retrieve, "question": RunnablePassthrough()}
    | ChatPromptTemplate.from_messages([("system", "基于上下文回答：{context}"),
                                        ("human", "用户提问：{question}")])
    | llm
    | StrOutputParser()
)
# invoke的内容，就是下面这段话，会作为参数传递给“{"context": retrieve, "question": RunnablePassthrough()}”这个东西。
# 他的输出又会作为后续ChatPromptTemplate的输入，ChatPromptTemplate会读取到context或者question
print(passthrough_chain.invoke("什么是 Runnable？它支持哪些调用方式？"))

# .assign()：透传输入字典的同时，用 lambda 追加新字段（不会覆盖已有字段）
# 这个时候，assign_chain本质上就类似于一个数据存储器，
# 当assign_chain.invoke的时候，他会存储这些入参“{"question": "LCEL 是什么？", "user": "张三"}”
# 同时会新增一个context并存储下来，用于后续的输入。
assign_chain = RunnablePassthrough.assign(
    context=lambda x: retrieve(x["question"]),  # 把检索结果挂到新增的 context 字段上
)
assigned = assign_chain.invoke({"question": "LCEL 是什么？", "user": "张三"})
print(f"assign 后保留下来的原字段：question={assigned['question']!r}, user={assigned['user']!r}")
print(f"新增的 context 字段（截断显示）：{assigned['context'][:60]}…")
