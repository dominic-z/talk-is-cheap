# tools.py
"""code-reviewer 技能的工具实现（code-based skill）。

核心审查逻辑由代码完成（静态规则扫描），
LLM 只负责调用工具并基于结果撰写审查报告。
"""
import os
import re

# SKILL.md 中声明的工具列表
TOOL_NAMES = ["read_file", "analyze_code"]

# 静态扫描规则：(级别, 类别, 描述, 正则)
_SECURITY_RULES = [
    ("🔴 严重", "SQL 注入", "SQL 语句使用字符串拼接，存在 SQL 注入风险",
     r"""(?:execute|cursor\.execute)\s*\(\s*["'].*%s"""),
    ("🔴 严重", "SQL 注入", "SQL 语句使用 f-string 拼接，存在 SQL 注入风险",
     r"""f["'].*\b(?:SELECT|INSERT|UPDATE|DELETE)\b"""),
    ("🔴 严重", "硬编码凭证", "疑似硬编码的密钥或密码",
     r"""(?i)(?:password|passwd|secret|api_key|apikey|token)\s*=\s*["'][^"']+["']"""),
    ("🔴 严重", "不安全的反序列化", "pickle 反序列化不可信数据可能导致任意代码执行",
     r"\bpickle\.loads?\("),
    ("🔴 严重", "不安全的反序列化", "yaml.load 未指定安全 Loader",
     r"\byaml\.load\((?![^)]*Loader)"),
    ("🔴 严重", "危险执行", "eval 执行动态代码存在注入风险",
     r"(?<![\w.])eval\("),
    ("🔴 严重", "危险执行", "exec 执行动态代码存在注入风险",
     r"(?<![\w.])exec\("),
    ("🟡 警告", "未验证的输入", "使用 input() 获取用户输入且未见后续校验",
     r"(?<![\w.])input\("),
]

_PERFORMANCE_RULES = [
    ("🟡 警告", "性能", "在循环内拼接字符串，建议改用 ''.join() 或 io.StringIO",
     r"for\s+\w+\s+in\s+.*:\s*\n\s+\w+\s*\+=\s*[\"']"),
]

_MAX_FUNC_LINES = 50
_MAX_LOOP_DEPTH = 3
_FUNC_DEF_PATTERN = re.compile(r"^\s*def\s+", re.MULTILINE)


def read_file(path: str) -> str:
    """读取本地文件内容。

    Args:
        path: 文件路径（相对或绝对）。

    Returns:
        文件内容文本；文件不存在或过大时返回错误说明。
    """
    if not os.path.exists(path):
        return f"[错误] 文件不存在: {path}"
    size = os.path.getsize(path)
    if size > 200 * 1024:
        return f"[错误] 文件过大（{size // 1024} KB），请提供更小的文件"
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()


def analyze_code(code: str) -> str:
    """对代码进行静态规则扫描，输出结构化审查结果。

    扫描维度：安全漏洞（SQL 注入、硬编码密钥、不安全反序列化等）、
    代码质量（函数过长、循环嵌套过深）、性能问题。

    Args:
        code: 待审查的源代码文本。

    Returns:
        文本格式的审查结果（按严重级别分组）。
    """
    lines = code.splitlines()
    issues = []

    # 1. 安全与性能规则扫描
    for level, category, message, pattern in _SECURITY_RULES + _PERFORMANCE_RULES:
        for m in re.finditer(pattern, code, flags=re.MULTILINE):
            line_no = code.count("\n", 0, m.start()) + 1
            issues.append(f"{level} [{category}] 第 {line_no} 行: {message}")

    # 2. 函数长度检查
    func_start = None
    func_name = None
    for i, line in enumerate(lines, 1):
        m = re.match(r"\s*def\s+(\w+)", line)
        if m:
            if func_start and i - func_start > _MAX_FUNC_LINES:
                issues.append(
                    f"🟡 警告 [代码质量] 第 {func_start} 行: "
                    f"函数 {func_name} 超过 {_MAX_FUNC_LINES} 行，建议拆分"
                )
            func_start, func_name = i, m.group(1)
    if func_start and len(lines) - func_start + 1 > _MAX_FUNC_LINES:
        issues.append(
            f"🟡 警告 [代码质量] 第 {func_start} 行: "
            f"函数 {func_name} 超过 {_MAX_FUNC_LINES} 行，建议拆分"
        )

    # 3. 循环嵌套深度检查
    max_depth = _max_loop_depth(code)
    if max_depth > _MAX_LOOP_DEPTH:
        issues.append(
            f"🟡 警告 [代码质量] 循环嵌套深度为 {max_depth} 层"
            f"（超过 {_MAX_LOOP_DEPTH} 层），建议重构"
        )

    # 4. 汇总输出
    stats = (f"代码行数: {len(lines)} | "
             f"函数数: {len(_FUNC_DEF_PATTERN.findall(code))} | "
             f"最大循环嵌套: {max_depth} 层")
    if not issues:
        return f"{stats}\n\n✅ 静态扫描未发现明显问题，请结合上下文做人工审查。"
    return stats + "\n\n" + "\n".join(issues)


def _max_loop_depth(code: str) -> int:
    """基于缩进计算循环语句的最大嵌套层数"""
    loop_indents = []
    max_depth = 0
    for line in code.splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        indent = len(line) - len(line.lstrip(" \t"))
        # 弹出缩进更浅的循环
        while loop_indents and indent <= loop_indents[-1]:
            loop_indents.pop()
        if re.match(r"(for|while)\b", stripped):
            loop_indents.append(indent)
            max_depth = max(max_depth, len(loop_indents))
    return max_depth
