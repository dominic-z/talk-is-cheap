from pydantic import BaseModel, ValidationError, validator, field_validator
from openai import OpenAI
from typing import Optional
import json
import time

import os

from openai import OpenAI

client = OpenAI(
    api_key=os.getenv("QWEN_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
)


class UserProfile(BaseModel):
    username: str
    email: str
    age: int
    role: str  # "admin" | "user" | "guest"

    @field_validator("email")
    def email_must_be_valid(cls, v):
        if "@" not in v or "." not in v.split("@")[-1]:
            raise ValueError(f"邮箱格式无效：{v}")
        return v.lower()

    @field_validator("age")
    def age_must_be_positive(cls, v):
        if not (0 < v < 150):
            raise ValueError(f"年龄超出合理范围：{v}")
        return v

    @field_validator("role")
    def role_must_be_valid(cls, v):
        allowed = {"admin", "user", "guest"}
        if v not in allowed:
            raise ValueError(f"角色 '{v}' 不在允许列表 {allowed} 中")
        return v


def extract_with_retry(
        text: str,
        schema_class: type[BaseModel],
        max_retries: int = 3,
        model: str = "qwen3.7-flash",
) -> BaseModel:
    """
    带重试的结构化提取

    第一轮：普通提取
    后续轮：将上一轮的错误信息反馈给模型，引导修复
    """
    last_error = None
    last_output = None

    for attempt in range(max_retries):
        # 构建 Prompt
        if attempt == 0:
            user_content = f"提取以下文本中的用户信息：\n\n{text}"
        else:
            user_content = f"""
上一次提取的结果有误，请修正：

上一次输出：
{last_output}

验证错误：
{last_error}

原始文本：
{text}

请重新提取，确保所有字段类型和格式正确。
"""

        try:
            # 使用 OpenAI Structured Outputs

            messages = [
                {"role": "system", "content": "我在测试通过pydantic+response_format实现的llm返回数据json harness系统，"
                                              "我期望你第一次返回json数据里，有一些奇怪的异常，例如年龄字段返回值小于0,随后在我第二轮提交request的时候，我会将json中的异常发给你，你再输出一个正确的json数据"},
                {"role": "system", "content": "你是一个用户信息提取助手"},
                {"role": "user", "content": user_content},
            ]
            # 这里response_format本身会除法校验，校验不通过会抛出异常。
            # response = client.beta.chat.completions.parse(
            #     model=model,
            #     messages=messages,
            #     response_format=schema_class,
            # )
            # parsed = response.choices[0].message.parsed

            # 没有必要校验了
            # 即使 API 返回了结果，也做一遍 Pydantic 完整验证
            # （包含业务逻辑校验，如邮箱格式、年龄范围等）
            # parsed = schema_class.model_validate(parsed.model_dump())

            # 等价下面
            completion = client.chat.completions.create(
                model="qwen3.7-flash",
                messages=messages,
                response_format={
                    "type": "json_schema",
                    "json_schema": {
                        "name": "user_profile",
                        "strict": True,
                        "schema": UserProfile.model_json_schema(),
                    },
                },
            )
            raw_json = completion.choices[0].message.content  # 拿到的是字符串
            print("llm回复内容: \n" + raw_json)
            parsed = UserProfile.model_validate(json.loads(raw_json))  # 自己解析 + 自己校验

            if attempt > 0:
                print(f"✅ 第 {attempt + 1} 次重试成功")

            return parsed

        except ValidationError as e:
            last_error = str(e)
            last_output = str(parsed) if 'parsed' in locals() else "解析失败"
            print(f"⚠️ 第 {attempt + 1} 次验证失败：{last_error[:100]}")

            if attempt < max_retries - 1:
                time.sleep(0.5 * (attempt + 1))  # 指数退避

        except Exception as e:
            print(f"❌ API 调用失败：{e}")
            raise

    raise ValueError(f"经过 {max_retries} 次重试仍无法提取有效结果。最后错误：{last_error}")


# 使用示例
profile = extract_with_retry(
    text="用户：小明，邮件是 xiaoming@company.com，年龄30，普通用户",
    schema_class=UserProfile,
)
print(profile.model_dump())
# {'username': '小明', 'email': 'xiaoming@company.com', 'age': 30, 'role': 'user'}
