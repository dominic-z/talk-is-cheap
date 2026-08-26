from dataclasses import dataclass, field
from enum import Enum
import  json
import time
import os


from openai import OpenAI
client = OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )

class InfoSource(Enum):
    SYSTEM_PROMPT = "system_prompt"
    USER_MESSAGE = "user_message"
    CONVERSATION = "conversation"
    TOOL_RESULT = "tool_result"
    RETRIEVED_DOC = "retrieved_doc"
    TASK_STATE = "task_state"
    AGENT_NOTE = "agent_note"

@dataclass
class ContextItem:
    """流水线中流动的信息片段。"""
    content: str
    source: InfoSource
    priority: int = 5              # 1（最高）到 10（最低）
    relevance_score: float = 1.0   # 0~1
    token_count: int = 0
    timestamp: float = field(default_factory=time.time)
    metadata: dict = field(default_factory=dict)

    def __post_init__(self):
        if self.token_count == 0:
            self.token_count = len(self.content) // 3  # 粗略估算

@dataclass
class ContextBudget:
    """每个来源的 token 预算上限。"""
    total_tokens: int = 128000
    output_reserve: int = 4096
    system_prompt_max: int = 2000
    task_state_max: int = 3000
    agent_notes_max: int = 2000
    recent_conversation_max: int = 20000
    tool_results_max: int = 40000
    retrieved_docs_max: int = 20000
    history_max: int = 30000

    @property
    def available_input_tokens(self) -> int:
        return self.total_tokens - self.output_reserve



class GatherStage:
    """G - 从各来源拉取数据，形成候选信息池。"""

    def __init__(self):
        self.items: list[ContextItem] = []

    def add_system_prompt(self, prompt: str):
        self.items.append(ContextItem(content=prompt, source=InfoSource.SYSTEM_PROMPT, priority=1))

    def add_user_message(self, message: str):
        self.items.append(ContextItem(content=message, source=InfoSource.USER_MESSAGE, priority=1))

    def add_conversation_history(self, messages: list[dict]):
        """越近的消息优先级越高——大多数场景下最近对话与当前任务最相关。"""
        for i, msg in enumerate(messages):
            recency = i / max(len(messages), 1)
            priority = int(8 - recency * 5)    # 最新 3，最旧 8
            self.items.append(ContextItem(
                content=f"[{msg['role']}]: {msg['content']}",
                source=InfoSource.CONVERSATION,
                priority=priority,
                metadata={"turn_index": i, "role": msg["role"]}
            ))

    def add_tool_result(self, tool_name: str, result: str, is_recent: bool = True):
        self.items.append(ContextItem(
            content=f"[工具: {tool_name}]\n{result}",
            source=InfoSource.TOOL_RESULT,
            priority=2 if is_recent else 6,
            metadata={"tool_name": tool_name}
        ))

    def add_retrieved_doc(self, doc: str, score: float):
        self.items.append(ContextItem(content=doc, source=InfoSource.RETRIEVED_DOC,
                                      priority=4, relevance_score=score))

    def add_task_state(self, state: dict):
        self.items.append(ContextItem(content=json.dumps(state, ensure_ascii=False, indent=2),
                                      source=InfoSource.TASK_STATE, priority=2))

    def add_agent_note(self, note: str):
        self.items.append(ContextItem(content=note, source=InfoSource.AGENT_NOTE, priority=2))


class SelectStage:
    """S1 - 按优先级和预算筛选信息。"""

    def __init__(self, budget: ContextBudget):
        self.budget = budget

    def select(self, items: list[ContextItem]) -> list[ContextItem]:
        # 按来源分组
        groups: dict[InfoSource, list[ContextItem]] = {}
        for item in items:
            groups.setdefault(item.source, []).append(item)

        selected = []
        # 1. 必选：system prompt + user message（始终保留）
        for src in [InfoSource.SYSTEM_PROMPT, InfoSource.USER_MESSAGE]:
            selected.extend(groups.get(src, []))

        # 2. 任务状态 + Agent 笔记
        for src in [InfoSource.TASK_STATE, InfoSource.AGENT_NOTE]:
            items = groups.get(src, [])
            max_t = self.budget.task_state_max if src == InfoSource.TASK_STATE \
                    else self.budget.agent_notes_max
            selected.extend(self._fit_within_budget(items, max_t))

        # 3. 工具结果（按 priority 升序，最近的优先）
        selected.extend(self._fit_within_budget(
            sorted(groups.get(InfoSource.TOOL_RESULT, []), key=lambda x: x.priority),
            self.budget.tool_results_max
        ))

        # 4. 检索文档（按 relevance_score 降序）
        selected.extend(self._fit_within_budget(
            sorted(groups.get(InfoSource.RETRIEVED_DOC, []),
                   key=lambda x: x.relevance_score, reverse=True),
            self.budget.retrieved_docs_max
        ))

        # 5. 对话历史（按 turn_index 降序，最近的优先）
        selected.extend(self._fit_within_budget(
            sorted(groups.get(InfoSource.CONVERSATION, []),
                   key=lambda x: x.metadata.get("turn_index", 0), reverse=True),
            self.budget.history_max
        ))

        return selected

    def _fit_within_budget(self, items: list[ContextItem], max_tokens: int) -> list[ContextItem]:
        """贪心填充：在预算内选尽可能多的项。"""
        selected, remaining = [], max_tokens
        for item in items:
            if item.token_count <= remaining:
                selected.append(item)
                remaining -= item.token_count
            else:
                break
        return selected



