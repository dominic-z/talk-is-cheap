import json
import os

from fastapi import FastAPI
from fastapi.responses import HTMLResponse, StreamingResponse
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser

app = FastAPI()

llm = ChatOpenAI(model="qwen3.7-flash",
                 api_key=os.getenv("QWEN_API_KEY"),
                 base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
                 streaming=True)
prompt = ChatPromptTemplate.from_messages([
    ("system", "你是一个助手。"),
    ("human", "{question}")
])
chain = prompt | llm | StrOutputParser()

def sse_response(question: str) -> StreamingResponse:
    async def event_generator():
        async for chunk in chain.astream({"question": question}):
            if not chunk:
                # qwen3.7-flash 是思考型模型：思考阶段的 delta 解析后是空串
                # （所以首字延迟明显偏长）。跳过它们，免得刷一堆空的 SSE 帧。
                continue
            # SSE 格式
            yield f"data: {json.dumps({'content': chunk}, ensure_ascii=False)}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",  # Nginx 反缓冲
        }
    )


# 调用注意：
# 1) `question: str` 是裸类型参数，FastAPI 按 **query string** 解析，不是 JSON body。
#    发 {"question": "..."} 会直接 422 Unprocessable Entity。
#      正确：curl -N -G -X POST --data-urlencode "question=你好" http://127.0.0.1:8000/chat/stream
#      错误：curl -X POST -H 'Content-Type: application/json' -d '{"question":"hi"}' ...
# 2) URL 里的中文必须编码，否则 uvicorn 直接拒掉：WARNING: Invalid HTTP request received。
#    （页面里已用 encodeURIComponent 处理，手写 curl 时用 --data-urlencode。）
@app.post("/chat/stream")
async def chat_stream_post(question: str):
    """SSE 流式响应（POST；question 是 query 参数）"""
    return sse_response(question)


@app.get("/chat/stream")
async def chat_stream_get(question: str):
    """SSE 流式响应（GET；浏览器 EventSource 只支持 GET）"""
    return sse_response(question)


@app.get("/")
async def index() -> HTMLResponse:
    """测试页面：由同一个 app 提供，所以是同源请求，不需要配 CORS。"""
    return HTMLResponse(INDEX_HTML)


INDEX_HTML = """<!doctype html>
<html lang="zh-CN">
<head><meta charset="utf-8"><title>SSE Demo</title></head>
<body style="font-family: sans-serif; max-width: 720px; margin: 2rem auto">
  <h3>SSE 流式输出 Demo</h3>
  <p>
    <input id="q" size="40" value="用一句话解释 SSE">
    <button onclick="start()">发送</button>
  </p>
  <pre id="out" style="white-space: pre-wrap; background: #f5f5f5; padding: 1rem; min-height: 6rem"></pre>
  <script>
    let es = null;
    function start() {
      if (es) es.close();
      const out = document.getElementById('out');
      out.textContent = '';
      const q = document.getElementById('q').value;
      es = new EventSource('/chat/stream?question=' + encodeURIComponent(q));
      es.onmessage = (e) => {
        if (e.data === '[DONE]') { es.close(); es = null; return; }
        out.textContent += JSON.parse(e.data).content;
      };
      es.onerror = () => { if (es) { es.close(); es = null; } };
    }
  </script>
</body>
</html>"""


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="127.0.0.1", port=int(os.getenv("PORT", "8000")))
