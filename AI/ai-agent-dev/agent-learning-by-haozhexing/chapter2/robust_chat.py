from openai import OpenAI, RateLimitError, APIError, APITimeoutError
import time
import logging

logger = logging.getLogger(__name__)
from client_utils import client,QWEN_3_7_FLASH


def robust_chat(
        message: str,
        max_retries: int = 3,
        retry_delay: float = 1.0
) -> str:
    """带重试机制的鲁棒调用"""

    for attempt in range(max_retries):
        try:
            response = client.chat.completions.create(
                model=QWEN_3_7_FLASH,
                messages=[{"role": "user", "content": message}],
                timeout=30  # 30秒超时
            )
            return response.choices[0].message.content

        except RateLimitError as e:
            # 触发速率限制，等待后重试
            wait_time = retry_delay * (2 ** attempt)  # 指数退避
            logger.warning(f"触发速率限制，{wait_time}秒后重试（第{attempt + 1}次）")
            time.sleep(wait_time)

        except APITimeoutError:
            logger.warning(f"请求超时，重试中（第{attempt + 1}次）")
            time.sleep(retry_delay)

        except APIError as e:
            if e.status_code >= 500:  # 服务器错误，可以重试
                logger.error(f"API 服务器错误 {e.status_code}，重试中")
                time.sleep(retry_delay)
            else:  # 客户端错误（400等），不重试
                raise

    raise RuntimeError(f"API 调用在 {max_retries} 次重试后仍然失败")


# 使用示例
try:
    result = robust_chat("你好，今天天气怎么样？")
    print(result)
except RuntimeError as e:
    print(f"调用失败：{e}")
