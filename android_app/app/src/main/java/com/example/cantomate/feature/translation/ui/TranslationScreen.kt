package com.example.cantomate.feature.translation.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cantomate.feature.translation.audio.TranslationSpeaker
import com.example.cantomate.feature.translation.viewmodel.TranslationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationScreen(
    onBack: (() -> Unit)? = null,
    translationViewModel: TranslationViewModel = viewModel(),
) {
    val context = LocalContext.current
    val speaker = remember(context) { TranslationSpeaker(context) }
    val uiState = translationViewModel.uiState

    DisposableEffect(Unit) {
        onDispose { speaker.release() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        if (onBack != null) {
            TopAppBar(
                title = { Text("翻譯示範模組") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            )
        }

        Text(
            text = "普通話轉粵語（含粵拼）",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = uiState.inputText,
            onValueChange = translationViewModel::onInputChange,
            label = { Text("輸入普通話文本") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = translationViewModel::translate,
            enabled = !uiState.isLoading && uiState.inputText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("翻譯並生成粵拼")
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("翻譯中，請稍候...")
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = Color(0xFFE57373))
        }

        if (uiState.translatedText.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1F1F))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("譯文：${uiState.translatedText}", color = Color.White)
                        IconButton(
                            onClick = { speakOrToast(speaker, context, uiState.translatedText) }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "播放譯文",
                                tint = Color(0xFF00B894)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("粵拼：${uiState.jyutping}", color = Color(0xFFB0BEC5))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("數據來源：${uiState.provider}", color = Color(0xFF8E8E8E))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "歷史記錄",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.history) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("原文：${item.sourceText}", color = Color.White)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("譯文：${item.translatedText}", color = Color.White)
                            IconButton(
                                onClick = { speakOrToast(speaker, context, item.translatedText) }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "播放歷史譯文",
                                    tint = Color(0xFF00B894)
                                )
                            }
                        }
                        Text("粵拼：${item.jyutping}", color = Color(0xFFB0BEC5))
                    }
                }
            }
        }
    }
}

private fun speakOrToast(speaker: TranslationSpeaker, context: android.content.Context, text: String) {
    val ok = speaker.speak(text)
    if (!ok) {
        Toast.makeText(context, "目前裝置暫不支援 TTS 發音", Toast.LENGTH_SHORT).show()
    }
}
