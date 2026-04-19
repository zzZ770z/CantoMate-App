package com.example.cantomate.slang.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cantomate.slang.data.SlangRepository
import com.example.cantomate.slang.model.Slang
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SlangUiState(
    val isLoading: Boolean = true,
    val slang: Slang? = null,
    val error: String? = null
)

class SlangViewModel(
    private val repository: SlangRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SlangUiState())
    val uiState: StateFlow<SlangUiState> = _uiState.asStateFlow()

    init {
        loadTodaySlang()
    }

    fun loadTodaySlang() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { repository.getSlangOfToday() }
                .onSuccess { slang ->
                    _uiState.value = SlangUiState(
                        isLoading = false,
                        slang = slang
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = SlangUiState(
                        isLoading = false,
                        error = throwable.message ?: "Failed to load slang of the day."
                    )
                }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val prefs = appContext.getSharedPreferences(
                        SlangRepository.PREF_NAME,
                        Context.MODE_PRIVATE
                    )
                    val repository = SlangRepository(prefs)
                    return SlangViewModel(repository) as T
                }
            }
        }
    }
}