class SummarizeStage:
    """S2 - 对超长内容按来源类型差异化压缩。"""

    def __init__(self, max_item_tokens: int = 2000):
        self.max_item_tokens = max_item_tokens

    def summarize(self, items: list[ContextItem]) -> list[ContextItem]:
        result = []
        for item in items:
            if item.token_count > self.max_item_tokens:
                result.append(self._compress_item(item))
            else:
                result.append(item)
        return result

    def _compress_item(self, item: ContextItem) -> ContextItem:
        # 按来源类型用不同 prompt
        if item.source == InfoSource.TOOL_RESULT:
            prompt = f"""压缩以下工具结果，保留所有数字数据和结论，去除冗余格式：
{item.content}
要求：保留所有数字、保留结论、控制在 500 字内。"""
        elif item.source == InfoSource.RETRIEVED_DOC:
            prompt = f"""提取以下文档与当前任务最相关的核心内容：
{item.content}
要求：控制在 300 字内，只保留关键知识点。"""
        else:
            prompt = f"""简要总结以下内容要点（300 字内）：{item.content}"""

        resp = client.chat.completions.create(
            model="qwen3.7-flash",    # 压缩用小模型即可
            messages=[{"role": "user", "content": prompt}],
            max_tokens=600
        )
        return ContextItem(
            content=f"[压缩] {resp.choices[0].message.content}",
            source=item.source,
            priority=item.priority,
            relevance_score=item.relevance_score,
            metadata={**item.metadata, "compressed": True}
        )

class ConstructStage:
    """C - 按 Lost-in-the-Middle 感知的最优布局组装。

    注意（已知问题，暂不修复）：
    本阶段的教学重点是 GSSC 流水线与注意力布局，对消息角色的处理做了简化，
    直接拿构造结果调用真实 API 时，以下写法并不完全符合 OpenAI Chat Completions 规范：
    1. 允许出现多条 system 消息——这一点本身合法（API 不强制唯一/首位），
       但部分模型实现对非首位 system 支持不佳；
    2. 工具结果被错误地放成 assistant 角色（见下方 TOOL_RESULT 处理处的注释），
       规范写法应为 role="tool" 且必须紧跟在发起 tool_calls 的 assistant 消息之后；
    3. 对话历史与工具结果分别独立追加，打乱了真实的轮次顺序，
       导致 assistant(tool_calls) → tool 的配对关系丢失；
    4. 若对话历史以 assistant 结尾，紧接追加的 assistant 工具结果会造成连续同角色消息，
       部分服务商会拒绝。
    若工具结果仅作为背景资料（而非还原真实调用链），更稳妥的做法是像检索文档一样
    并入 system 消息，或作为 user 消息的附件内容。
    """

    def construct(self, items: list[ContextItem]) -> list[dict]:
        groups: dict[InfoSource, list[ContextItem]] = {}
        for item in items:
            groups.setdefault(item.source, []).append(item)

        messages = []

        # 开头（高注意力）：System Prompt + 嵌入任务状态和笔记
        # 说明：本块的 system 消息与下方检索文档的 system 消息会产生两条 system。
        # 这并不违反 OpenAI API 规范（允许多条、允许非首位），但官方推荐放在开头，
        # 且少数模型对非首位 system 处理不佳，生产环境需留意。
        if InfoSource.SYSTEM_PROMPT in groups:
            system_content = groups[InfoSource.SYSTEM_PROMPT][0].content
            extras = []
            if InfoSource.TASK_STATE in groups:
                extras.append(f"\n\n## 当前任务状态\n{groups[InfoSource.TASK_STATE][0].content}")
            if InfoSource.AGENT_NOTE in groups:
                extras.append(f"\n\n## 执行笔记\n{groups[InfoSource.AGENT_NOTE][0].content}")
            messages.append({"role": "system", "content": system_content + "".join(extras)})

        # 中间（较低注意力）：检索文档 → 历史对话 → 工具结果
        if InfoSource.RETRIEVED_DOC in groups:
            docs = [item.content for item in groups[InfoSource.RETRIEVED_DOC]]
            messages.append({"role": "system", "content": "## 相关知识\n\n" + "\n\n---\n\n".join(docs)})

        for item in sorted(groups.get(InfoSource.CONVERSATION, []),
                           key=lambda x: x.metadata.get("turn_index", 0)):
            # 说明：此处按 turn_index 重建对话历史，但工具结果（TOOL_RESULT）是在下方
            # 独立追加的，脱离了其在历史中的真实位置。OpenAI 规范要求工具调用遵循
            # assistant(tool_calls) → tool(tool_call_id) → assistant 的严格配对与顺序，
            # 当前写法会破坏这种配对关系。
            role = item.metadata.get("role", "user")
            content = item.content
            prefix = f"[{role}]: "
            if content.startswith(prefix):
                content = content[len(prefix):]
            messages.append({"role": role, "content": content})

        # ⚠️ 已知问题：
        # 1. 角色错误——工具结果的规范角色是 role="tool"（且需携带 tool_call_id，
        #    前面必须有发起该调用的 assistant(tool_calls) 消息），这里却伪装成 assistant，
        #    模型会误以为这是自己之前说过的话；
        # 2. 顺序错误——无差别追加在全部对话历史之后，若真实历史中工具调用发生在中间轮次，
        #    配对与顺序都已失真；
        # 3. 若上方对话历史以 assistant 结尾，这里会出现连续两条 assistant，部分接口会报 400。
        # 仅作背景资料时，更推荐把工具结果并入 system 消息（类似检索文档的处理方式）。
        for item in groups.get(InfoSource.TOOL_RESULT, []):
            messages.append({"role": "assistant", "content": item.content})

        # 结尾（最高注意力）：当前用户消息
        if InfoSource.USER_MESSAGE in groups:
            messages.append({"role": "user", "content": groups[InfoSource.USER_MESSAGE][0].content})

        return messages

