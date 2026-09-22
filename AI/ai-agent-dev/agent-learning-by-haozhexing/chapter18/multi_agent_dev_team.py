# examples/dev_team/dev_team.py
# 一个多agent协作的例子，真实做agent开发要考虑的东西还真挺多的。
from __future__ import annotations

import os
import subprocess
import sys
import tempfile
from functools import partial
from typing import Optional, Protocol, TypedDict

from pydantic import BaseModel, Field

from langgraph.graph import START, END, StateGraph
import os
from langchain_openai import ChatOpenAI

llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1")


class DevArtifact(BaseModel):
    """LLM 的结构化产出：实现代码 + 测试代码。"""
    source_code: str = Field(description="源代码，不能包含前后缀，内容将被直接写入到代码文件中，例如定义函数/类，本身不应当包含测试代码")
    source_code_filename: str = Field(description="app模块的文件名称，例如app.py")
    test_code: str = Field(description="pytest 测试代码，不能包含前后缀，内容将被直接写入到代码文件中，需能从源代码导入被测对象并覆盖主要逻辑，注意导入的衔接性")
    test_code_filename: str = Field(description="测试代码的文件名称，例如app_test.py")


# Protocol 来自 typing 模块（第 9 行导入），用于声明一个接口约定：只要某个类"长成这样"（拥有约定中的方法、签名一致），它就被认为"符合这个协议"，而不需要显式继承它。
#
# 这是"鸭子类型"的静态类型版本：
#
# 如果它走起来像鸭子、叫起来像鸭子，那它就是鸭子。

class DevState(TypedDict):
    original_requirement: str
    workspace_dir: str  # 各角色把产物写到这个目录
    product_spec: Optional[str]
    technical_design: Optional[str]
    implementation: Optional[str]
    src_filename: Optional[str]
    test_filename: Optional[str]
    test_output: Optional[str]  # pytest 的真实输出（不是"假装通过"）
    test_passed: Optional[bool]  # 测试是否通过（客观判据）
    deployment_config: Optional[str]
    documentation: Optional[str]
    current_phase: str
    iteration: int
    max_iterations: int
# 例如下面的DeterministicDeveloper以及llmDeveloper


class Developer(Protocol):
    """代码生成器：输入设计，输出 (源码, 测试源码)。"""

    def develop(self, design: str, requirement: str) -> DevArtifact: ...


class DeterministicDeveloper:
    """离线开发者：写出一份已知能通过测试的模块，保证图可跑可测。"""

    def develop(self, design: str, requirement: str) -> DevArtifact:
        code = (
            "def add(a: int, b: int) -> int:\n"
            "    return a + b\n"
            "\n"
            "def multiply(a: int, b: int) -> int:\n"
            "    return a * b\n"
        )
        test_code = (
            "from app import add, multiply\n"
            "\n"
            "def test_add():\n"
            "    assert add(2, 3) == 5\n"
            "    assert add(-1, 1) == 0\n"
            "\n"
            "def test_multiply():\n"
            "    assert multiply(2, 3) == 6\n"
        )
        return DevArtifact(source_code=code,source_code_filename="app.py",test_code=test_code,test_code_filename="app_test.py")


class LLMDeveloper:
    """真实开发者：调用 LLM 生成代码与测试，输出为结构化对象。"""

    def __init__(self, llm):
        self.llm = llm
        # 绑定结构化输出解析器，让 LLM 直接返回 DevArtifact
        self._structured = llm.with_structured_output(DevArtifact)

    def develop(self, design: str, requirement: str) -> DevArtifact:
        # 把 design + requirement 交给 LLM，由结构化输出解析为 DevArtifact
        messages = [
            ("system",
             "你是一名资深 Python 工程师。请根据给定的技术方案与需求，"
             "生成可运行的实现代码与对应的 pytest 测试代码，"
             "严格按要求的字段结构输出，不要添加额外解释。"),
            ("human",
             f"# 原始需求\n{requirement}\n\n# 技术方案\n{design}\n"),
        ]
        artifact = self._structured.invoke(messages)
        return artifact


# ---------- 节点 ----------

def product_manager(state: DevState) -> dict:
    # 真正由 LLM 把原始需求转化为产品规格（用户故事 + 验收标准）
    messages = [
        ("system",
         "你是一名产品经理，负责把用户需求转化为清晰的产品规格"
         "（用户故事 + 验收标准）。只输出规格文本，不要解释。"),
        ("human", f"需求：{state['original_requirement']}"),
    ]
    spec = str(llm.invoke(messages).content)
    return {"product_spec": spec, "current_phase": "product_manager"}


def architect(state: DevState) -> dict:
    # 真正由 LLM 依据产品规格产出技术方案（语言、模块划分、测试策略）
    messages = [
        ("system",
         "你是一名架构师，负责根据产品规格产出技术方案"
         "（语言、模块划分、测试策略）。只输出方案文本，不要解释。"),
        ("human", f"产品规格：\n{state['product_spec']}"),
    ]
    design = str(llm.invoke(messages).content)
    return {"technical_design": design, "current_phase": "architect"}


def developer(state: DevState, dev: Developer) -> dict:
    # 关键区别：开发工程师把代码真正写到磁盘，而不是只返回一段文本
    dev_artifact = dev.develop(state["technical_design"], state["original_requirement"])
    src_filename = dev_artifact.source_code_filename
    test_filename = dev_artifact.test_code_filename
    ws = state["workspace_dir"]
    with open(os.path.join(ws, src_filename), "w", encoding="utf-8") as f:
        f.write(dev_artifact.source_code)
    with open(os.path.join(ws, test_filename), "w", encoding="utf-8") as f:
        f.write(dev_artifact.test_code)
    return {
        "implementation": dev_artifact.source_code,
        "src_filename":dev_artifact.source_code_filename,
        "test_filename":dev_artifact.test_code_filename,
        "current_phase": "developer",
        "iteration": state.get("iteration", 0) + 1,
    }


