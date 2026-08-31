from openai import OpenAI
from pydantic import BaseModel, Field
from typing import Optional, Literal

import os


from openai import OpenAI
client = OpenAI(
            api_key=os.getenv("QWEN_API_KEY"),
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
        )

# ── 用 Pydantic 定义 Schema ──
class PersonInfo(BaseModel):
    name: str = Field(description="姓名")
    age: int = Field(description="年龄，必须为正整数", ge=0, le=150)
    email: Optional[str] = Field(default=None, description="邮箱地址")
    gender: Literal["male", "female", "unknown"] = Field(
        default="unknown", description="性别"
    )

class ExtractionResult(BaseModel):
    persons: list[PersonInfo] = Field(description="提取到的所有人员信息")
    confidence: float = Field(description="提取置信度 0~1", ge=0, le=1)
    notes: Optional[str] = Field(default=None, description="备注")

# ── 调用 API，使用 parse() 方法直接得到 Pydantic 对象 ──
response = client.beta.chat.completions.parse(
    model="qwen3.7-flash",   # 支持 Structured Outputs 的版本
    messages=[
        {"role": "system", "content": "你是一个信息提取助手，从文本中提取人员信息。"},
        {"role": "user", "content": "张三，男，28岁，邮箱 zhangsan@example.com。李四，35岁。"},
    ],
    response_format=ExtractionResult,  # 直接传 Pydantic 类！
)

# 返回的直接是 Pydantic 对象，类型安全，无需手动解析
result: ExtractionResult = response.choices[0].message.parsed
print(result.persons[0].name)    # "张三"
print(result.confidence)         # 0.95（示例）
print(type(result.persons[0].age))  # <class 'int'>，类型已保证
