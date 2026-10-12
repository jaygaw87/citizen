package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AppTextToSpeechHelper(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentSpeechRate: Float = 1.0f

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        _errorMessage.value = "English US voice data is not supported or missing on this device."
                        _isInitialized.value = false
                    } else {
                        tts?.setSpeechRate(currentSpeechRate)
                        _isInitialized.value = true
                        _errorMessage.value = null
                    }
                } else {
                    _errorMessage.value = "Text-to-Speech initialization failed."
                    _isInitialized.value = false
                }
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        } catch (e: Exception) {
            Log.e("AppTextToSpeech", "Failed to init TextToSpeech", e)
            _errorMessage.value = "Text-to-Speech unavailable on this device."
            _isInitialized.value = false
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate
        try {
            tts?.setSpeechRate(rate)
        } catch (_: Exception) {}
    }

    fun speak(text: String, utteranceId: String = "civics_utterance") {
        if (!_isInitialized.value || tts == null) {
            return
        }
        stop()
        try {
            tts?.setSpeechRate(currentSpeechRate)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e("AppTextToSpeech", "Error during speak", e)
            _isSpeaking.value = false
        }
    }

    fun stop() {
        try {
            if (tts != null) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.e("AppTextToSpeech", "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
        }
    }

    fun release() {
        stop()
        try {
            tts?.shutdown()
            tts = null
            _isInitialized.value = false
        } catch (e: Exception) {
            Log.e("AppTextToSpeech", "Error releasing TTS", e)
        }
    }
}
