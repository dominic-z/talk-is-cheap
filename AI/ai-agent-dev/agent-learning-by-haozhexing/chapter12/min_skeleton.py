# pip install langchain langchain-openai langchain-community
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
import os

llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
                 temperature=0.7)
chain = ChatPromptTemplate.from_messages([
    ("system", "你是一个{role}，专注于{domain}领域。"),
    ("user", "{question}")
]) | llm | StrOutputParser()

print(chain.invoke({"role": "Python 专家", "domain": "机器学习",
                    "question": "如何用 sklearn 训练一个分类器？"}))

print("="*60)
print("="*60)
print("="*60)

# ==================== RunnableParallel：并行 Runnable ====================
# 作用：扇出（fan-out）——把同一份输入并行分发给多个 Runnable；
#       扇入（fan-in）——再把各分支的输出重组为 dict 喂给下游链。
# 说明：{"pro": pro_chain, "simple": simple_chain} 这种 dict 字面量
#       是 RunnableParallel(...) 的等价快捷写法。
from langchain_core.runnables import RunnableParallel

# 两条分支链：一个专业版，一个通俗版
pro_chain = ChatPromptTemplate.from_messages([
    ("system", "你是资深的 {domain} 专家，回答要专业、严谨。"),
    ("user", "{question}")
]) | llm | StrOutputParser()

simple_chain = ChatPromptTemplate.from_messages([
    ("system", "你是耐心的老师，请用通俗的比喻给初学者讲解。"),
    ("user", "{question}")
]) | llm | StrOutputParser()

# 扇出：并行执行两条分支，返回 {"pro": ..., "simple": ...}
branch = RunnableParallel(pro=pro_chain, simple=simple_chain)

# 扇入：把两个分支的输出合并后交给总结链
summary_chain = ChatPromptTemplate.from_messages([
    ("system", "你是一名学习教练，请综合下面的两种解释，"
               "输出一份深入浅出的最终回答。\n\n"
               "专业版：{pro}\n通俗版：{simple}"),
    ("user", "请开始。")
]) | llm | StrOutputParser()

# 整体并行链：一次输入 -> 两个分支同时执行 -> 合并输出
parallel_chain = branch | summary_chain
print(parallel_chain.invoke({"domain": "机器学习",
                             "question": "什么是过拟合？"}))
