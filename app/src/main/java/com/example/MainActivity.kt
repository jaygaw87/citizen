package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.data.local.DatabaseProvider
import com.example.data.local.PreferencesRepository
import com.example.data.model.SettingsUiState
import com.example.ui.navigation.AppNavGraph
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CivicsDiagnostic"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesRepository = PreferencesRepository.getInstance(applicationContext)
        val questionRepository = DatabaseProvider.getQuestionRepository(applicationContext)

        // Ensure database is initialized proactively upon Activity creation
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = questionRepository.ensureInitialized()
                Log.i(TAG, "[MainActivity] ensureInitialized finished: $result")
            } catch (e: Exception) {
                Log.e(TAG, "[MainActivity] ensureInitialized failed", e)
            }
        }

        setContent {
            val onboardingCompleted by preferencesRepository.onboardingCompleted.collectAsState()
            val themeMode by preferencesRepository.themeMode.collectAsState()
            val questionAudio by preferencesRepository.questionAudio.collectAsState()
            val answerAudio by preferencesRepository.answerAudio.collectAsState()
            val audioAutoplay by preferencesRepository.audioAutoplay.collectAsState()
            val playbackSpeed by preferencesRepository.playbackSpeed.collectAsState()
            val dailyReminder by preferencesRepository.dailyReminder.collectAsState()

            val isDarkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            val settingsUiState = SettingsUiState(
                themeMode = themeMode,
                questionAudio = questionAudio,
                answerAudio = answerAudio,
                audioAutoplay = audioAutoplay,
                playbackSpeed = playbackSpeed,
                dailyReminder = dailyReminder,
                onboardingCompleted = onboardingCompleted
            )

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    preferencesRepository = preferencesRepository,
                    settingsUiState = settingsUiState,
                    onboardingCompleted = onboardingCompleted,
                    questionRepository = questionRepository
                )
            }
        }
    }
}
