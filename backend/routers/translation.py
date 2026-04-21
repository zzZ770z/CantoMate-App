import json
import os
import re
from typing import Optional
from dotenv import load_dotenv
from fastapi import APIRouter, HTTPException
from openai import AsyncOpenAI
from pydantic import BaseModel, Field

router = APIRouter()

load_dotenv()

SYSTEM_PROMPT = (
    "你是一个精通香港粤语的语言专家。"
    "请将用户输入的文本翻译成地道香港口语，并提供准确的粤拼(Jyutping)。"
    "你必须严格返回合法的 JSON 格式，不要包含任何 Markdown 代码块标签（如```json）。"
    "输出结构必须为："
    "{\"translatedText\":\"...\",\"jyutping\":\"...\"}"
)


class TranslationMockRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=100)


class TranslationMockResponse(BaseModel):
    sourceText: str
    translatedText: str
    jyutping: str
    provider: str


def _resolve_api_key() -> Optional[str]:
    # Support both new and existing env var names to avoid breaking teammates.
    return (
        os.getenv("LLM_API_KEY")
        or os.getenv("DEEPSEEK_API_KEY")
        or os.getenv("OPENAI_API_KEY")
    )


def _build_client(api_key: str) -> AsyncOpenAI:
    return AsyncOpenAI(
        api_key=api_key,
        base_url="https://api.deepseek.com",
    )


def _extract_json_object(content: str) -> dict:
    try:
        return json.loads(content)
    except json.JSONDecodeError:
        match = re.search(r"\{[\s\S]*\}", content)
        if not match:
            raise
        return json.loads(match.group(0))


@router.post("/translation/mock", response_model=TranslationMockResponse)
async def translation_mock(request: TranslationMockRequest) -> TranslationMockResponse:
    source_text = request.text.strip()
    if not source_text:
        raise HTTPException(status_code=400, detail="text 不能为空")

    api_key = _resolve_api_key()
    if not api_key:
        raise HTTPException(
            status_code=500,
            detail="未检测到 API Key，请在 backend/.env 配置 LLM_API_KEY 或 DEEPSEEK_API_KEY",
        )

    try:
        client = _build_client(api_key)
        response = await client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": source_text},
            ],
            temperature=0.2,
            response_format={"type": "json_object"},
        )
        content = response.choices[0].message.content or "{}"
        payload = _extract_json_object(content)

        translated_text = str(payload.get("translatedText", "")).strip()
        jyutping = str(payload.get("jyutping", "")).strip()
        if not translated_text or not jyutping:
            raise ValueError("模型返回字段不完整")

        return TranslationMockResponse(
            sourceText=source_text,
            translatedText=translated_text,
            jyutping=jyutping,
            provider="deepseek",
        )
    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=502, detail=f"翻译请求失败：{str(exc)}") from exc
