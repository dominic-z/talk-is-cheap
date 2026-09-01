# ape.py
"""APE（Automatic Prompt Engineer）迷你实现

闭环三步，正好对应自我进化系统里的三个角色：

    ┌─────────────┐    ┌──────────────┐    ┌──────────────┐
    │  Generator  │ -> │  Evaluator   │ -> │   Selector   │
    │ LLM 生成候选 │    │ 留出集上打分  │    │ 选最优并落盘  │
    │  system     │    │ （确定性指标）│    │              │
    │  prompt     │    │              │    │              │
    └─────────────┘    └──────────────┘    └──────────────┘

本实验刻意只做「搜索式」进化（生成一批候选 -> 打分 -> 挑最好的），
不做 TextGrad/DSPy 那种「根据反馈反推改写」的梯度式进化，
因为前者更容易看清闭环的每一环，代码量也最小。

关于 Evaluator 的一点说明（也是这个实验的核心收获）：
    打分用的是「标签精确匹配」这种确定性指标，不依赖 LLM-as-judge。
    自我进化必须有稳定、可复现的奖励信号 —— 评分抖动的话，
    进化就变成了随机游走。

运行：
    export QWEN_API_KEY=sk-xxxxxx
    pip install -r requirements.txt
    python ape.py --candidates 8
"""

from __future__ import annotations

import argparse
import json
import re
import time
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime
from pathlib import Path

from dataset import DEMO_SAMPLES, EVAL_SAMPLES, LABELS, TASK_DESC
from llm import MODEL, get_client

OUT_DIR = Path(__file__).parent / "git_ignore" /"output"

# 人工写的 baseline prompt：用来对比「自动生成的 prompt 到底有没有更优」
# 注意：baseline 与候选 prompt 面对的是【同一份】用户消息（含类别清单），
# 唯一变量就是 system prompt 本身，这样比较才公平。
BASELINE_PROMPT = (
    "你是一个电商客服助手，请判断用户消息的意图类别。"
    "只能从给定类别中选择一个，只输出类别名称，不要输出任何解释。"
)

GENERATOR_SYSTEM = "你是一位资深的 Prompt 工程师，擅长为 LLM 任务编写高质量的系统提示词。"

GENERATOR_TEMPLATE = """我要让一个 LLM 完成下面的任务：

【任务描述】
{task_desc}

【可选类别】
{labels}

【参考示例】（用户输入 -> 期望输出）
{demos}

请为这个任务写出 {n} 个**风格不同、各自有效**的 system prompt。要求：

1. 每个 prompt 都要能被直接作为 system 角色使用，不要出现"请你帮我写"这类元指令；
2. 请在下面的维度上刻意做出差异，让候选之间有区分度：
   - 角色设定（客服专家 / 意图路由引擎 / 严谨的分类器 ...）
   - 是否给出每个类别的定义与判别规则
   - 是否强调边界情况（如"维修报价"既像售后又像咨询）
   - 是否要求先分析再给结论
   - 输出格式的严格程度
3. 每个 prompt 控制在 80-200 字，不要在 prompt 里塞示例（示例会在运行时另外给出）；
4. 每个 prompt 都必须包含"只能从给定类别中选一个"和"只输出类别名称"这两条约束；
5. 不要在 prompt 里重复列举类别（运行时会附在用户消息里），
   并且严禁自创类别名称 —— 上游系统按标准名称做路由，自创标签等于解析失败。

输出格式（严格遵守，除此之外不要输出任何内容）：
<<<PROMPT_1>>>
第一个 system prompt
<<<END>>>
<<<PROMPT_2>>>
第二个 system prompt
<<<END>>>
...
"""

CANDIDATE_RE = re.compile(r"<<<PROMPT_\d+>>>(.*?)<<<END>>>", re.DOTALL)


