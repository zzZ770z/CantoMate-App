package com.example.cantomate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
                    ChatScreen(client)
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