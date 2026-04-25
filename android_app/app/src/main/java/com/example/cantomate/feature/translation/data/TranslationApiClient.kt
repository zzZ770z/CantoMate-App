package com.example.cantomate.feature.translation.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class TranslationResult(
    val sourceText: String,
    val translatedText: String,
    val jyutping: String,
    val provider: String,
)

class TranslationApiClient(
) {
    private val baseUrl = "http://10.0.2.2:8000"
    private val client = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun translate(text: String): TranslationResult = withContext(Dispatchers.IO) {
        val body = JSONObject().put("text", text).toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/api/translation/mock")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("translation api failed: ${response.code}")
            }
            val payload = JSONObject(response.body?.string().orEmpty())
            TranslationResult(
                sourceText = payload.optString("sourceText"),
                translatedText = payload.optString("translatedText"),
                jyutping = payload.optString("jyutping"),
                provider = payload.optString("provider"),
            )
        }
    }
}
