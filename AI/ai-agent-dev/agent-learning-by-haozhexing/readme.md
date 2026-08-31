# 概述

来自[agent-learning](https://haozhe-xing.github.io/agent_learning/zh/)


# 零碎笔记

## 第一部分入门篇
### 第1章 什么是Agent？

#### 1.2 Agent的核心概念与定义

关于“特征3：推理能力（Reasoning）”，目前工业界主流 Agent 并不是 ToT，而是以 ReAct（或者 ReAct 的变体）为主。CoT 更多作为内部推理能力存在，ToT/MCTS 类搜索只在少数复杂任务中使用。也就是说，CoT基本上可以理解为ReAct的reason部分。

```
用户任务
      |
      v
Planner
(制定计划，先做什么、再做什么，例如首先引入xx包，然后开发xx代码，更注重执行)
      |
      v
CoT
(分析问题，例如，数据库压力可能较大，需要加缓存，需要考虑哪些方面，更注重思考)
      |
      v
Action
(调用工具)
      |
      v
Observation
(获得真实结果)

      |
      v
CoT
(重新分析)
      |
      v
完成任务
```


#### 1.6 智能体发展史

练习3
```python

def agent(prompt):
    observations = Obj();
    context = Context();
    context.constraint = "你必须控制思考两轮以内。"
    while true:

        if not llm.make_any_progress():
            context.hint = "最近2次思考没有进展，需要重新考虑其他方向"
        else:
            context.hint = ""
        plan,thought,action = llm.thought(promt,context)
        if action == 'finish':
            break
        action_res = agent.do(action)
        context.add_observation(plan,thought,action,action_res)
        



```


### 第2章 大语言模型基础
#### 2.1 

关于迷失在中间 (Lost in the middle)。因为长上下文会导致注意力被稀疏。在少上下文的时候，预测下一个词的时候，可以确保权重关注下一个词相关的prompt，但是长上下文会导致权重被稀释。


#### 2.4 模型调用API入门

换成了qwen的API，也能兼容openai的接口，舒服：https://platform.qianwenai.com/docs/developer-guides/getting-started/first-api-call

OpenAI的接口里，message是个很重要的字段，里面有很多范式，参考：https://developers.openai.com/api/reference/resources/chat/subresources/completions/methods/create

#### 2.5 Token Temperature与模型参数

Temperature vs Top-p 的区别：https://chatgpt.com/s/t_6a77ef551248819198756e20a20acbf9

在实际 LLM 应用里，Temperature 更像“创造力旋钮”，Top-p 更像“候选空间边界”。对于 Agent，通常优先调低 temperature，因为稳定性比创造性更重要。


presence_penalty和frequency_penalty的区别

## 第二部分 核心能力篇
### 第5章 规划与推理
#### 5.1 Agent 如何"思考"？

心理模型：为什么直接询问通常无效？就像人类做题一样，答案是一步步推导出来的，而不是一下子就能从题目算出来的，所以，让LLM生成中间步骤，然后通过ReAct工作循环将这些中间步骤作为新的上下问题提交给LLM，能让LLM更加准确地输出正确结果。

关于：OODA 循环，图里的A和D应该反了。

> 在AiAgent开发领域，我知道了CoT、ReAct，但是我问题是，thought和reasoning两个概念看起来都是思考的过程，他们的区别是什么？有必要区分开么？
> 
> https://chatgpt.com/share/6a8c61a3-19e0-83ea-81f3-0765be7c3ce9
> 
> 比如deepseek模型刚出现的时候，每问一个问题，deepseek会先自己絮絮叨叨说很多自己的思考过程，这个过程可以称为reasoning，而产出的这些思考过程结果可以被称为thought，我这样理解对么？
> 
> 对，基本可以这样理解，而且你这个理解已经比较接近 Agent/LLM 领域里这两个词的实际关系了。
> 不过我建议你加一个小修正：
> > Reasoning 更准确地说是“推理过程”，Thought 是这个推理过程中产生的某个“中间思考步骤/状态”。
> 
> > Reasoning 是上位概念；Thought、Plan、Decision 是 reasoning 过程中不同类型的中间产物；Action 则是 reasoning/decision 之后对外部世界的实际操作。

#### 5.5  自动化研究助手 Agent

[自动化研究助手 Agent 相关论文](https://chatgpt.com/s/t_6a8cfbde5168819180a4740da22e3a36)

| 工作                      | 你应该把它理解成                                           |
| ----------------------- |------------------------------------------------------------|
| **WebGPT**              | **让 LLM 学会浏览网页**                                    |
| **STORM**               | **让 LLM 学会从多个角度思考“应该研究什么”**                |
| **MindSearch**          | **让研究计划能够随着搜索结果动态扩展**                     |
| **WebSailor**           | **让 Agent 学会处理极难、极不确定的 Web 搜索（交叉验证）** |
| **The AI Scientist**    | **让 Research Agent 从“查资料”进一步走向“做实验”**         |
| **Deep Research**       | **把这些能力组合成真正可用的长程研究 Agent**               |
| **BrowseComp**          | **给 Web Agent 出一套高难度考试题**                        |
| **Open Deep Research**  | **把 Deep Research 的工程架构开源出来**                    |
| **Tongyi DeepResearch** | **开始专门训练擅长 Deep Research 的模型**                  |


#### 5.6 Plan-and-Execute 与 Test-time Compute Scaling

[在ai agent领域，plan-then-execute和ReAct，我觉得非常相似，我感觉plan本身是Reasoning的一种体现，而execute和action本身也是相似的。对么？](https://chatgpt.com/s/t_6a8cfeb7f2888191a728aec5d2317759)
> Reasoning 和 Action 是“能力维度”，而 Plan-Then-Execute / ReAct 是“组织这些能力的工作流方式”。


引申阅读：
1. 推理模型是什么，与传统的LLM模型有啥区别？
2. CoT和Test-time Compute Scaling到底指的是什么？——同一个问题，推理模型可以比传统模型多生成一大段文本，而这段文本承担了推理过程的作用。就像DeepSeek刚出的时候，会絮絮叨叨一大段思考过程。这个就是cot和Test-time Compute Scaling的一种具体体现。另外：传统的 CoT 往往依赖用户在 Prompt 中写“请一步步思考”来激发。而 DeepSeek-R1 的思考过程是通过大规模强化学习（RL） 训练出来的内化能力。
3. 推理模型之所以有推理能力，推理模型确实会使用大量“包含推理过程”的训练数据，但它之所以形成强推理能力，并不只是因为训练数据里多了 CoT 文本，更关键的是后续的强化学习让模型学会“自己产生有效的推理过程”。
### 第6章 检索增强生成（RAG）


#### 6.2 文档加载与文本分割

[在rag场景中，为啥要对检索出来的文本分chunk？](https://chatgpt.com/s/t_6a8d538bbe0c8191b080b2c3f997e6af)


#### 6.7 进阶 RAG：GraphRAG 与 Agentic RAG 工程实战

chai老师还是专业。
[GraphRag出现的背景是什么，他解决naive rag的什么问题？]([https://chatgpt.com/s/t_6a8dae3c8d7881919ea5749e8c9ec207](https://chatgpt.com/share/6a8db1e0-a23c-83e9-8f51-b4bd93538795))



### 第7章  上下文工程


#### 7.3 长时程任务的上下文策略

[在ai agent里，压缩整合在什么情况下可能造成关键信息丢失？如何设计一个"安全网"来缓解这个问题？](https://qianwen.my.cn/share/chat/e66a5a515f334c309a166cd8bbe024c5)

### 第8章  harness engineering

[什么是harness](https://chatgpt.com/share/6a8faf3a-3208-83ea-b113-802394c75011)


#### 8.1 什么是harness engineering？

关于文中提到的上下文利用率，我的理解是，一个模型的上下文窗口比如说是128k，如果128k用满了，这个就是上下文利用率过高。

#### 8.5 实战：构建你的第一个 Harness 系统

好例子，但重点在思路，在demo代码的基础上做了一些调整。

### 第10章

#### 10.1 什么是Agentic-RL

https://chatgpt.com/share/6a91a23c-fed0-83ea-ab77-d064f037846a

SFT可以让模型知道专家是怎样做的，RL能够让模型自己尝试，试出更优的路径。就好比大侠向某个掌门学会了一套武功，但还是要日后自己和其他大侠切磋才能有自己的体会、才能进入更高的境界一样。



#### 11.1 Prompt自动调优

##### 11.1.5 自动生成Prompt

"prompt" 这个词在日常语境里是**泛称**（泛指"喂给模型的文本"），但在工程/接口层面，它其实可以拆成几个**不同角色、不同来源**的部分。OpenAI 的 `messages` 数组正是用 `role` 来显式区分这些部分的：

| 你的问题 | OpenAI 里的 role | 一般叫法 | 来源 |
|---|---|---|---|
| 用户的输入 | `user` | **user message / 用户消息** | 终端用户 |
| system 部分 | `system` | **system prompt / 系统提示** | 开发者/系统设定 |
| agent 循环里 LLM 自己生成、喂回下一轮的输入 | `assistant` / `tool` | **assistant message / tool message**（统称**模型自生成消息**） | LLM / 工具 |

关键认知：**`user`、`system`、`assistant`、`tool` 都属于"送给 LLM 的上下文"的一部分，但把它们笼统全叫 prompt 会丢失信息。** 更严谨的说法是：整个 `messages` 数组合起来叫 **prompt（泛称）/ context（上下文）**，而里面的每一段按 role 各有专名。

假设做一个"翻译 Agent"：

```json
[
  {"role": "system",    "content": "你是一个严谨的翻译助手，只输出译文，不解释。"},   // ← system prompt（开发者设定）
  {"role": "user",      "content": "请把'今天天气真好'翻译成英文。"},                // ← user message（用户输入）
  {"role": "assistant", "content": "The weather is really nice today."},           // ← assistant message（LLM 生成，可能直接是答案，也可能触发工具）
  {"role": "tool",      "content": "{\"glossary\": {\"天气\": \"weather\"}}"},       // ← tool message（工具返回，喂回下一轮）
  {"role": "assistant", "content": "The weather is really nice today."}             // ← 下一轮 LLM 基于上面所有内容再生成
]
```



**APE（ICLR 2023）：让 LLM 自动写 Prompt**

一个完整的任务描述，应当是：
```json
{role: system, content: "你是客服助手，请根据知识库回答退款相关问题。注意区分无理由退款和质量问题售后。"},
{role: user, content: "签收 7 天能退款吗？"}
```

但是实际情况下，用户只会抛出`签收 7 天能退款吗？`一个问题。

所以我们要根据用户的问题，自动生成一个system prompt（即上文的`system`）部分。

APE的方式是，他对模型提供了`签收 7 天能退款吗？`这种用户问题，并给这一条问题匹配了一个最优质的回答。让模型先根据用户的用户输入生成一堆prompt，然后分别组装，总给llm，与最优质回答比较，相似的得分高，得分高的对应的prompt，就是最好的prompt，ape最后得到了，如下的这一堆东西，后续当来了一个新的用户输入时候，路由层会根据用户输入找一个最相似的prompt，然后把用户输入和这个prompt当做一个完整的上下文，送给llm。
```
task_id_A → prompt_A
task_id_B → prompt_B
task_id_C → prompt_C
```

**实际项目中如何落地 Prompt 自动调优？**

它讲的是工程上怎么把 prompt 自动调优跑成一个可持续的系。
```
Prompt仓库 → 任务样本集 → Runner(跑出trace) → Evaluator(打分+文字反馈)
           → Reflector(分析失败原因、定位到模块) → Rewriter(LLM改写prompt)
           → Selector(选最优候选保留版本)

```
这段落地文档讲的不是"用 LLM 从任务描述冷启动生成 任务→prompt 映射"（那是 APE）。它讲的是：已有一个按模块拆好的 prompt 仓库后，如何用 LLM 当 Rewriter，结合 Evaluator/Reflector 的失败反馈，持续把每个模块的 prompt 改写成更好的版本，并用 Selector + 版本管理挑留最优。 本质是"模块→prompt"的迭代进化系统，而不是从零造映射。

比如面的数据记录了`intent_classifier`任务的一个好的prompt是什么
```
PROMPTS = {
    "intent_classifier": "...",
    "planner": "...",
    "tool_selector": "...",
    "reader": "...",
    "verifier": "...",
    "final_answer": "...",
}
```