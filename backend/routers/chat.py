import os
import json
from fastapi import APIRouter
from fastapi.responses import StreamingResponse
from openai import OpenAI
from dotenv import load_dotenv


from models import ChatRequest
from config import SCENARIOS

# 加载 .env 里的 API Keys 
load_dotenv()

# 初始化路由
router = APIRouter()

# 初始化 DeepSeek 客户端
client = OpenAI(
    api_key=os.getenv("DEEPSEEK_API_KEY"),
    base_url="https://api.deepseek.com"
)

# 编写流式对话接口 (注意这里只写 /chat)
@router.post("/chat")
async def chat_endpoint(request: ChatRequest):
    # 1. 根据前端传来的 key 获取对应的剧本
    system_content = SCENARIOS.get(request.scenario, SCENARIOS["cha_chaan_teng"])
    
    # 2. 构造发送给 DeepSeek 的消息列表
    api_messages = [{"role": "system", "content": system_content}]
    for msg in request.messages:
        api_messages.append({"role": msg.role, "content": msg.content})

    def stream_generator():
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=api_messages,
            stream=True,
            max_tokens=300
        )
        
        for chunk in response:
            if chunk.choices[0].delta.content is not None:
                text = chunk.choices[0].delta.content
                yield f"data: {json.dumps({'text': text}, ensure_ascii=False)}\n\n"
        
        yield "data: [DONE]\n\n"

    return StreamingResponse(stream_generator(), media_type="text/event-stream")