import tiktoken  # Token 计数库（注：此处以 OpenAI 体系为例演示核心原理，2026 年不同厂商如 Qwen、Gemini 均有专属的 Tokenizer，但底层逻辑相通）


def count_tokens(text: str, model_encoding: str = "cl100k_base") -> int:
    """计算文本的 Token 数量"""
    encoding = tiktoken.get_encoding(model_encoding)
    tokens = encoding.encode(text)
    return len(tokens)


def visualize_tokens(text: str, model_encoding: str = "cl100k_base"):
    """可视化 Token 分割"""
    encoding = tiktoken.get_encoding(model_encoding)
    tokens = encoding.encode(text)

    print(f"文本：{text}")
    print(f"Token 数量：{len(tokens)}")
    print(f"Token 列表：{[encoding.decode([t]) for t in tokens]}")
    print()


def token_demo():
    # 英文分词示例
    visualize_tokens("Hello, how are you today?")
    # Token 列表：['Hello', ',', ' how', ' are', ' you', ' today', '?']
    # Token 数量：7

    # 中文分词示例（中文通常更多 Token，不同模型切词粒度差异较大）
    visualize_tokens("你好，今天天气怎么样？")
    # 中文每个字通常占 0.5 到 2 个 Token 不等

    # 代码的 Token 计数
    code = """
    def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)
    """
    visualize_tokens(code)


if __name__ == "__main__":
    token_demo()