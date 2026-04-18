from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
# 引入你刚刚写好的 chat 路由
from routers import chat
from routers import game

# 1. 初始化 FastAPI 应用
app = FastAPI(title="CantoMate API")

# 2. 允许跨域请求
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 3. 挂载路由 (把 chat 的功能挂到 /api 下面)
app.include_router(chat.router, prefix="/api")
app.include_router(game.router, prefix="/api")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)