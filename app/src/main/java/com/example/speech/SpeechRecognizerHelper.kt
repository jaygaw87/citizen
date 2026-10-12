package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechRecognitionState {
    object Idle : SpeechRecognitionState()
    object Starting : SpeechRecognitionState()
    data class Listening(
        val partialText: String = "",
        val rmsLevel: Float = 0f
    ) : SpeechRecognitionState()
    object Processing : SpeechRecognitionState()
    data class Success(val recognizedText: String) : SpeechRecognitionState()
    data class Error(val message: String, val isOfflineUnavailable: Boolean = false) : SpeechRecognitionState()
    object Unavailable : SpeechRecognitionState()
}

class SpeechRecognizerHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _isRecognitionAvailable = MutableStateFlow(false)
    val isRecognitionAvailable: StateFlow<Boolean> = _isRecognitionAvailable.asStateFlow()

    private var lastRmsUpdateTime = 0L

    init {
        checkAvailability()
    }

    private fun checkAvailability() {
        try {
            val available = SpeechRecognizer.isRecognitionAvailable(context)
            _isRecognitionAvailable.value = available
            if (!available) {
                _state.value = SpeechRecognitionState.Unavailable
            }
        } catch (_: Exception) {
            _isRecognitionAvailable.value = false
            _state.value = SpeechRecognitionState.Unavailable
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = SpeechRecognitionState.Unavailable
            return
        }

        stopListening()

        try {
            _state.value = SpeechRecognitionState.Starting
            _rmsLevel.value = 0f

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                // Prefer offline recognition where available on device
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("SpeechRecognizerHelper", "Failed to start speech recognition", e)
            _state.value = SpeechRecognitionState.Error("Could not start speech recognition: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("SpeechRecognizerHelper", "Error stopping speech recognition", e)
        }
        _rmsLevel.value = 0f
    }

    fun resetState() {
        stopListening()
        _rmsLevel.value = 0f
        _state.value = SpeechRecognitionState.Idle
    }

    fun release() {
        stopListening()
        _rmsLevel.value = 0f
        _state.value = SpeechRecognitionState.Idle
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            private var lastRmsUpdateTime = 0L
            private var smoothedRms = 0f

            override fun onReadyForSpeech(params: Bundle?) {
                smoothedRms = 0f
                _rmsLevel.value = 0f
                _state.value = SpeechRecognitionState.Listening()
            }

            override fun onBeginningOfSpeech() {
                val current = _state.value
                if (current !is SpeechRecognitionState.Listening) {
                    _state.value = SpeechRecognitionState.Listening()
                }
            }

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB typically ranges from -2dB to 10dB (or 0 to 10)
                // Normalize safely between 0f and 1f
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                // Exponential moving average smoothing: 0.70 * previous + 0.30 * current
                smoothedRms = (smoothedRms * 0.70f + normalized * 0.30f).coerceIn(0f, 1f)
                _rmsLevel.value = smoothedRms

                val now = System.currentTimeMillis()
                if (now - lastRmsUpdateTime > 25) {
                    lastRmsUpdateTime = now
                    val current = _state.value
                    if (current is SpeechRecognitionState.Listening) {
                        _state.value = current.copy(rmsLevel = smoothedRms)
                    }
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                smoothedRms = 0f
                _rmsLevel.value = 0f
                _state.value = SpeechRecognitionState.Processing
            }

            override fun onError(error: Int) {
                _rmsLevel.value = 0f
                val isOffline = (error == SpeechRecognizer.ERROR_NETWORK ||
                        error == SpeechRecognizer.ERROR_SERVER ||
                        error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED)

                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check your microphone."
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is needed for Speaking Practice."
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue encountered."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition timed out."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Couldn't hear your answer. Tap microphone to try again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Try again in a moment."
                    SpeechRecognizer.ERROR_SERVER -> "Recognition server error."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Couldn't hear your answer. Tap microphone and speak."
                    SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT,
                    SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "Offline speech recognition is not available on this device. You can still use Hear Question and Reveal Answer."
                    else -> "Speech recognition error ($error)."
                }

                val displayMessage = if (isOffline) {
                    "Offline speech recognition is not available on this device. You can still use Hear Question and Reveal Answer."
                } else {
                    errorMessage
                }

                _state.value = SpeechRecognitionState.Error(
                    message = displayMessage,
                    isOfflineUnavailable = isOffline
                )
            }

            override fun onResults(results: Bundle?) {
                _rmsLevel.value = 0f
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val primaryMatch = matches?.firstOrNull()?.trim()

                if (!primaryMatch.isNullOrBlank()) {
                    _state.value = SpeechRecognitionState.Success(primaryMatch)
                } else {
                    _state.value = SpeechRecognitionState.Error("Couldn't hear your answer. Tap microphone to try again.")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()?.trim()
                if (!partial.isNullOrBlank()) {
                    val currentRms = (_state.value as? SpeechRecognitionState.Listening)?.rmsLevel ?: _rmsLevel.value
                    _state.value = SpeechRecognitionState.Listening(partialText = partial, rmsLevel = currentRms)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
