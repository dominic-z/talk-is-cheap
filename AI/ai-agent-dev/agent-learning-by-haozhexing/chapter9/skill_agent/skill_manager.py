# skill_manager.py
import os
import yaml
import hashlib
import importlib.util
import inspect
from pathlib import Path
from openai import OpenAI


class Skill:
    """技能数据类"""

    def __init__(self, name, description, content,
                 version="1.0", tags=None, tools=None, path=None):
        self.name = name
        self.description = description
        self.content = content  # SKILL.md 的完整内容
        self.version = version
        self.tags = tags or []
        self.tools = tools or []
        self.path = path

    def __repr__(self):
        return f"Skill(name='{self.name}', version='{self.version}')"


class SkillManager:
    """技能管理器：加载、注册、发现、选择"""

    def __init__(self, skills_dir: str = "skills"):
        self.skills_dir = Path(skills_dir)
        self.skills: dict[str, Skill] = {}
        self._load_all_skills()

    def _load_all_skills(self):
        """扫描技能目录，加载所有技能"""
        if not self.skills_dir.exists():
            print(f"⚠️ 技能目录不存在: {self.skills_dir}")
            return

        for skill_dir in self.skills_dir.iterdir():
            if skill_dir.is_dir():
                skill_file = skill_dir / "SKILL.md"
                if skill_file.exists():
                    skill = self._parse_skill_file(skill_file)
                    skill.tool_funcs = self._load_tools(skill)
                    self.skills[skill.name] = skill
                    tool_names = list(skill.tool_funcs.keys())
                    suffix = f"，工具: {', '.join(tool_names)}" if tool_names else ""
                    print(f"  📦 已加载技能: {skill.name} (v{skill.version}){suffix}")

    def _load_tools(self, skill: Skill) -> dict:
        """加载技能目录下的 tools.py，返回工具名到函数的映射"""
        if not skill.path:
            return {}
        tools_file = Path(skill.path).parent / "tools.py"
        if not tools_file.exists():
            return {}

        # —— 第一步：动态加载 tools.py 为 Python 模块 ——
        # 按文件路径（而非包内导入路径）用 importlib 加载模块；
        # 模块名唯一化（skill_tools_<技能名>），避免不同技能的同名工具冲突。
        module_name = f"skill_tools_{skill.name.replace('-', '_')}"
        spec = importlib.util.spec_from_file_location(module_name, tools_file)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)  # 执行模块代码，此时模块内的函数已定义

        # —— 第二步：从模块中提取公开的工具函数 ——
        # 遍历模块命名空间：跳过下划线开头的私有成员（含 import 的模块等）
        # 和非函数对象，只保留真正的 def 函数；
        # 若 SKILL.md 的 frontmatter 声明了 tools 列表，则只提取声明过的函数，
        # 否则提取全部公开函数。
        declared = set(skill.tools)
        funcs = {}
        for name, obj in vars(module).items():
            if name.startswith("_") or not inspect.isfunction(obj):
                continue
            # 若 SKILL.md 声明了 tools，只加载声明过的；否则加载全部公开函数
            if declared and name not in declared:
                continue
            funcs[name] = obj
        return funcs

    def _parse_skill_file(self, path: Path) -> Skill:
        """解析 SKILL.md 文件"""
        content = path.read_text(encoding="utf-8")

        # 解析 YAML frontmatter
        metadata = {}
        if content.startswith("---"):
            parts = content.split("---", 2)
            if len(parts) >= 3:
                metadata = yaml.safe_load(parts[1])
                body = parts[2].strip()
            else:
                body = content
        else:
            body = content

        return Skill(
            name=metadata.get("name", path.parent.name),
            description=metadata.get("description", ""),
            content=body,
            version=metadata.get("version", "1.0"),
            tags=metadata.get("tags", []),
            tools=metadata.get("tools", []),
            path=str(path)
        )

    def get_skill(self, name: str) -> Skill:
        """获取指定技能（兼容连字符 / 下划线两种写法）"""
        if name in self.skills:
            return self.skills[name]
        for variant in (name.replace("_", "-"), name.replace("-", "_")):
            if variant in self.skills:
                return self.skills[variant]
        raise ValueError(f"未找到技能: {name}。可用技能: {list(self.skills.keys())}")

    def list_skills(self) -> list[dict]:
        """列出所有技能的摘要"""
        return [
            {
                "name": skill.name,
                "description": skill.description,
                "version": skill.version,
                "tags": skill.tags
            }
            for skill in self.skills.values()
        ]

    def discover(self, task: str) -> list[Skill]:
        """根据任务描述发现相关技能（简单版：关键词匹配）"""
        task_lower = task.lower()
        scored_skills = []

        for skill in self.skills.values():
            score = 0
            # 检查描述匹配
            if any(word in skill.description.lower()
                   for word in task_lower.split()):
                score += 2
            # 检查标签匹配
            for tag in skill.tags:
                if tag.lower() in task_lower:
                    score += 3
            # 检查技能名匹配
            if skill.name.replace("-", " ") in task_lower:
                score += 5

            if score > 0:
                scored_skills.append((skill, score))

        # 按分数降序排列
        scored_skills.sort(key=lambda x: x[1], reverse=True)
        return [skill for skill, _ in scored_skills]

    def get_skill_summaries_prompt(self) -> str:
        """生成技能摘要文本（用于 LLM 决策）"""
        lines = ["你具备以下技能，请根据用户的任务选择最合适的技能：\n"]
        for skill in self.skills.values():
            lines.append(f"- **{skill.name}**: {skill.description}")
            lines.append(f"  标签: {', '.join(skill.tags)}")
            lines.append("")
        return "\n".join(lines)
