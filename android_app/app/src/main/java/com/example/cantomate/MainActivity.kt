package com.example.cantomate

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(val role: String, val content: String)
data class GameWordItem(val id: Int, val text: String)

class MainActivity : ComponentActivity() {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // IG 暗黑模式：纯黑背景
            MaterialTheme(colorScheme = darkColorScheme(background = Color.Black)) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(client)
                }
            }
        }
    }
}

@Composable
fun AppRoot(client: OkHttpClient) {
    var currentPage by remember { mutableStateOf("chat") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                .border(1.dp, Color(0xFF262626), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            val chatSelected = currentPage == "chat"
            val gameSelected = currentPage == "game"

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (chatSelected) Color(0xFF0095F6) else Color.Transparent, RoundedCornerShape(20.dp))
                    .clickable { currentPage = "chat" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("聊天", color = if (chatSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (gameSelected) Color(0xFF00B894) else Color.Transparent, RoundedCornerShape(20.dp))
                    .clickable { currentPage = "game" }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("配對遊戲", color = if (gameSelected) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
            }
        }

        if (currentPage == "chat") {
            ChatScreen(client)
        } else {
            GameScreen(client)
        }
    }
}

@Composable
fun GameScreen(client: OkHttpClient) {
    var leftItems by remember { mutableStateOf(listOf<GameWordItem>()) }
    var rightItems by remember { mutableStateOf(listOf<GameWordItem>()) }
    var selectedLeftId by remember { mutableStateOf<Int?>(null) }
    var matchedIds by remember { mutableStateOf(setOf<Int>()) }
    var isLoading by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("按「開始遊戲」生成 5 對詞語") }

    fun startGame() {
        isLoading = true
        selectedLeftId = null
        matchedIds = emptySet()
        statusText = "正在生成題目..."

        fetchGamePairs(
            client = client,
            onSuccess = { left, right ->
                leftItems = left
                rightItems = right
                isLoading = false
                statusText = "先點左邊普通話，再點右邊粵語完成一組配對"
            },
            onError = { error ->
                isLoading = false
                statusText = error
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text("普通話 x 粵語 配對", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("規則：每次先點左，再點右；一組一組配對。", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { startGame() },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894))
        ) {
            Text(if (leftItems.isEmpty()) "開始遊戲" else "重新抽題")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(statusText, color = Color(0xFFB0BEC5), fontSize = 13.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF00B894))
        }

        if (leftItems.isNotEmpty() && rightItems.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("普通話", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    leftItems.forEach { item ->
                        val isMatched = matchedIds.contains(item.id)
                        val isSelected = selectedLeftId == item.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isMatched && !isLoading) {
                                    selectedLeftId = item.id
                                    statusText = "已選左側「${item.text}」，請點右側對應詞語"
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isMatched -> Color(0xFF2E7D32)
                                    isSelected -> Color(0xFF1565C0)
                                    else -> Color(0xFF1F1F1F)
                                }
                            )
                        ) {
                            Text(
                                item.text,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("粵語", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    rightItems.forEach { item ->
                        val isMatched = matchedIds.contains(item.id)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isMatched && !isLoading) {
                                    val leftId = selectedLeftId
                                    if (leftId == null) {
                                        statusText = "請先點左側普通話，再點右側粵語"
                                    } else {
                                        if (leftId == item.id) {
                                            matchedIds = matchedIds + item.id
                                            selectedLeftId = null
                                            statusText = "配對正確：${item.text}"
                                            if (matchedIds.size == 5) {
                                                statusText = "全部配對完成！做得好！"
                                            }
                                        } else {
                                            selectedLeftId = null
                                            statusText = "配對錯誤，再試一次。"
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMatched) Color(0xFF2E7D32) else Color(0xFF1F1F1F)
                            )
                        ) {
                            Text(
                                item.text,
                                color = Color.White,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            if (matchedIds.size == 5) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { startGame() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("再次闖關")
                }
            }
        }
    }
}

@Composable
fun ChatScreen(client: OkHttpClient) {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var currentScenario by remember { mutableStateOf("cha_chaan_teng") }
    var expanded by remember { mutableStateOf(false) }

    val scenarios = mapOf(
        "cha_chaan_teng" to Pair("旺角茶餐廳 ☕", "👨‍🍳"),
        "taxi" to Pair("紅磡的士 🚕", "🚖"),
        "market" to Pair("深水埗街市 🥬", "🧑‍🌾")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // --- 1. IG 风格顶部导航栏 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮占位
            Text("〈", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))

            // 头像与名字 (点击唤出场景切换)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 圆形头像
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262626)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(scenarios[currentScenario]!!.second, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(scenarios[currentScenario]!!.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("CantoMate 粵語陪練 〉", color = Color.Gray, fontSize = 12.sp)
                }

                // 下拉菜单
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF262626))
                ) {
                    scenarios.forEach { (key, data) ->
                        DropdownMenuItem(
                            text = { Text("${data.second} ${data.first}", color = Color.White) },
                            onClick = {
                                currentScenario = key
                                expanded = false
                                messages.clear()
                            }
                        )
                    }
                }
            }
            // 视频/电话图标占位
            Text("🎥  📞", color = Color.White, fontSize = 18.sp)
        }

        Divider(color = Color(0xFF262626), thickness = 0.5.dp)

        // --- 2. 聊天记录区 ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubbleIG(msg, scenarios[currentScenario]!!.second)
            }
        }

        // --- 3. IG 风格底部输入框 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp)
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 相机图标占位
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0095F6)), // IG 蓝
                contentAlignment = Alignment.Center
            ) {
                Text("📷", color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))

            // 胶囊输入框
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF262626), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextFieldIG(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    isTyping = isTyping
                )

                if (inputText.isNotBlank()) {
                    Text(
                        "發送",
                        color = Color(0xFF0095F6),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(enabled = !isTyping) {
                                val userMsg = ChatMessage("user", inputText)
                                messages.add(userMsg)
                                inputText = ""
                                isTyping = true
                                messages.add(ChatMessage("assistant", ""))

                                sendMessage(client, messages, currentScenario, onUpdate = { newText ->
                                    val lastIndex = messages.lastIndex
                                    messages[lastIndex] = messages[lastIndex].copy(
                                        content = messages[lastIndex].content + newText
                                    )
                                }, onComplete = { isTyping = false })
                            }
                            .padding(start = 8.dp)
                    )
                } else {
                    Text("🎙️", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

// 提取的无边框文本输入框
@Composable
fun BasicTextFieldIG(value: String, onValueChange: (String) -> Unit, modifier: Modifier, isTyping: Boolean) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) {
            Text("發送消息...", color = Color.Gray)
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = !isTyping,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// IG 风格的气泡
@Composable
fun ChatBubbleIG(message: ChatMessage, aiAvatar: String) {
    val isUser = message.role == "user"

    // IG 标志性的渐变色（紫 -> 蓝）
    val igGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF833AB4), Color(0xFF9146FF), Color(0xFF0095F6))
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            // AI 头像
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF262626)),
                contentAlignment = Alignment.Center
            ) {
                Text(aiAvatar, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(
                    brush = if (isUser) igGradient else Brush.linearGradient(listOf(Color(0xFF262626), Color(0xFF262626))),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text = message.content, color = Color.White, fontSize = 15.sp)
        }
    }
}

