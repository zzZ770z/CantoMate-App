from openai import OpenAI
import os

# 1. 填入你刚刚在 DeepSeek 官网申请的 API Key
API_KEY = "sk-55d612cc77634b3ab4e37016b1ecd417" # 把这里的 x 换成真实的 Key

# 2. 初始化客户端，注意 base_url 必须指向 DeepSeek 的服务器
client = OpenAI(
    api_key=API_KEY,
    base_url="https://api.deepseek.com"
)

# 3. 核心 System Prompt（保持原汁原味的港味设定）
SYSTEM_PROMPT = """你现在是香港旺角一家传统茶餐厅的资深伙计（侍应）。
你的性格特点：做事极度麻利，说话语速极快，为了翻台率略带一点催促感。

你的语言要求（必须严格遵守）：
1. 必须使用纯正的香港粤语口语，并输出繁体字。
2. 【黑名单】绝对禁止使用普通话的结构助词和语气词（如：的、了、吗、呢、什么、怎么）。
3. 【白名单】必须强制使用粤语语气词（如：嘅、咗、咩、呀、喇、噃、㖞、点解、做咩）。
4. 必须自然地使用茶餐厅专属行话，例如：白饭=靓仔，冰柠檬茶=冻柠，不要葱=走青，外卖=行街。
5. 每次回复要像真实的面对面对话，简短、直接，不要长篇大论。

场景开始：客人刚刚坐下，你拿着点菜单走过去。请你主动用一句极其地道的粤语开场，问客人吃什么。
"""

def main():
    print("🎬 【CantoMate 场景测试】: 旺角茶餐厅点单 (DeepSeek 驱动)")
    print("输入 'quit' 退出")
    print("-" * 50)
    
    # 构造历史对话列表
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": "（我行入茶餐廳，搵咗個位坐低）"}
    ]
    
    try:
        # 获取第一句开场白
        response = client.chat.completions.create(
            model="deepseek-chat", # 使用 DeepSeek 的主模型
            messages=messages,
            max_tokens=300
        )
        
        ai_reply = response.choices[0].message.content
        print(f"\n👨‍🍳 茶餐厅伙计: {ai_reply}")
        messages.append({"role": "assistant", "content": ai_reply})
        
        # 循环对话
        while True:
            user_input = input("\n👤 你的回复 (点单): ")
            if user_input.lower() in ['quit', 'exit', 'q']:
                print("\n👋 埋单！多谢惠顾！")
                break
                
            messages.append({"role": "user", "content": user_input})
            
            response = client.chat.completions.create(
                model="deepseek-chat",
                messages=messages,
                max_tokens=300
            )
            
            ai_reply = response.choices[0].message.content
            print(f"\n👨‍🍳 茶餐厅伙计: {ai_reply}")
            messages.append({"role": "assistant", "content": ai_reply})
            
    except Exception as e:
        print(f"\n❌ 发生错误: {e}")

if __name__ == "__main__":
    main()