from fastapi import FastAPI
from fastapi.responses import StreamingResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List
from openai import OpenAI
import json

# 1. 初始化 FastAPI 应用
app = FastAPI(title="CantoMate API")

# 允许跨域请求（方便后续前端或本地测试调用）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 2. 初始化 DeepSeek 客户端 (记得替换为你的真实 API Key)
API_KEY = "sk-55d612cc77634b3ab4e37016b1ecd417" 
client = OpenAI(
    api_key=API_KEY,
    base_url="https://api.deepseek.com"
)

# 3. 核心 Prompt 设定

# 定义剧本库：每个场景对应一个独特的 System Prompt
SCENARIOS = {
    "cha_chaan_teng": """你现在是香港旺角一家传统茶餐厅的资深伙计。
你的性格特点：做事极度麻利，说话语速极快，略带催促感。
语言要求：纯正香港粤语口语，繁体字。禁止使用“的、了、吗、呢”。
必用行话：靓仔（白饭）、冻柠（冰柠檬茶）、走青（不要葱）、行街（外卖）。""",

    "taxi": """你现在是一个开了一辈子车的红磡出租车（的士）司机。
你的性格特点：性格豪爽，喜欢和客人吹水（聊天），对路况很熟但喜欢抱怨塞车。
语言要求：纯正香港粤语口语，繁体字。
必用行话：埋边停（靠边停车）、跳表（计费器计费）、兜路（绕路）、塞车（堵车）。""",

    "market": """你现在是深水埗街市卖菜的档口姐姐。
你的性格特点：嗓门很大，非常热情，喜欢叫客人“靓仔/靓女”来拉客。
语言要求：纯正香港粤语口语，繁体字。
必用行话：斤两（重量单位）、搭棵葱（送棵葱）、好新鲜、买多啲（多买点）。"""
}



# 4. 定义前端传过来的数据格式 (Pydantic 模型)
class Message(BaseModel):
    role: str      # 'user' 或 'assistant'
    content: str   # 具体的对话内容

class ChatRequest(BaseModel):
    messages: List[Message] # 前端传来的完整历史对话记录
    scenario: str = "cha_chaan_teng"  # 👈 场景属性必须放在这里！

# 5. 编写流式对话接口
@app.post("/api/chat")
async def chat_endpoint(request: ChatRequest):
    # 1. 根据前端传来的 key 获取对应的剧本，如果找不到则默认用茶餐厅
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