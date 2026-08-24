from openai import OpenAI
import subprocess
import tempfile
import traceback
import os
client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)


def self_correcting_code_generator(requirement: str) -> str:
    """
    自我纠错的代码生成器
    生成代码 → 自动测试 → 发现错误 → 修复 → 循环
    """


    max_attempts = 3

    for attempt in range(max_attempts):
        print(f"\n[尝试 {attempt + 1}]")

        # 生成代码
        response = client.chat.completions.create(
            model="qwen3.7-flash",
            messages=[
                {
                    "role": "user",
                    "content": f"""
编写Python代码完成以下需求：
{requirement}

要求：
1. 代码必须能够直接运行（包含完整的测试用例）
2. 在文件末尾添加 if __name__ == '__main__': 测试代码
3. 只返回纯Python代码，不要Markdown格式
"""
                }
            ]
        )

        code = response.choices[0].message.content

        # 清理代码（移除可能的markdown标记）
        if "```python" in code:
            code = code.split("```python")[1].split("```")[0].strip()
        elif "```" in code:
            code = code.split("```")[1].split("```")[0].strip()

        # 测试代码
        with tempfile.NamedTemporaryFile(mode='w', suffix='.py',
                                         delete=False, encoding='utf-8') as f:
            f.write(code)
            tmp_file = f.name

        try:
            result = subprocess.run(
                ["/home/dominiczhu/Programs/miniconda3/envs/agent-env/bin/python", tmp_file],
                capture_output=True,
                text=True,
                timeout=10
            )

            if result.returncode == 0:
                print(f"✅ 代码运行成功！")
                os.unlink(tmp_file)
                return code
            else:
                error = result.stderr
                print(f"❌ 运行错误：{error[:200]}")

                # 修复错误
                fix_response = client.chat.completions.create(
                    model="qwen3.7-flash",
                    messages=[
                        {
                            "role": "user",
                            "content": f"""以下代码有错误，请修复：
                            代码：
```python
{code}
```
错误信息： {error}
请返回修复后的完整Python代码（不要Markdown格式）：""" } ] )
                code = fix_response.choices[0].message.content
                if "python" in code:
                    code = code.split("python")[1].split("```")[0].strip()
        except subprocess.TimeoutExpired as e:
            print("❌ 代码执行超时")
            print(f"超时异常：{e}")
            print("异常错误栈：")
            print(traceback.format_exc())
        
        except Exception as e:
            print(f"❌ 发生异常：{e}")
            print("异常错误栈：")
            print(traceback.format_exc())
        
        finally:
            try:
                if os.path.exists(tmp_file):
                    os.unlink(tmp_file)
            except Exception as e:
                print(f"⚠️ 删除临时文件失败：{e}")
                print("异常错误栈：")
                print(traceback.format_exc())

    return f"# 无法生成满足要求的代码（{max_attempts}次尝试后）\n" + code


code = self_correcting_code_generator( "实现一个函数，计算列表中所有偶数的平均值，如果没有偶数返回0" )
print(code)