class GSSCPipeline:
    """GSSC 上下文构建流水线：Gather → Select → Summarize → Construct。"""

    def __init__(self, budget: ContextBudget = None, max_item_tokens: int = 2000):
        self.budget = budget or ContextBudget()
        self.max_item_tokens = max_item_tokens
        self.gather = GatherStage()
        self.select = SelectStage(self.budget)
        self.summarize = SummarizeStage(max_item_tokens)
        self.construct = ConstructStage()

    def build(self, system_prompt: str, user_message: str,
              conversation_history=None, tool_results=None,
              retrieved_docs=None, task_state=None, agent_notes=None) -> list[dict]:
        """一站式构建最优上下文，返回 messages 列表（可直接调 LLM API）。"""
        # G: Gather
        self.gather = GatherStage()
        self.gather.add_system_prompt(system_prompt)
        self.gather.add_user_message(user_message)
        if conversation_history:
            self.gather.add_conversation_history(conversation_history)
        for tr in (tool_results or []):
            self.gather.add_tool_result(tr["tool"], tr["result"], tr.get("recent", False))
        for d in (retrieved_docs or []):
            self.gather.add_retrieved_doc(d["content"], d["score"])
        if task_state:
            self.gather.add_task_state(task_state)
        if agent_notes:
            self.gather.add_agent_note(agent_notes)

        all_items = self.gather.get_all_items()
        # S1: Select
        selected = self.select.select(all_items)
        # S2: Summarize
        summarized = self.summarize.summarize(selected)
        # C: Construct
        return self.construct.construct(summarized)


def main():
    pipeline = GSSCPipeline(budget=ContextBudget(total_tokens=128000))

    messages = pipeline.build(
        system_prompt="你是资深数据分析师，擅长用户行为分析。",
        user_message="基于之前的分析，请给出提升留存率的前 3 条建议。",
        conversation_history=[
            {"role": "user", "content": "帮我分析 Q1 的用户留存数据"},
            {"role": "assistant", "content": "好的，我来查询数据库..."},
            {"role": "user", "content": "重点看新用户的留存"},
        ],
        tool_results=[{"tool": "sql_query", "result": "新用户7日留存率: 38%...", "recent": True}],
        task_state={"objective": "分析 Q1 用户留存率下降原因", "current_step": "生成建议"},
        agent_notes="关键发现：新用户首日引导流程完成率仅 45%，是留存下降的主因。"
    )

    # messages 现在可直接传给 client.chat.completions.create(...)
