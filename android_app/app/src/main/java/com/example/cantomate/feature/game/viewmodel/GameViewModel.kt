package com.example.cantomate.feature.game.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cantomate.feature.game.model.GameWordItem
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class GameViewModel : ViewModel() {
    var leftItems by mutableStateOf(listOf<GameWordItem>())
    var rightItems by mutableStateOf(listOf<GameWordItem>())
    var selectedLeftId by mutableStateOf<Int?>(null)
    var matchedIds by mutableStateOf(setOf<Int>())
    var isLoading by mutableStateOf(false)
    var statusText by mutableStateOf("按「開始遊戲」生成 5 對詞語")

    private val client = OkHttpClient()

    fun startGame() {
        isLoading = true
        selectedLeftId = null
        matchedIds = emptySet()
        statusText = "正在生成題目..."

        val body = "{}".toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/game/start")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                isLoading = false
                statusText = "[網絡錯誤] 無法連線到遊戲服務"
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        isLoading = false
                        statusText = "[伺服器錯誤] ${it.code}"
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

                        leftItems = left
                        rightItems = right
                        isLoading = false
                        statusText = "先點左邊普通話，再點右邊粵語完成一組配對"
                    } catch (e: Exception) {
                        isLoading = false
                        statusText = "[解析錯誤] 遊戲資料格式不正確"
                    }
                }
            }
        })
    }

    fun onLeftItemSelected(id: Int, text: String) {
        selectedLeftId = id
        statusText = "已選左側「$text」，請點右側對應詞語"
    }

    fun onRightItemSelected(item: GameWordItem) {
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
    }
}
