package com.example.cantomate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cantomate.slang.ui.SlangOfTheDayScreen
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
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
            MaterialTheme(colorScheme = darkColorScheme(background = Color.Black)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CantoMateApp(client = client)
                }
            }
        }
    }
}

private sealed class AppDestination(
    val route: String,
    val label: String,
    val iconText: String
) {
    data object Chat : AppDestination(
        route = "chat",
        label = "对话 Chat",
        iconText = "聊"
    )

    data object Slang : AppDestination(
        route = "slang",
        label = "每日一词 Slang",
        iconText = "词"
    )
}

@Composable
private fun CantoMateApp(client: OkHttpClient) {
    val navController = rememberNavController()
    val destinations = listOf(AppDestination.Chat, AppDestination.Slang)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(destination.iconText) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Chat.route) {
                ChatScreen(client = client)
            }
            composable(AppDestination.Slang.route) {
                SlangOfTheDayScreen()
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
        "cha_chaan_teng" to Pair("茶餐厅", "🍞"),
        "taxi" to Pair("的士", "🚕"),
        "market" to Pair("街市", "🛒")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⟵", color = Color.White, fontSize = 24.sp, modifier = Modifier.padding(end = 16.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    Text(
                        scenarios[currentScenario]!!.first,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text("CantoMate 粤语练习", color = Color.Gray, fontSize = 12.sp)
                }

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

            Text("📹  📞", color = Color.White, fontSize = 18.sp)
        }

        Divider(color = Color(0xFF262626), thickness = 0.5.dp)

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 8.dp)
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0095F6)),
                contentAlignment = Alignment.Center
            ) {
                Text("📷", color = Color.White, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))

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
                        "发送",
                        color = Color(0xFF0095F6),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(enabled = !isTyping) {
                                val userMsg = ChatMessage("user", inputText)
                                messages.add(userMsg)
                                inputText = ""
                                isTyping = true
                                messages.add(ChatMessage("assistant", ""))

                                sendMessage(
                                    client = client,
                                    messageHistory = messages,
                                    scenario = currentScenario,
                                    onUpdate = { newText ->
                                        val lastIndex = messages.lastIndex
                                        messages[lastIndex] = messages[lastIndex].copy(
                                            content = messages[lastIndex].content + newText
                                        )
                                    },
                                    onComplete = { isTyping = false }
                                )
                            }
                            .padding(start = 8.dp)
                    )
                } else {
                    Text("🎤", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
fun BasicTextFieldIG(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    isTyping: Boolean
) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        if (value.isEmpty()) {
            Text("发送消息...", color = Color.Gray)
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = !isTyping,
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ChatBubbleIG(message: ChatMessage, aiAvatar: String) {
    val isUser = message.role == "user"

    val igGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF833AB4), Color(0xFF9146FF), Color(0xFF0095F6))
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262626)),
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
                    brush = if (isUser) {
                        igGradient
                    } else {
                        Brush.linearGradient(
                            listOf(Color(0xFF262626), Color(0xFF262626))
                        )
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(text = message.content, color = Color.White, fontSize = 15.sp)
        }
    }
}

fun sendMessage(
    client: OkHttpClient,
    messageHistory: List<ChatMessage>,
    scenario: String,
    onUpdate: (String) -> Unit,
    onComplete: () -> Unit
) {
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
            if (data == "[DONE]") {
                onComplete()
            } else {
                try {
                    val text = JSONObject(data).getString("text")
                    onUpdate(text)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            onUpdate("\n[连接错误]")
            onComplete()
        }
    })
}