fun sendMessage(client: OkHttpClient, messageHistory: List<ChatMessage>, scenario: String, onUpdate: (String) -> Unit, onComplete: () -> Unit) {
    val jsonArray = JSONArray()
    for (i in 0 until messageHistory.size - 1) {
        val obj = JSONObject()
        obj.put("role", messageHistory[i].role)
        obj.put("content", messageHistory[i].content)
        jsonArray.put(obj)
    }

    val jsonBody = JSONObject()
        .put("messages", jsonArray)
        .put("scenario", scenario)
        .toString()

    val body = jsonBody.toRequestBody("application/json".toMediaType())
    val request = Request.Builder().url("http://10.0.2.2:8000/api/chat").post(body).build()
    val factory = EventSources.createFactory(client)

    factory.newEventSource(request, object : EventSourceListener() {
        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            if (data == "[DONE]") onComplete()
            else {
                try {
                    val text = JSONObject(data).getString("text")
                    onUpdate(text)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            onUpdate("\n[網絡錯誤]")
            onComplete()
        }
    })
}

fun fetchGamePairs(
    client: OkHttpClient,
    onSuccess: (List<GameWordItem>, List<GameWordItem>) -> Unit,
    onError: (String) -> Unit
) {
    val mainHandler = Handler(Looper.getMainLooper())
    val body = "{}".toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("http://10.0.2.2:8000/api/game/start")
        .post(body)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: java.io.IOException) {
            mainHandler.post { onError("[網絡錯誤] 無法連線到遊戲服務") }
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!it.isSuccessful) {
                    mainHandler.post { onError("[伺服器錯誤] ${it.code}") }
                    return
                }

                val bodyText = it.body?.string().orEmpty()
                try {
                    val root = JSONObject(bodyText)
                    val leftArray = root.getJSONArray("left")
                    val rightArray = root.getJSONArray("right")

                    val left = mutableListOf<GameWordItem>()
                    val right = mutableListOf<GameWordItem>()

                    for (i in 0 until leftArray.length()) {
                        val obj = leftArray.getJSONObject(i)
                        left.add(GameWordItem(id = obj.getInt("id"), text = obj.getString("text")))
                    }

                    for (i in 0 until rightArray.length()) {
                        val obj = rightArray.getJSONObject(i)
                        right.add(GameWordItem(id = obj.getInt("id"), text = obj.getString("text")))
                    }

                    mainHandler.post { onSuccess(left, right) }
                } catch (e: Exception) {
                    mainHandler.post { onError("[解析錯誤] 遊戲資料格式不正確") }
                }
            }
        }
    })
}