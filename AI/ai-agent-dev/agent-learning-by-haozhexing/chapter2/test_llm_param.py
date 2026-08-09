from openai import OpenAI

from unified_llm_client import default_qwen_client


def test_temperature():
    """对比不同 Temperature 的输出效果"""
    # 测试创意写作（高 Temperature 更好）
    # Temperature=0.0：每次输出完全相同（绝对确定性）
    # Temperature=0.7：有一定变化，语言流畅自然
    # Temperature=1.5：高度发散创意，但词汇可能跳跃或不连贯

    temperatures = [0.0, 0.7, 1.5]
    prompt = "用一句话描述春天"
    for temp in temperatures:
        print(f"\n{'=' * 50}")
        print(f"Temperature = {temp}")
        print('=' * 50)

        for i in range(3):
            response = default_qwen_client.chat(
                messages=[{"role": "user", "content": prompt}],
                temperature = temp,
                max_tokens=50
            )
            print(f"  运行 {i + 1}：{response.choices[0].message.content}")



TEMPERATURE_GUIDE = {
    "代码生成": 0.1,          # 要求精确，低随机性
    "数据提取/JSON格式化": 0.0, # 完全确定性，防崩溃
    "问答/事实查询": 0.3,      # 稍微稳定
    "文案/摘要": 0.7,         # 平衡创意和准确
    "头脑风暴/创意": 1.0,      # 鼓励多样性
    "诗歌/创意写作": 1.2,      # 高创意
    "Agent 逻辑路由": 0.1,    # 工具调用需要极度稳定
    "对话/闲聊": 0.8,         # 自然对话
}


def chat_with_length_control():
    """控制输出长度"""
    message = "写一篇500字的文章"
    response = default_qwen_client.chat(
        messages=[{"role": "user", "content": message}],
        max_tokens=100  # 限制生成长度
    )

    usage = response.usage
    content = response.choices[0].message.content
    finish_reason = response.choices[0].finish_reason


    print({
        "content": content,
        "total_tokens": usage.total_tokens,
        "finish_reason": finish_reason  # "stop"=正常结束, "length"=达到上限被截断
    })

def demo_penalties():
    # 这两个参数帮助避免模型像车轱辘话一样重复自己
    response = default_qwen_client.chat(
        messages=[{"role": "user", "content": "列举3种不同的创业方向"}],

        # presence_penalty：存在惩罚（只要出现过就惩罚，鼓励开启新话题）
        # 范围：-2.0 到 2.0，正值降低话题重复率
        presence_penalty=0.5,

        # frequency_penalty：频率惩罚（用的次数越多越不想用，鼓励词汇丰富度）
        # 范围：-2.0 到 2.0，正值降低高频词
        frequency_penalty=0.3,
    )

    print(response.choices[0].message.content)

# 适合用于：需要列举多样选项、生成长篇不重复研报的场景


if __name__ == "__main__":

    # test_temperature()
    # chat_with_length_control()
    demo_penalties()
