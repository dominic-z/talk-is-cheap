

# 究竟如何在TMD ubuntu虚拟机配置codex！！！

环境：在windows vmware 16里跑的ubuntu虚拟机。

一方面代码、环境都在ubuntu里，另一方面不希望ai agent接管宿主机。因此需要折腾。

## 先让虚拟机能够使用宿主机的魔法

参考：https://burgess-t.cn/2024/10/08/643

1. 虚拟机网络使用NAT模式，并且获取NAT的虚拟网卡地址，例如192.168.58.1
2. v2rayN开启：设置 -- 参数设置 -- 允许来自局域网连接，随后底部看到的，局域网对应的协议（socks/http）和端口号（10808/10809），同时配置路由模式为“全局”
3. 打开 Ubuntu，找到设置-网络-代理，改为手动(Manual)，输入 3，4 步的 IP 和 端口完成配置
4. 但是vs code或者ideaj并不会用到第三步的代理，因此要在环境变量里新增两个配置

```shell
vim ~/.bashrc

# 新增配置：
export HTTP_PROXY=http://192.168.58.1:10809
export HTTPS_PROXY=http://192.168.58.1:10809
export ALL_PROXY=socks5://192.168.58.1:10809
export NO_PROXY=localhost,127.0.0.1,192.168.49.0/24

# 刷新环境变量
source ~/.bashrc
```

## 配置ide插件

- vscode：安装codex插件即可，然后跳转登录（注意，一定关闭自动升级，tmd有一次自动升级给我codex干黑屏了，我用的26.623.70xxx），自动升级之后，vscode-help-toggledevelopertool里面一堆报错
- idea：安装最新版idea，开启右侧的aichat，然后跳转登录即可。

排查指南：v2ray的日志会实时展示请求的日志，正常情况下，不应该报错，如果日志中出现报错，可能还是部分流量没有走v2ray，例如我一开始没有配置环境变量里的代理，而只是配置了vscode中的应用代理，但是部分请求还是被漏掉了，导致v2ray中一堆报错


# AI 编程First Step

https://chatgpt.com/s/t_6a62d73c327481918662602e36dc6701

价格原因我先用Qoder了

## 第一阶段：建立 AI Coding 心智模型

LLM是什么、AI Coding Agent架构


https://chatgpt.com/s/t_6a62d9629dd88191a6114cf0a190e92d

AiAgent和人是一模一样的，当人类需要执行一个工作或者任务的时候。人类需要：
1. 大而全地学习背景知识：对应AiAgent训练过程

然后有人给我们布置了一个任务，我们需要：
1. 设计一个方案，我们可能要先理解任务，或者找任务发布者澄清任务细节：对应aigent的planning
2. 理解任务内容：将问题向量化
3. 检索任务相关的知识：通过任务向量进行rag寻找相关的外部知识、整合memory（来自历史交互、偏好）、查看当前文件目录、当前项目目录、阅读agent.md等等，构建context
4. 汇总任务相关的信息：基于context和我们的prompt构建完整的模型。
5. 设计计划：设计任务完成的具体方案，生成了一个plan
6. 看到这个plan，我思考该如何执行方案的某一步，并且根据执行过程不断调整方案、甚至生成子方案：对应到ai，根据任务内容进行reAct，并使用tool真正的去操作系统接口，例如读写文件，执行结果等等。基于上述执行结果，例如读取到了新的文件、发现了一些新问题，就会更新plan、甚至拆解成多个plan，这个就是plan和react
7. 然后重复上述过程。

## 第二阶段：必须掌握的核心概念（3~5天）

### Context

```
                 Context

                    |
     --------------------------------
     |              |               |

 AGENTS.md        Memory          RAG

 项目规则       用户偏好       项目知识

```

比如Qoder的设置中，就有一个Memory的配置，用来描述我使用过程的记忆

### RAG

在传统的没有Transformer模型的时候，RNN或者LSTM是语言模型的标准神经元，这个东西的目的是让神经元能够记录前文信息。但是在LLM中，我问一个问题，没有上下文的基础上，LLM无法给出准确的答案，因此LLM需要基于问题构建一个上下文，也就是形成一个context，并基于context和我的问题形成完整的prompt，让llm进行回答。

https://www.qianwen.com/share/chat/764dd28b079043f59e5fcd72d578f553


## 第三阶段：学习 Codex 实战能力（5~7天）

### Task 1：让 Codex理解项目

```
请分析这个项目：

1. 技术架构

2. 模块关系

3. 核心流程

4. 给我生成architecture.md
```

### Task 2：创建 AGENTS.md

```
生成一个AGENTS.md，包括：
1. 技术栈
2. 代码规范
3. 测试规范
4. 架构约束
```

### Task 3：让Codex完成一个真实需求

用的Qoder CN

https://chatgpt.com/s/t_6a644c9bc0148191828d326a793e8bc0


## 第四阶段：Skill

使用qoder CN基于
```
/​create-skill​ 基于docs/workflow.md创建一个仅本项目可以使用的skill
```

```
# 开发工作流

收到一个位于 tasks/ 下的新任务时，必须遵循以下流程：

## Phase 1：分析

读取：

- tasks/<task>/01-task.md
- tasks/<task>/02-context.md
- tasks/<task>/03-acceptance.md

生成：

tasks/<task>/04-plan.md

plan 中必须包含：

- 修改哪些模块
- 新增哪些文件
- 修改哪些文件
- 风险分析
- 测试方案

暂停，等待用户确认。

---

## Phase 2：开发

收到确认后：

按照 plan 实现代码。

---

## Phase 3：验收

开发完成后：

生成：

tasks/<task>/05-result.md

必须包含：

- 修改文件列表
- 新增文件列表
- 删除文件列表
- 测试结果
- 已完成的验收项
- 未完成项
```

随后可以通过
```
按照 free-flow-dev-workflow skill 的流程，读取 tasks/20260725-lost-worker-task-reschedule/ 目录下的 task.md、context.md 和 acceptance.md，执行 Phase 1 分析阶段，生成修改计划并等待确认。
```