from fastapi import APIRouter, Query
from fastapi.responses import StreamingResponse
import edge_tts
import io
import urllib.parse

# Create router, consistent with your teammates' structure
router = APIRouter(
    prefix="/jyutping",
    tags=["Jyutping TTS"]
)


# Jyutping TTS API (fixed Chinese encoding issue)
@router.get("/tts")
async def jyutping_tts(text: str = Query(..., description="Cantonese/Jyutping word to pronounce")):
    try:
        # Standard Hong Kong Cantonese female voice
        VOICE = "zh-HK-HiuGaaiNeural"

        # Generate Cantonese pronunciation
        communicate = edge_tts.Communicate(text, VOICE)

        # Write audio to in-memory stream
        audio_buffer = io.BytesIO()
        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                audio_buffer.write(chunk["data"])

        # Reset stream pointer
        audio_buffer.seek(0)

        # Fix: URL-encode the filename to avoid latin-1 encoding error
        encoded_filename = urllib.parse.quote(f"jyutping_{text}.mp3")

        # Return MP3 audio stream with UTF-8 compatible headers
        return StreamingResponse(
            audio_buffer,
            media_type="audio/mpeg",
            headers={
                "Content-Disposition": f"inline; filename*=UTF-8''{encoded_filename}"
            }
        )
    except Exception as e:
        return {"code": 500, "error": f"Failed to generate pronunciation: {str(e)}"}