# tools.py
"""data-analyst 技能的工具实现。

仅依赖 Python 标准库（csv / statistics / json），保持简单。
"""
import csv
import json
import os
import statistics

# SKILL.md 中声明的工具列表
TOOL_NAMES = ["read_csv", "compute_stats", "create_chart"]

_ROWS = {}  # 已加载的 CSV 缓存：路径 -> 行列表（dict）


def _load_rows(path: str) -> list[dict]:
    """内部方法：加载（或从缓存读取）CSV 行数据"""
    if path not in _ROWS:
        with open(path, encoding="utf-8", errors="replace") as f:
            _ROWS[path] = list(csv.DictReader(f))
    return _ROWS[path]


def _numeric(values) -> list[float]:
    """内部方法：把可转成数字的值提取出来"""
    nums = []
    for v in values:
        try:
            nums.append(float(v))
        except (TypeError, ValueError):
            pass
    return nums


def _as_list(value) -> list:
    """内部方法：把 LLM 传入的参数宽容地转成列表。

    兼容：真实列表、JSON 数组字符串、逗号/竖线/分号分隔字符串。
    """
    if isinstance(value, (list, tuple)):
        return list(value)
    if isinstance(value, str):
        s = value.strip()
        if s.startswith("["):
            try:
                parsed = json.loads(s)
                if isinstance(parsed, list):
                    return parsed
            except json.JSONDecodeError:
                pass
        for sep in (",", "|", ";"):
            if sep in s:
                return [item.strip() for item in s.split(sep) if item.strip()]
        return [s] if s else []
    return []


def read_csv(path: str) -> str:
    """加载 CSV 文件，返回数据概览（行列数、字段类型、缺失值比例）。

    Args:
        path: CSV 文件路径。

    Returns:
        数据概览文本；文件不存在或为空时返回错误说明。
    """
    if not os.path.exists(path):
        return f"[错误] 文件不存在: {path}"
    rows = _load_rows(path)
    if not rows:
        return f"[错误] 文件为空或没有表头: {path}"

    columns = list(rows[0].keys())
    total_cells = len(rows) * len(columns)
    missing_cells = sum(1 for r in rows for c in columns if not r.get(c))

    lines = [f"文件: {path}", f"行数: {len(rows)}", f"列数: {len(columns)}",
             f"缺失值比例: {missing_cells / total_cells:.1%}", "", "字段信息:"]
    for col in columns:
        values = [r.get(col) for r in rows]
        nums = _numeric(values)
        non_empty = [v for v in values if v]
        dtype = "数值" if len(nums) >= max(1, int(len(non_empty) * 0.6)) else "文本"
        col_missing = sum(1 for v in values if not v)
        lines.append(f"  - {col} ({dtype}, 缺失 {col_missing / len(rows):.1%})")
    return "\n".join(lines)


def compute_stats(path: str, column: str = "", group_by: str = "") -> str:
    """计算描述性统计量。

    Args:
        path: 已通过 read_csv 加载的 CSV 文件路径。
        column: 要统计的数值列名；留空则统计所有数值列。
        group_by: 可选的分类列，指定后按该列分组统计目标数值列的均值/计数。

    Returns:
        文本格式的统计结果。
    """
    rows = _load_rows(path)
    if not rows:
        return f"[错误] 请先用 read_csv 加载文件: {path}"
    columns = list(rows[0].keys())

    # 分组统计
    if group_by:
        if group_by not in columns:
            return f"[错误] 分组列不存在: {group_by}，可用列: {columns}"
        if not column or column not in columns:
            return f"[错误] 分组统计需要指定有效的数值列 column，可用列: {columns}"
        groups: dict[str, list[float]] = {}
        for r in rows:
            groups.setdefault(r.get(group_by) or "未知", []).append(r.get(column))
        lines = [f"按 [{group_by}] 分组统计 [{column}]:"]
        for g, vals in sorted(groups.items()):
            nums = _numeric(vals)
            if nums:
                lines.append(f"  - {g}: 计数 {len(nums)}, 合计 {sum(nums):.2f}, "
                             f"均值 {statistics.mean(nums):.2f}")
            else:
                lines.append(f"  - {g}: 计数 {len(vals)}, 无数值数据")
        return "\n".join(lines)

    # 整体描述统计
    if column:
        targets = [column]
        if column not in columns:
            return f"[错误] 列不存在: {column}，可用列: {columns}"
    else:
        targets = columns

    lines = []
    for col in targets:
        nums = _numeric([r.get(col) for r in rows])
        if len(nums) < 2:
            continue
        q1, q2, q3 = statistics.quantiles(nums, n=4)
        lines.append(
            f"[{col}] 计数 {len(nums)} | 均值 {statistics.mean(nums):.2f} | "
            f"中位数 {q2:.2f} | 标准差 {statistics.stdev(nums):.2f} | "
            f"Q1 {q1:.2f} | Q3 {q3:.2f} | 最小 {min(nums):.2f} | 最大 {max(nums):.2f}"
        )
    if not lines:
        return "[提示] 没有可统计的数值列"
    return "\n".join(lines)


def create_chart(chart_type: str, labels, values,
                 title: str = "chart") -> str:
    """生成简单的 ASCII 横向条形图（不依赖绘图库）。

    Args:
        chart_type: 图表类型，支持 line / bar / histogram / pie / scatter，
                    统一以条形图形式呈现分布。
        labels: 数据点标签列表，或逗号分隔的字符串（如 "A,B,C"）。
        values: 与 labels 一一对应的数值列表，或逗号分隔的字符串（如 "1,2,3"）。
        title: 图表标题。

    Returns:
        ASCII 图表文本。
    """
    labels = _as_list(labels)
    values = _as_list(values)
    if not labels or not values or len(labels) != len(values):
        return "[错误] labels 与 values 必须非空且长度一致"

    nums = _numeric(values)
    if not nums:
        return "[错误] values 中没有有效数值"

    lines = [f"{title} ({chart_type})", "-" * 50]
    max_abs = max(abs(v) for v in nums) or 1.0
    for label, value in zip(labels, values):
        try:
            v = float(value)
        except (TypeError, ValueError):
            continue
        bar = "█" * max(1, round(abs(v) / max_abs * 30))
        sign = "-" if v < 0 else ""
        lines.append(f"{str(label)[:20]:<22}| {sign}{bar} {v:.2f}")
    return "\n".join(lines)
