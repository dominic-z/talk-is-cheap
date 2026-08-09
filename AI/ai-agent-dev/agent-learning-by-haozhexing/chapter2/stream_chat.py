from client_utils import client

def stream_chat(message: str, system: str = None) -> str:
    """流式输出，实时打印生成内容"""

    messages = []
    if system:
        messages.append({"role": "system", "content": system})
    messages.append({"role": "user", "content": message})

    # stream=True 开启流式模式
    stream = client.chat.completions.create(
        model="qwen3.7-flash",
        messages=messages,
        stream=True
    )

    full_response = ""
    print("助手：", end="", flush=True)

    for chunk in stream:
        # 每个 chunk 包含一小段文本
        if len(chunk.choices)!=0 and chunk.choices[0].delta.content is not None:
            content = chunk.choices[0].delta.content
            print(content, end="", flush=True)
            full_response += content

    print()  # 换行
    return full_response


# 测试流式输出
result = stream_chat("写一首关于 Python 的短诗")
print(f"\n完整内容（{len(result)} 字）")
