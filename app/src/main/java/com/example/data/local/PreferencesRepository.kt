package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("citizenship_2026_prefs", Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _themeMode = MutableStateFlow(
        prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _audioAutoplay = MutableStateFlow(
        prefs.getBoolean(KEY_AUDIO_AUTOPLAY, false)
    )
    val audioAutoplay: StateFlow<Boolean> = _audioAutoplay.asStateFlow()

    private val _questionAudio = MutableStateFlow(
        prefs.getBoolean(KEY_QUESTION_AUDIO, true)
    )
    val questionAudio: StateFlow<Boolean> = _questionAudio.asStateFlow()

    private val _answerAudio = MutableStateFlow(
        prefs.getBoolean(KEY_ANSWER_AUDIO, true)
    )
    val answerAudio: StateFlow<Boolean> = _answerAudio.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(
        prefs.getFloat(KEY_PLAYBACK_SPEED, 1.0f)
    )
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _dailyReminder = MutableStateFlow(
        prefs.getBoolean(KEY_DAILY_REMINDER, false)
    )
    val dailyReminder: StateFlow<Boolean> = _dailyReminder.asStateFlow()

    private val _dailyStudyGoal = MutableStateFlow(
        prefs.getInt(KEY_DAILY_STUDY_GOAL, 10)
    )
    val dailyStudyGoal: StateFlow<Int> = _dailyStudyGoal.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setAudioAutoplay(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUDIO_AUTOPLAY, enabled).apply()
        _audioAutoplay.value = enabled
    }

    fun setQuestionAudio(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUESTION_AUDIO, enabled).apply()
        _questionAudio.value = enabled
    }

    fun setAnswerAudio(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANSWER_AUDIO, enabled).apply()
        _answerAudio.value = enabled
    }

    fun setPlaybackSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_PLAYBACK_SPEED, speed).apply()
        _playbackSpeed.value = speed
    }

    fun setDailyReminder(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_REMINDER, enabled).apply()
        _dailyReminder.value = enabled
    }

    fun setDailyStudyGoal(goal: Int) {
        prefs.edit().putInt(KEY_DAILY_STUDY_GOAL, goal).apply()
        _dailyStudyGoal.value = goal
    }

    fun resetAllProgress() {
        // Keeps onboarding/theme, resets user practice progress
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, false).apply()
        _onboardingCompleted.value = false
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_AUDIO_AUTOPLAY = "audio_autoplay"
        private const val KEY_QUESTION_AUDIO = "question_audio"
        private const val KEY_ANSWER_AUDIO = "answer_audio"
        private const val KEY_PLAYBACK_SPEED = "playback_speed"
        private const val KEY_DAILY_REMINDER = "daily_reminder"
        private const val KEY_DAILY_STUDY_GOAL = "daily_study_goal"

        @Volatile
        private var INSTANCE: PreferencesRepository? = null

        fun getInstance(context: Context): PreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesRepository(context).also { INSTANCE = it }
            }
        }
    }
}
