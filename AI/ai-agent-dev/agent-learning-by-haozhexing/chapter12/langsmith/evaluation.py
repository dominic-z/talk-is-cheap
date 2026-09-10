"""LangSmith 评估示例：数据集准备 + LLM-as-Judge + 自定义工具使用评估器。

被评估对象是一个「真·工具调用 Agent」（langchain 1.x `create_agent`），
因此工具使用指标有真实信号。

适配 langsmith 0.12.x：
- `LangChainStringEvaluator` 已从 langsmith 移除，改为官方推荐的
  「函数式评估器 + RunEvaluator 子类」写法；
- `evaluate()` 返回的 ExperimentResults 迭代出来的是 ExperimentResultRow
  （运行时就是 dict），字段为 run / example / evaluation_results，必须下标访问；
- RunEvaluator 的接口方法是 evaluate_run（不是 evaluate）。
"""

import json
import os

from dotenv import load_dotenv
from langchain.agents import create_agent
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.tools import tool
from langchain_openai import ChatOpenAI

from langsmith import Client, evaluate
from langsmith.evaluation.evaluator import EvaluationResult, RunEvaluator
from langsmith.schemas import Example, Run

load_dotenv("gitignore/.env")  # 加载 .env 中的 LangSmith 配置

DATASET_NAME = "customer-service-qa"


# ---------------------------------------------------------------------------
# 工具：Agent 可用的「外部能力」，名字必须与数据集里的 expected_tools 完全一致
# ---------------------------------------------------------------------------
@tool
def get_refund_policy() -> str:
    """查询退款/退货政策。用户问退款、退货、7天无理由等问题时调用。"""
    return "购买7天内可以申请退款，需要保留原始包装。"


@tool
def get_order_status(order_id: str) -> str:
    """根据订单号查询发货与物流状态。订单号形如 ORD-12345678。"""
    fake_db = {
        "ORD-12345678": "已发货，预计明天到达。",
        "ORD-87654321": "尚未发货，预计 48 小时内出库。",
    }
    return fake_db.get(order_id, f"未查询到订单 {order_id}，请确认订单号是否正确。")


@tool
def get_payment_methods() -> str:
    """查询本店支持的支付方式。"""
    return "支持微信、支付宝、银行卡支付。"


AGENT_TOOLS = [get_refund_policy, get_order_status, get_payment_methods]

# 数据集示例：outputs 里既有参考答案 answer，也声明了期望调用的工具 expected_tools
EXAMPLES = [
    {
        "inputs": {"question": "退款政策是什么？"},
        "outputs": {
            "answer": "购买7天内可以申请退款，需要保留原始包装。",
            "expected_tools": ["get_refund_policy"],
        },
    },
    {
        "inputs": {"question": "订单 ORD-12345678 发货了吗？"},
        "outputs": {
            "answer": "已发货，预计明天到达。",
            "expected_tools": ["get_order_status"],
        },
    },
    {
        "inputs": {"question": "你们支持什么支付方式？"},
        "outputs": {
            "answer": "支持微信、支付宝、银行卡支付。",
            "expected_tools": ["get_payment_methods"],
        },
    },
    {
        "inputs": {"question": "推荐一本 Python 入门书"},
        "outputs": {
            "answer": "推荐《Python编程：从入门到实践》，适合零基础学习者。",
            # 显式声明为空列表：这类闲聊「不应该」调用任何工具
            "expected_tools": [],
        },
    },
]

# 评分维度：维度名 -> 维度说明
CRITERIA = {
    "helpfulness": "回答是否有帮助且解决了用户问题？",
    "correctness": "回答的事实是否正确？",
    "conciseness": "回答是否简洁不冗余？",
}

