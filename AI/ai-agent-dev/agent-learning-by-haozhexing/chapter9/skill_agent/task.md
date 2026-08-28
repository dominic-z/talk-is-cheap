# 背景与目标
我正在学习agent开发，并按照[构建可复用的技能系统](https://haozhe-xing.github.io/agent_learning/zh/chapter_skill/05_practice_skill_system.html)来开发一个Agent。但是网页中给的代码缺失了很多，你帮我补全。

# 要求

## 关于Skill的补全

### code_reviewer
1. 这是一个代码审查的skill，他的路径位于`skills/code_reviewer`下，他有一个`skill.md`说明，但是他没有`skill.md`提到的tool，你需要补全他所需的tool，尽可能简单。
2. 按照教程来说，他本应该是一个code-based skill，但我不确定`skill.md`加上代码文件，并且通过嵌入上下文来使用的这种模式是不是违背code-based.skill的模式，本质会不会仍然是prompt-based skill，如果你认为他违背了code-based skill的模式，请你直接修改为code-based skill


### report_writer

1. 这是一个报告撰写的skill，他的路径位于`skills/report_writer`下，他目前有一个空的`skill.md`和一个空的模板`skills/report_writer/templates/report.md`，请你补全。
2. 这是一个prompt-based skill

### data_analyst
1. 这是一个数据分析skill，他的路径位于`skills/data_analyst`，他有一个`skill.md`说明，但是他没有`skill.md`提到的tool，你需要补全他所需的tool，尽可能简单。

## 关于Agent主代码的修改
`agent.py`是agent的主代码文件，他在使用skill的时候，将skill内容嵌入上下文来实现。针对纯prompt-based的skill我能理解，但是对于具有部分tool的skill，我理解这样调用是不是不正确？他无法完成skill所需的tool调用。如果你也是同样想法，请你修改`agent.py`。