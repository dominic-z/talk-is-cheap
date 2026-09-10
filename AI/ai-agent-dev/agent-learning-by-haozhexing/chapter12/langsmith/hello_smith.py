# 只需设置环境变量，所有 LangChain 调用自动被追踪
import os
from dotenv import load_dotenv

load_dotenv("gitignore/.env")  # 加载 .env 中的 LangSmith 配置

from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate,MessagesPlaceholder
from langchain_classic.agents import AgentExecutor, create_openai_tools_agent
from langchain_core.output_parsers import StrOutputParser

llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1")

def simple_chat():
    prompt = ChatPromptTemplate.from_messages([
    ("system", "你是一个{role}。"),
    ("human", "{question}")])
    chain = prompt | llm | StrOutputParser()

    # 这次调用会被自动追踪到 LangSmith
    result = chain.invoke({"role": "Python 专家", "question": "什么是装饰器？"})
    print(result)

def official_quick_start():
    """
    langsmith新建project下面的quick start
    :return:
    """
    from langchain.agents import create_agent

    def get_weather(city: str) -> str:
        """Get weather for a given city."""
        return f"It's always sunny in {city}!"

    agent = create_agent(
        model=llm,
        tools=[get_weather],
        system_prompt="You are a helpful assistant",
    )

    # Run the agent
    print(agent.invoke(
        {"messages": [{"role": "user", "content": "What is the weather in San Francisco?"}]}
    ))




def  main():
    print("="*10+"简单聊聊"+"="*10)
    simple_chat()
    print("="*10+"official_quick_start"+"="*10)
    official_quick_start()
#     执行完成后，可以在smith的项目里看到漂亮的执行记录，尤其official_quick_start()


if __name__ == "__main__":
    main()
