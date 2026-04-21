package com.example.cantomate.feature.translation.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TranslationSpeaker(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var initialized = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            initialized = false
            return
        }
        val engine = tts ?: return
        val cantoneseLocale = Locale.Builder().setLanguage("zh").setRegion("HK").build()
        val localeResult = engine.setLanguage(cantoneseLocale)
        initialized = localeResult != TextToSpeech.LANG_MISSING_DATA &&
            localeResult != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun speak(text: String): Boolean {
        if (!initialized || text.isBlank()) return false
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "translation-demo")
        return result == TextToSpeech.SUCCESS
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
    }
}
