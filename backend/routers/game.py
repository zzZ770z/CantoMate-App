import json
import os
import random
import re
from fastapi import APIRouter, HTTPException
from openai import OpenAI
from dotenv import load_dotenv

from models import GameColumnItem, GameStartResponse


load_dotenv()

router = APIRouter()

client = OpenAI(
	api_key=os.getenv("DEEPSEEK_API_KEY"),
	base_url="https://api.deepseek.com"
)


def _extract_json(content: str) -> dict:
	try:
		return json.loads(content)
	except json.JSONDecodeError:
		match = re.search(r"\{[\s\S]*\}", content)
		if not match:
			raise
		return json.loads(match.group(0))


def _fallback_pairs() -> list[dict]:
	return [
		{"mandarin": "谢谢", "cantonese": "多谢"},
		{"mandarin": "哪里", "cantonese": "边度"},
		{"mandarin": "为什么", "cantonese": "点解"},
		{"mandarin": "多少钱", "cantonese": "几多钱"},
		{"mandarin": "不要", "cantonese": "唔好"},
	]


@router.post("/game/start", response_model=GameStartResponse)
async def start_game():
	prompt = (
		"请生成 5 对适合初学者的普通话-粤语词汇。"
		"返回严格 JSON，不要 Markdown，不要解释。"
		"格式必须是：{\"pairs\":[{\"mandarin\":\"...\",\"cantonese\":\"...\"}]}"
	)

	pairs: list[dict]
	try:
		response = client.chat.completions.create(
			model="deepseek-chat",
			messages=[
				{"role": "system", "content": "你是粤语教学助手，只输出合法 JSON。"},
				{"role": "user", "content": prompt},
			],
			max_tokens=300,
			response_format={"type": "json_object"},
		)

		content = response.choices[0].message.content or ""
		payload = _extract_json(content)
		raw_pairs = payload.get("pairs", [])

		cleaned = []
		for item in raw_pairs:
			mandarin = str(item.get("mandarin", "")).strip()
			cantonese = str(item.get("cantonese", "")).strip()
			if mandarin and cantonese:
				cleaned.append({"mandarin": mandarin, "cantonese": cantonese})

		if len(cleaned) < 5:
			pairs = _fallback_pairs()
		else:
			pairs = cleaned[:5]

	except Exception as exc:
		# 出错时提供兜底词库，避免游戏不可用
		pairs = _fallback_pairs()
		if not pairs:
			raise HTTPException(status_code=500, detail=str(exc)) from exc

	indexed_pairs = [
		{"id": idx + 1, "mandarin": p["mandarin"], "cantonese": p["cantonese"]}
		for idx, p in enumerate(pairs)
	]

	left = [GameColumnItem(id=p["id"], text=p["mandarin"]) for p in indexed_pairs]
	right = [GameColumnItem(id=p["id"], text=p["cantonese"]) for p in indexed_pairs]

	random.shuffle(left)
	random.shuffle(right)

	return GameStartResponse(left=left, right=right)
