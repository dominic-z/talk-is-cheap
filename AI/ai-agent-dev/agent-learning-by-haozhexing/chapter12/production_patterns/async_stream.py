import asyncio
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
import os

llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
                 streaming=True)


prompt = ChatPromptTemplate.from_messages([
    ("system", "你是一个专业的技术顾问。"),
    ("human", "{question}")
])
chain = prompt | llm | StrOutputParser()

"""
async 省下的不是「把线程还给操作系统去跑 CPU 密集任务」，
而是不让一个线程在等待网络 I/O 时被白白占住。LLM 调用是典型的 I/O 密集（99% 时间在等 DashScope 返回），
async 让同一个线程在等待期间去推进其他请求。
"""
async def stream_response(question: str):
    """异步流式输出"""
    async for chunk in chain.astream({"question": question}):
        print(chunk, end="", flush=True)
    print()

asyncio.run(stream_response("解释一下什么是向量数据库"))
