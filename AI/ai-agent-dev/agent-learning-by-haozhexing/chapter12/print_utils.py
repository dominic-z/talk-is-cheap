

def print_separator(title: str = "") -> None:
    """在终端打印一条漂亮的分隔线（可选带标题）。"""
    width = 60
    line = "═" * width
    if title:
        print(f"\n{line}\n  🚀 {title}\n{line}\n")
    else:
        print(f"\n{line}\n")