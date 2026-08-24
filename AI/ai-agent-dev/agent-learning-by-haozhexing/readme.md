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

#### 5.5 
[Plan-then-Execute和ReAct是如何结合的？](https://qianwen.my.cn/share/chat/e12a9597f1c1441db8cdaea175ae2d4d)