# ---------------------------------------------------------------------------
# 1. Generator：让 LLM 生成候选 system prompt
# ---------------------------------------------------------------------------
def generate_candidates(client, n_candidates: int, temperature: float) -> list[str]:
    demos = "\n".join(f'{s["text"]} -> {s["label"]}' for s in DEMO_SAMPLES)
    messages = [
        {"role": "system", "content": GENERATOR_SYSTEM},
        {
            "role": "user",
            "content": GENERATOR_TEMPLATE.format(
                task_desc=TASK_DESC,
                labels="、".join(LABELS),
                demos=demos,
                n=n_candidates,
            ),
        },
    ]

    response = client.chat.completions.create(
        model=MODEL,
        messages=messages,
        temperature=temperature,  # 想要候选有差异，温度就得调高
    )
    content = response.choices[0].message.content or ""

    candidates = [c.strip() for c in CANDIDATE_RE.findall(content) if c.strip()]

    # 兜底：模型偶尔会不遵守格式，退而求其次按分隔符切
    if not candidates:
        parts = re.split(r"<<<PROMPT_\d+>>>", content)
        candidates = [
            p.replace("<<<END>>>", "").strip() for p in parts if p.strip()
        ]

    if not candidates:
        raise RuntimeError(f"候选 prompt 解析失败，模型原始输出：\n{content}")

    return candidates


# ---------------------------------------------------------------------------
# 2. Evaluator：用候选 prompt 在留出集上跑分
# ---------------------------------------------------------------------------
def classify(client, system_prompt: str, text: str) -> str:
    """用指定的 system prompt 对一条用户输入做分类，返回模型原始输出

    类别清单放在【用户消息】里，而不是 system prompt 里 —— 这是刻意的设计：
    标签空间属于"任务"的一部分，所有候选必须面对同一份任务描述，
    否则候选之间比的就不是 prompt 好坏，而是谁自己列的标签更好认。
    """
    response = client.chat.completions.create(
        model=MODEL,
        messages=[
            {"role": "system", "content": system_prompt},
            {
                "role": "user",
                "content": (
                    f"可选类别：" + "、".join(LABELS) + "\n"
                    f"用户消息：{text}\n\n"
                    f"意图类别："
                ),
            },
        ],
        temperature=0,  # 评估阶段要可复现，所以关掉随机性
        # 输出预算要给足：有些候选 prompt 会要求"先分析再给结论"，
        # 卡太死会把最终答案截掉 —— 那是脚手架的锅，不是 prompt 的锅
        max_tokens=512,
    )
    return (response.choices[0].message.content or "").strip()


def normalize_output(raw: str) -> str | None:
    """从模型输出中抽取标签

    模型可能输出「意图类别：退款申请」「退款申请。」等，
    这里取【最先出现】的合法标签；一个都匹配不上则返回 None（记为解析失败）。
    """
    hits = [(raw.rfind(label), label) for label in LABELS if raw.rfind(label) != -1]
    if not hits:
        return None
    # 取【最后】出现的标签：要求"先思考再作答"的 prompt 会在推理过程中
    # 提到其他标签（例如"排除物流查询"），最终结论一定在末尾
    # max([(1,"退款"),(0,"不错")]) = (1,"退款")
    # 功能是找到模型输出的后出现的label，这是为了规避模型可能输出“看起来是退款申请，但实际是售后咨询”这种话。
    return max(hits)[1]


def _eval_one(client, system_prompt: str, sample: dict) -> dict:
    """评测单条样本，返回一条 trace 记录"""
    started = time.perf_counter()
    raw = classify(client, system_prompt, sample["text"])
    latency_ms = int((time.perf_counter() - started) * 1000)

    pred = normalize_output(raw)
    return {
        "ts": datetime.now().isoformat(timespec="seconds"),
        "model": MODEL,
        "input": sample["text"],
        "gold": sample["label"],
        "raw_output": raw,
        "pred": pred,
        "correct": pred == sample["label"],
        "latency_ms": latency_ms,
    }


def evaluate_prompt(
    client,
    name: str,
    system_prompt: str,
    samples: list[dict],
    workers: int,
) -> tuple[dict, list[dict]]:
    """在一个候选 prompt 上跑完整评测集，返回 (汇总结果, trace 列表)"""
    with ThreadPoolExecutor(max_workers=workers) as pool:
        records = list(
            pool.map(
                lambda s: _eval_one(client, system_prompt, s),
                samples,
            )
        )

    for r in records:
        r["candidate"] = name

    correct = sum(1 for r in records if r["correct"])
    unparsed = sum(1 for r in records if r["pred"] is None)
    return {
        "name": name,
        "prompt": system_prompt,
        "correct": correct,
        "total": len(records),
        "accuracy": correct / len(records),
        "unparsed": unparsed,
    }, records


