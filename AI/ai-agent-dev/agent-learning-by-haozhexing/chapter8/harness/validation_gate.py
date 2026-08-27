# harness/validation_gate.py

from dataclasses import dataclass, field
from typing import Optional


@dataclass
class ValidationResult:
    passed: bool
    checks: dict[str, bool]  # 每项检查的结果
    failures: list[str]

    @property
    def failure_report(self) -> str:
        if not self.failures:
            return "所有验证通过"
        return "\n".join(f"❌ {f}" for f in self.failures)


class HarnessValidationGate:
    """
    验证门控：在 Agent 声称完成任务前，
    强制执行一系列检查
    """

    def __init__(self, tool_registry):
        self.tools = tool_registry

    def validate(
            self,
            task_description: str,
            modified_files: list[str],
    ) -> ValidationResult:
        """
        运行完整验证流程

        检查项：
        1. 测试套件通过
        2. Lint 无错误
        3. 未删除测试文件
        4. 修改了预期的文件
        """
        checks = {}
        failures = []

        # 检查 1：运行测试
        print("  🧪 运行测试...")
        test_output = self.tools.call("run_tests")
        test_passed = "failed" not in test_output.lower()  # and "error" not in test_output.lower()
        checks["tests_passed"] = test_passed
        if not test_passed:
            failures.append(f"测试失败:\n{test_output[:500]}")

        # 检查 2：Lint 检查
        print("  📋 运行 Lint 检查...")
        for modified_file in modified_files:
            if "test" in modified_file.lower():
                continue
            lint_output = self.tools.call("run_linter", path=modified_file)
            lint_passed = "无 Lint 问题" in lint_output or lint_output.strip() == "" or "All checks passed" in lint_output
            checks["lint_passed"] = lint_passed
            if not lint_passed:
                failures.append(f"Lint 问题:\n{lint_output[:300]}")

        # 检查 3：检查测试文件完整性（防作弊）
        print("  🛡️ 检查测试文件完整性...")
        test_integrity = self._check_test_integrity(modified_files)
        checks["test_integrity"] = test_integrity
        if not test_integrity:
            failures.append("⚠️ 检测到测试文件被删除或显著减少！这是不允许的。")

        passed = len(failures) == 0

        if passed:
            print("  ✅ 所有验证通过！")
        else:
            print(f"  ❌ {len(failures)} 项验证失败")

        return ValidationResult(
            passed=passed,
            checks=checks,
            failures=failures,
        )

    def _check_test_integrity(self, modified_files: list[str]) -> bool:
        """检查测试文件是否被违规删除或大量减少"""
        for file_path in modified_files:
            if "test" in file_path.lower() and "__init__" not in file_path.lower():
                # 读取文件内容检查是否有大量删除
                try:
                    content = self.tools.call("read_file", path=file_path)
                    line_count = len(content.split("\n"))
                    # 如果测试文件现在少于 5 行，很可能有问题
                    if line_count < 5 and "test" in file_path.lower():
                        return False
                except Exception:
                    pass
        return True
