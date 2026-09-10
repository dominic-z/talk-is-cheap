import json

from langchain_core.prompts import ChatPromptTemplate
from langchain_core.load import dumps
from langsmith import Client
from langsmith.utils import LangSmithConflictError
from dotenv import load_dotenv

load_dotenv("gitignore/.env")  # 加载 .env 中的 LangSmith 配置
client = Client()

PROMPT_NAME = "customer-service-prompt"
PROMPT_DESCRIPTION = "客服 Agent 系统提示词 v2"


def build_prompt() -> ChatPromptTemplate:
    return ChatPromptTemplate.from_messages([
        ("system", """你是"小慧"客服助手。
    职责：{responsibilities}
    服务准则：{guidelines}"""),
        ("human", "{question}"),
    ])


def prompt_manifest(prompt: ChatPromptTemplate) -> dict:
    """按 push 时同样的序列化方式计算 manifest，用于和远端最新 commit 比对。"""
    return json.loads(dumps(prompt))


def push_prompt() -> None:
    # 创建 Prompt
    prompt = build_prompt()
    manifest = prompt_manifest(prompt)

    # 幂等：远端已存在且最新 commit 内容一致时，不再产生新的 commit
    existing = client.get_prompt(PROMPT_NAME)
    if existing is not None and existing.num_commits:
        # skip_cache：客户端会缓存 pull 结果，不跳过会读到过期的 commit
        remote_manifest = client.pull_prompt_commit(PROMPT_NAME, skip_cache=True).manifest
        if remote_manifest == manifest:
            if existing.description != PROMPT_DESCRIPTION:
                # 仅描述变化时只更新元信息，同样不产生新版本
                client.update_prompt(PROMPT_NAME, description=PROMPT_DESCRIPTION)
                print("内容未变化，仅同步描述，未产生新版本")
            else:
                print("内容未变化，跳过推送")
            return

    # 首次创建或内容有变化：推送到 Hub
    try:
        url = client.push_prompt(
            PROMPT_NAME,
            object=prompt,
            description=PROMPT_DESCRIPTION,
        )
    except LangSmithConflictError:
        # 服务端在内容与最新 commit 完全一致时返回 409 "Nothing to commit"
        print("远端已是最新内容，跳过推送")
        return
    print(f"Prompt 已推送到 LangSmith Hub: {url}")


def pull_prompt() -> None:
    # 从 Hub 拉取 Prompt
    prompt = client.pull_prompt(PROMPT_NAME)

    # 在 Chain 中直接使用
    from langchain_openai import ChatOpenAI
    from langchain_core.output_parsers import StrOutputParser

    llm = ChatOpenAI(model="gpt-4.1-mini", temperature=0.7)
    chain = prompt | llm | StrOutputParser()

    result = chain.invoke({"question": "退款政策是什么？"})
    print(result)


if __name__ == "__main__":
    push_prompt()
    pull_prompt()
