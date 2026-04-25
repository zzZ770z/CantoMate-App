package com.example.cantomate.feature.translation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cantomate.feature.translation.data.TranslationApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

data class TranslationUiState(
    val inputText: String = "",
    val isLoading: Boolean = false,
    val translatedText: String = "",
    val jyutping: String = "",
    val provider: String = "",
    val errorMessage: String? = null,
    val history: List<TranslationHistoryItem> = emptyList(),
)

data class TranslationHistoryItem(
    val sourceText: String,
    val translatedText: String,
    val jyutping: String,
)

class TranslationViewModel(
    private val apiClient: TranslationApiClient = TranslationApiClient(),
) : ViewModel() {
    private val minLoadingMs = 500L

    var uiState by mutableStateOf(TranslationUiState())
        private set

    fun onInputChange(value: String) {
        uiState = uiState.copy(inputText = value)
    }

    fun translate() {
        val text = uiState.inputText.trim()
        if (text.isEmpty() || uiState.isLoading) return

        viewModelScope.launch {
            val start = System.currentTimeMillis()
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                translatedText = "",
                jyutping = "",
                provider = "",
            )

            runCatching { apiClient.translate(text) }
                .onSuccess { result ->
                    val elapsed = System.currentTimeMillis() - start
                    delay(max(0L, minLoadingMs - elapsed))
                    val newHistory = listOf(
                        TranslationHistoryItem(
                            sourceText = result.sourceText,
                            translatedText = result.translatedText,
                            jyutping = result.jyutping,
                        )
                    ) + uiState.history
                    uiState = uiState.copy(
                        isLoading = false,
                        translatedText = result.translatedText,
                        jyutping = result.jyutping,
                        provider = result.provider,
                        history = newHistory,
                    )
                }
                .onFailure {
                    val elapsed = System.currentTimeMillis() - start
                    delay(max(0L, minLoadingMs - elapsed))
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "翻译服务暂不可用，请稍后重试。",
                    )
                }
        }
    }
}
