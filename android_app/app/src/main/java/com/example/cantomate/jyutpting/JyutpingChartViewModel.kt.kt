package com.example.cantomate.jyutping

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class JyutpingChartViewModel : ViewModel() {
    private var mediaPlayer: MediaPlayer? = null
    private val TTS_BASE_URL = "http://10.0.2.2:8000/jyutping/tts"

    fun playJyutpingAudio(context: Context, text: String) {
        viewModelScope.launch {
            releaseMediaPlayer()
            val cacheFile = File(context.cacheDir, "jyutping_${text}.mp3")

            if (cacheFile.exists()) {
                playLocalAudio(cacheFile.absolutePath)
                return@launch
            }

            withContext(Dispatchers.IO) {
                try {
                    // 【关键修改】对中文text进行URL编码，避免乱码
                    val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
                    val url = URL("$TTS_BASE_URL?text=$encodedText")

                    val connection = url.openConnection() as HttpURLConnection
                    connection.apply {
                        requestMethod = "GET"
                        connectTimeout = 5000
                        readTimeout = 5000
                    }

                    if (connection.responseCode == 200) {
                        val inputStream = connection.inputStream
                        val outputStream = FileOutputStream(cacheFile)
                        inputStream.copyTo(outputStream)
                        inputStream.close()
                        outputStream.close()
                        playLocalAudio(cacheFile.absolutePath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun playLocalAudio(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releaseMediaPlayer()
    }
}