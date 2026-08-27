# AGENTS.md — Harness Demo Project

## 项目概述
这是一个用于演示 Harness Engineering 的 Python 项目。

## 技术栈
- Python 3.11
- FastAPI（Web 框架）
- pytest（测试框架）
- ruff（Lint 工具）

## 强制工作流程
完成任何代码修改后，必须按顺序执行：
1. `pytest tests/ -v --tb=short`（运行测试）
2. `ruff check src/ --fix`（Lint 检查并自动修复）
3. 若有测试失败，修复后重新执行步骤 1

## 架构约束
- src/api/ → 只能调用 src/services/
- src/services/ → 只能调用 src/models/ 和 src/repositories/
- 禁止在 api 层直接执行数据库查询

## 禁止操作
- 禁止修改现有测试文件（除非明确修复测试 bug）
- 禁止硬编码任何 API Key 或密码
- 禁止删除 tests/ 目录下的任何文件
