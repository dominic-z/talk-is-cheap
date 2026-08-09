# 异步流式输出（生产环境推荐）
import asyncio

from client_utils import async_client,QWEN_3_7_FLASH

# https://qianwen.my.cn/share/chat/9fdd1bedad88435789d3bc2fb20e1460
# 就是js的异步思路
async def async_stream_chat(message: str) -> str:
    """异步流式输出"""
    stream = await async_client.chat.completions.create(
        model=QWEN_3_7_FLASH,
        messages=[{"role": "user", "content": message}],
        stream=True
    )

    full_response = ""
    async for chunk in stream:
        if len(chunk.choices)!=0 and chunk.choices[0].delta.content is not None:
            content = chunk.choices[0].delta.content
            print(content, end="", flush=True)
            full_response += content

    return full_response


# 运行异步函数
asyncio.run(async_stream_chat("解释一下什么是异步编程"))
