# pip install json-repair
from json_repair import repair_json
import json

# ── 基本使用：直接替代 json.loads ──
broken_json = """```json
{
  "name": "张三",
  'age': 28,   // 年龄
  "email": "zhangsan@example.com",
"""

# repair_json 返回修复后的 JSON 字符串
fixed_str = repair_json(broken_json)
data = json.loads(fixed_str)
# {"name": "张三", "age": 28, "email": "zhangsan@example.com"}

# ── 或者直接返回 Python 对象 ──
data = repair_json(broken_json, return_objects=True)
print(type(data))  # <class 'dict'>
print(data["name"])  # "张三"
