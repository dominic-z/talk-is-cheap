# main.py
from skill_manager import SkillManager
from agent import SkillAgent


def main():
    print("=" * 60)
    print("  🤖 技能驱动的 Agent 系统")
    print("=" * 60)

    # 1. 初始化技能管理器
    print("\n📂 加载技能...")
    manager = SkillManager(skills_dir="skills")

    # 2. 显示可用技能
    print(f"\n✅ 已加载 {len(manager.skills)} 个技能:")
    for skill_info in manager.list_skills():
        print(f"  - {skill_info['name']}: {skill_info['description']}")

    # 3. 初始化 Agent
    agent = SkillAgent(manager)

    # 4. 交互循环
    print("\n" + "-" * 60)
    print("开始对话（输入 'quit' 退出，'skills' 查看技能列表）")
    print("-" * 60)

    while True:
        user_input = input("\n🧑 你: ").strip()

        if user_input.lower() == "quit":
            print("👋 再见！")
            break
        elif user_input.lower() == "skills":
            for skill_info in manager.list_skills():
                print(f"  📦 {skill_info['name']}: {skill_info['description']}")
            continue
        elif not user_input:
            continue

        response = agent.chat(user_input)
        print(f"\n🤖 Agent: {response}")


if __name__ == "__main__":
    # 例如输入skills 可以列出所有skill
    # code-reviewer examples/bad_code_sample.py
    # data-analyst  examples/sample_sales.csv
    # report_writer  关于海尔市场调研的报告
    main()
