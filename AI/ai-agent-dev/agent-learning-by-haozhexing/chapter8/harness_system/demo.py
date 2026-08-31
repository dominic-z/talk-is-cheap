# demo.py

import os
from agent import HarnessAgent


def main():
    # 配置
    workspace = os.path.join(os.path.dirname(__file__))
    api_key = os.environ.get("OPENAI_API_KEY")

    # 创建 Harness Agent
    agent = HarnessAgent(
        workspace=workspace,
        api_key=api_key,
        model="qwen3.7-flash",
    )

    # 执行任务
    task = """
    请在 src/utils/math_helpers.py 中实现以下函数：

    1. `factorial(n: int) -> int`：计算阶乘（n >= 0），n < 0 时抛出 ValueError
    2. `fibonacci(n: int) -> int`：返回第 n 个斐波那契数（0-indexed），n < 0 时抛出 ValueError

    要求：
    - 包含完整的类型注解
    - 包含 Google 风格的文档字符串
    - 在 tests/test_math_helpers.py 中编写单元测试，覆盖正常情况和边界情况
    """

    result = agent.execute(task)

    # 显示结果
    print("\n" + "=" * 60)
    print("📊 执行报告")
    print("=" * 60)
    print(f"状态：{'✅ 成功' if result['success'] else '❌ 失败'}")
    print(f"迭代次数：{result['iterations']}")

    if result.get("context_stats"):
        stats = result["context_stats"]
        print(f"最终上下文利用率：{stats.utilization:.1%}")

    if result.get("tool_audit"):
        print(f"\n{result['tool_audit']}")

    if result.get("validation"):
        v = result["validation"]
        print(f"\n验证结果：{'✅ 通过' if v.passed else '❌ 失败'}")
        if v.failures:
            print(f"失败原因：{v.failure_report}")


if __name__ == "__main__":
    main()
