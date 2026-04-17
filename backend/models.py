from pydantic import BaseModel
from typing import List

class Message(BaseModel):
    role: str      # 'user' 或 'assistant'
    content: str   # 具体的对话内容

class ChatRequest(BaseModel):
    messages: List[Message] # 前端传来的完整历史对话记录
    scenario: str = "cha_chaan_teng"  # 场景属性