# LLM-as-Judge 的提示词：要求模型只输出 JSON
JUDGE_PROMPT = ChatPromptTemplate.from_messages([
    (
        "system",
        "你是严格的回答质量评估员。请根据【评分维度】判断【模型回答】是否达标。\n"
        "评分只能是 1（达标）、0.5（部分达标）或 0（不达标）。\n"
        '只输出 JSON，不要任何多余文字，格式：{{"score": 1, "reason": "一句话理由"}}',
    ),
    (
        "human",
        "用户问题：{question}\n\n参考答案：{reference}\n\n模型回答：{answer}\n\n"
        "评分维度：{criterion}\n（{criterion_desc}）",
    ),
])

client = Client()


def create_ds():
    """创建数据集；已存在则直接复用，避免反复执行时报 409 冲突。"""
    if client.has_dataset(dataset_name=DATASET_NAME):
        dataset = client.read_dataset(dataset_name=DATASET_NAME)
        sample = next(iter(client.list_examples(dataset_id=dataset.id, limit=1)), None)
        if sample is not None and "expected_tools" not in (sample.outputs or {}):
            print(
                f"警告：数据集 {DATASET_NAME} 已存在，但示例里没有 expected_tools 字段，"
                f"工具使用指标无法判定（会只显示空分数）。\n"
                f"      请在 LangSmith 上删除该数据集（或改掉 DATASET_NAME）后重新运行。"
            )
        else:
            print(f"数据集 {DATASET_NAME} 已存在，跳过创建")
        return dataset

    dataset = client.create_dataset(
        dataset_name=DATASET_NAME,
        description="客服 Agent 评估数据集",
    )

    # 批量创建（0.3.11 起推荐用 examples=[...]，旧的 inputs/outputs 参数已不推荐）
    client.create_examples(dataset_id=dataset.id, examples=EXAMPLES)

    print(f"数据集已创建，包含 {len(EXAMPLES)} 个示例，可以在langsmith的Datasets & Experiments里看到数据集")
    return dataset


def _parse_judge_output(raw: str) -> tuple[float | None, str]:
    """从 LLM 输出里抽出 {"score": ..., "reason": ...}，解析失败时返回 (None, 原因)。"""
    text = raw.strip()
    start, end = text.find("{"), text.rfind("}")
    if start == -1 or end <= start:
        return None, f"无法解析评估结果：{text[:100]}"

    try:
        data = json.loads(text[start:end + 1])
    except json.JSONDecodeError:
        return None, f"无法解析评估结果：{text[:100]}"

    try:
        score = float(data.get("score"))
    except (TypeError, ValueError):
        score = None
    return score, str(data.get("reason", ""))


def build_llm_judge(judge_chain, criteria: dict | None = None):
    """构造一个函数式评估器。

    langsmith 只按参数名注入，支持的形参为
    run / example / inputs / outputs / reference_outputs / attachments，
    且允许返回 list[dict]（会被归一化成多个反馈）。
    """
    criteria = criteria or CRITERIA

    def llm_judge(inputs: dict, outputs: dict, reference_outputs: dict) -> list[dict]:
        question = inputs.get("question", "")
        answer = outputs.get("answer", "")
        reference = (reference_outputs or {}).get("answer", "")

        feedbacks = []
        for name, desc in criteria.items():
            raw = judge_chain.invoke({
                "question": question,
                "reference": reference,
                "answer": answer,
                "criterion": name,
                "criterion_desc": desc,
            })
            score, reason = _parse_judge_output(raw)
            feedbacks.append({"key": f"qa_{name}", "score": score, "comment": reason})
        return feedbacks

    return llm_judge


def _collect_tool_names(run) -> list[str]:
    """递归收集 run 树中所有 tool 类型子运行的名称。"""
    names: list[str] = []

    def walk(node) -> None:
        for child in getattr(node, "child_runs", None) or []:
            if str(getattr(child, "run_type", "")) == "tool":
                names.append(getattr(child, "name", "<unknown>"))
            walk(child)

    walk(run)
    return names