# ---------------------------------------------------------------------------
# 3. Selector：排序并落盘
# ---------------------------------------------------------------------------
def print_leaderboard(results: list[dict]) -> None:
    print("\n─── 排行榜 ─────────────────────────────────────────────")
    print(f"{'排名':<4} {'候选':<10} {'准确率':<10} {'正确':<8} {'解析失败':<8}")
    print("─" * 46)
    for rank, r in enumerate(results, start=1):
        print(
            f"{rank:<4} {r['name']:<10} {r['accuracy'] * 100:>6.1f}%   "
            f"{r['correct']}/{r['total']:<6} {r['unparsed']:<8}"
        )


def save_outputs(best: dict, results: list[dict], records: list[dict]) -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    (OUT_DIR / "best_prompt.txt").write_text(best["prompt"], encoding="utf-8")

    (OUT_DIR / "results.json").write_text(
        json.dumps(results, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    # trace 日志：这就是「自我进化」最基础的基建
    # 有了它，下一步才能做 Reflector（分析失败原因）和 Rewriter（定向改写）
    with (OUT_DIR / "traces.jsonl").open("w", encoding="utf-8") as f:
        for r in records:
            f.write(json.dumps(r, ensure_ascii=False) + "\n")


def main() -> None:
    parser = argparse.ArgumentParser(description="APE 迷你实验：生成候选 prompt 并选优")
    parser.add_argument("--candidates", type=int, default=8, help="生成的候选 prompt 数量")
    parser.add_argument("--workers", type=int, default=8, help="打分时的并发线程数")
    parser.add_argument("--limit", type=int, default=0, help="只用前 N 条评测样本（0=全部）")
    parser.add_argument("--temperature", type=float, default=1.0, help="Generator 采样温度")
    args = parser.parse_args()

    client = get_client()
    samples = EVAL_SAMPLES[: args.limit] if args.limit else EVAL_SAMPLES

    print("=" * 60)
    print("  APE 迷你实验：生成候选 system prompt -> 打分 -> 选优")
    print("=" * 60)
    print(f"模型: {MODEL}   候选数: {args.candidates}   评测样本: {len(samples)} 条")

    # ---- 1. Generator ----
    print(f"\n[1/3] Generator：让 LLM 生成 {args.candidates} 个候选 system prompt ...")
    candidates = generate_candidates(client, args.candidates, args.temperature)
    print(f"      得到 {len(candidates)} 个候选")

    # ---- 2. Evaluator ----
    print(f"\n[2/3] Evaluator：在留出集上打分 ...")
    all_prompts = [("baseline", BASELINE_PROMPT)] + [
        (f"#{i}", p) for i, p in enumerate(candidates, start=1)
    ]

    results: list[dict] = []
    all_records: list[dict] = []
    for name, prompt in all_prompts:
        result, records = evaluate_prompt(client, name, prompt, samples, args.workers)
        results.append(result)
        all_records.extend(records)
        print(
            f"      {name:<9} {result['accuracy'] * 100:>6.1f}%  "
            f"({result['correct']}/{result['total']})"
        )

    # ---- 3. Selector ----
    print("\n[3/3] Selector：排序并保留最优版本")
    results.sort(key=lambda r: (r["accuracy"], -r["unparsed"]), reverse=True)
    print_leaderboard(results)

    best = results[0]
    baseline = next(r for r in results if r["name"] == "baseline")

    print("\n🏆 最优 prompt：" f"{best['name']}  准确率 {best['accuracy'] * 100:.1f}%")
    print("─" * 60)
    print(best["prompt"])
    print("─" * 60)

    delta = (best["accuracy"] - baseline["accuracy"]) * 100
    print(
        f"\n人工 baseline 准确率 {baseline['accuracy'] * 100:.1f}% "
        f"-> 最优候选 {best['accuracy'] * 100:.1f}%  ({delta:+.1f} 个百分点)"
    )

    # 展示最优 prompt 上仍然答错的样本 —— 这些就是下一步 Reflector 的输入
    failures = [
        r for r in all_records
        if r["candidate"] == best["name"] and not r["correct"]
    ]
    if failures:
        print(f"\n最优 prompt 仍有 {len(failures)} 条失败样本（下一步 Reflector 的原料）：")
        for r in failures:
            print(f"  - 输入: {r['input']}")
            print(f"    期望: {r['gold']}   实际: {r['pred']}   原始输出: {r['raw_output']}")

    save_outputs(best, results, all_records)
    print(f"\n💾 已保存：{OUT_DIR}/best_prompt.txt, results.json, traces.jsonl")


if __name__ == "__main__":
    main()