def tester(state: DevState) -> dict:
    # 注意：这里真正运行 pytest，而不是"假装测试通过"
    ws = state["workspace_dir"]
    # 用当前 Python 解释器（sys.executable）在产物目录 cwd=ws 下启动子进程跑 pytest：
    # - sys.executable 保证子进程与主脚本用同一个 conda 环境，依赖找得到；
    # - "-m pytest" 比裸 pytest 更稳，能正确处理 app_test.py 的 from app import ...；
    # - capture_output / text 把标准输出与错误收成字符串，供后续节点判读。
    proc = subprocess.run(
        [sys.executable, "-m", "pytest", state['test_filename'], "-q"],
        cwd=ws,
        capture_output=True,
        text=True,
    )
    return {
        "test_output": proc.stdout + proc.stderr,
        "test_passed": proc.returncode == 0,
        "current_phase": "tester",
    }


def devops(state: DevState) -> dict:
    dockerfile = (
        "FROM python:3.12-slim\nWORKDIR /app\nCOPY . .\n"
        "RUN pip install pytest\nCMD [\"python\", \"-m\", \"pytest\", \"-q\"]\n"
    )
    with open(os.path.join(state["workspace_dir"], "Dockerfile"), "w", encoding="utf-8") as f:
        f.write(dockerfile)
    return {"deployment_config": dockerfile, "current_phase": "devops"}


def docs(state: DevState) -> dict:
    readme = (
        "# 自动生成的项目\n\n由多 Agent 开发团队产出。\n\n"
        "## 测试\n\n```bash\npytest -q\n```\n"
    )
    with open(os.path.join(state["workspace_dir"], "README.md"), "w", encoding="utf-8") as f:
        f.write(readme)
    return {"documentation": readme, "current_phase": "docs"}


def route_after_test(state: DevState) -> str:
    """测试通过后交付；未通过且未达上限则回到开发重试。"""
    passed = state.get("test_passed")
    hit_cap = state.get("iteration", 0) >= state.get("max_iterations", 3)
    if passed or hit_cap:
        return "deliver"
    return "fix"


def build_graph(dev: Developer):
    g = StateGraph(DevState)
    g.add_node("product_manager", product_manager)
    g.add_node("architect", architect)
    # 注意：这里的 `developer` 是节点函数；不要把同名变量（开发者实例）绑到模块顶层，
    # 否则会遮蔽该函数，导致 partial(developer, ...) 报 "first argument must be callable"。
    g.add_node("developer", partial(developer, dev=dev))
    g.add_node("tester", tester)
    g.add_node("devops", devops)
    g.add_node("docs", docs)

    g.add_edge(START, "product_manager")
    g.add_edge("product_manager", "architect")
    g.add_edge("architect", "developer")
    g.add_edge("developer", "tester")
    g.add_conditional_edges("tester", route_after_test, {
        "deliver": "devops",
        "fix": "developer",
    })
    g.add_edge("devops", "docs")
    g.add_edge("docs", END)
    return g.compile()


def develop(requirement: str, dev: Optional[Developer] = None,
            workspace_dir: Optional[str] = None) -> dict:
    dev = dev or DeterministicDeveloper()
    ws = workspace_dir or tempfile.mkdtemp(prefix="devteam_")
    os.makedirs(ws, exist_ok=True)
    app = build_graph(dev)
    # 原一次性调用（保留以便对照）：
    # result = app.invoke({
    #     "original_requirement": requirement,
    #     "workspace_dir": ws,
    #     "product_spec": None,
    #     "technical_design": None,
    #     "implementation": None,
    #     "test_output": None,
    #     "test_passed": None,
    #     "deployment_config": None,
    #     "documentation": None,
    #     "current_phase": "init",
    #     "iteration": 0,
    #     "max_iterations": 3,
    # })
    # 改为流式调用：逐步观察每个节点的执行过程与产出
    initial_state = {
        "original_requirement": requirement,
        "workspace_dir": ws,
        "product_spec": None,
        "technical_design": None,
        "implementation": None,
        "test_output": None,
        "test_passed": None,
        "deployment_config": None,
        "documentation": None,
        "current_phase": "init",
        "iteration": 0,
        "max_iterations": 3,
    }
    result: dict = initial_state.copy()
    for chunk in app.stream(initial_state):
        for node_name, update in chunk.items():
            print(f"\n===== 节点: {node_name} =====")
            for k, v in update.items():
                if k == "current_phase":
                    continue
                # 长文本（代码/规格）截断，避免刷屏
                shown = v if not (isinstance(v, str) and len(v) > 500) else v[:500] + " ...(已截断)"
                print(f"  {k}: {shown}")
            result.update(update)
    return result


if __name__ == "__main__":
    # 变量名用 dev，别用 developer——否则会覆盖上面的 developer 节点函数（命名空间共享）
    dev = LLMDeveloper(llm)
    out = develop("作为用户，我希望有一个能够进行加、减、乘运算的python代码，功能很简单，不用创建多个嵌套文件夹，就在工作目录下创建源代码和测试代码即可",
                  dev, workspace_dir="gitignore")
    print("测试通过：", out["test_passed"])
    print("产物目录：", out["workspace_dir"])
    print(out["test_output"])