class ToolUsageEvaluator(RunEvaluator):
    """评估 Agent 的工具调用是否符合预期。

    判定规则（避免出现"假满分"）：
    - 数据集未声明 expected_tools → 没有判据，返回 score=None，不产出分数；
    - expected_tools 为空列表 → 要求不调用任何工具，调了就 0 分；
    - expected_tools 非空 → 期望的工具必须全部被调用，缺一个就 0 分。

    注意：接口方法是 evaluate_run，且必须接受 evaluator_run_id 关键字参数。
    """

    def evaluate_run(
        self,
        run: Run,
        example: Example | None = None,
        evaluator_run_id: str | None = None,
    ) -> EvaluationResult:
        called = _collect_tool_names(run)
        outputs = (example.outputs if example is not None else None) or {}

        if "expected_tools" not in outputs:
            return EvaluationResult(
                key="tool_usage_correctness",
                score=None,
                comment=f"调用工具={called}（数据集未声明 expected_tools，跳过判定）",
            )

        expected = list(outputs.get("expected_tools") or [])

        if not expected:
            score = 1.0 if not called else 0.0
            comment = f"调用工具={called}，预期不调用任何工具"
            if called:
                comment += "，属于多余调用"
        else:
            missing = [name for name in expected if name not in called]
            extra = [name for name in called if name not in expected]
            score = 1.0 if not missing else 0.0
            comment = f"调用工具={called}，预期={expected}"
            if missing:
                comment += f"，缺失={missing}"
            if extra:
                comment += f"，额外={extra}"

        return EvaluationResult(
            key="tool_usage_correctness",
            score=score,
            comment=comment,
        )


def _print_results(results) -> None:
    """打印评估结果；迭代产出的是 ExperimentResultRow，本质是 dict。"""
    for row in results:
        example = row["example"]
        run = row["run"]
        answer = (run.outputs or {}).get("answer", "")

        print(f"输入: {example.inputs.get('question', '')}")
        print(f"输出: {answer[:50]}...")
        for score in row["evaluation_results"]["results"]:
            value = score.score if score.score is not None else score.value
            text = f"{value:.2f}" if isinstance(value, (int, float)) else str(value)
            print(f"  {score.key}: {text}  {score.comment or ''}")
        print()


def evaluation():
    llm = ChatOpenAI(
        model=os.getenv("QWEN_MODEL", "qwen3.7-flash"),
        api_key=os.getenv("QWEN_API_KEY"),
        base_url=os.getenv(
            "QWEN_BASE_URL",
            "https://dashscope.aliyuncs.com/compatible-mode/v1",
        ),
    )

    # 被评估对象：一个真的会调用工具的 Agent
    agent = create_agent(
        llm,
        tools=AGENT_TOOLS,
        system_prompt=(
            "你是电商客服助手，回答要简洁专业。\n"
            "涉及退款政策、订单状态、支付方式的问题，必须先调用对应工具获取准确信息，"
            "不要凭记忆作答；与业务无关的问题（例如推荐书籍）直接回答即可，不要调用工具。"
        ),
        name="customer_service_agent",
    )

    def target_fn(inputs: dict) -> dict:
        """被评估的函数：把问题交给 Agent，返回最终回答"""
        state = agent.invoke(
            {"messages": [{"role": "user", "content": inputs["question"]}]}
        )
        return {"answer": state["messages"][-1].text}

    # 用同一个 LLM 作为裁判，构成 LLM-as-Judge 评估器
    judge_chain = JUDGE_PROMPT | llm | StrOutputParser()
    llm_judge = build_llm_judge(judge_chain)

    # 第 1 轮：只评估回答质量
    results = evaluate(
        target_fn,
        data=DATASET_NAME,  # 数据集名称
        evaluators=[llm_judge],
        experiment_prefix="customer-service-v1",
        max_concurrency=4,
    )
    _print_results(results)

    # 第 2 轮：再加上工具使用评估器，两条指标一起看
    results = evaluate(
        target_fn,
        data=DATASET_NAME,
        evaluators=[llm_judge, ToolUsageEvaluator()],
        experiment_prefix="customer-service-v2",
        max_concurrency=4,
    )
    _print_results(results)
    print("可以在可以在langsmith的Datasets & Experiments里看到数据集的评估结果")


if __name__ == "__main__":
    create_ds()
    evaluation()
