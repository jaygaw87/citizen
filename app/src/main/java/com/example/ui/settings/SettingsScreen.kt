package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.SettingsUiState
import com.example.ui.components.AppCard
import com.example.ui.components.AppTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.SettingsRow
import com.example.ui.components.SettingsSwitchRow
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeChange: (String) -> Unit,
    onQuestionAudioChange: (Boolean) -> Unit = {},
    onAnswerAudioChange: (Boolean) -> Unit = {},
    onAudioAutoplayChange: (Boolean) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onDailyStudyGoalChange: (Int) -> Unit = {},
    onResetProgress: () -> Unit,
    onResetOnboarding: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToDisclaimer: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showDailyGoalDialog by remember { mutableStateOf(false) }
    var showResetProgressDialog by remember { mutableStateOf(false) }
    var showResetOnboardingDialog by remember { mutableStateOf(false) }
    var showSourcesDialog by remember { mutableStateOf(false) }
    var showTestDateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Settings",
                subtitle = "App preferences & data management",
                showCivicEmblem = false,
                showBackButton = onBackClick != null,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ===================================================
                    // SECTION 1 — STUDY PREFERENCES
                    // ===================================================
                    Column {
                        SectionHeader(
                            title = "Study Preferences",
                            subtitle = "Daily targets and test scheduling"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppCard(
                            shape = RoundedCornerShape(14.dp),
                            elevation = 0.5.dp
                        ) {
                            val goalLabel = if (uiState.dailyStudyGoal > 0) "${uiState.dailyStudyGoal} Qs / day" else "Off"
                            SettingsRow(
                                title = "Daily Study Goal",
                                subtitle = if (uiState.dailyStudyGoal > 0) "${uiState.dailyStudyGoal} questions per day target" else "Goal tracking turned off",
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                trailingText = goalLabel,
                                onClick = { showDailyGoalDialog = true },
                                testTag = "settings_daily_goal"
                            )

                            SettingsSwitchRow(
                                title = "Daily Reminder",
                                subtitle = "Notification prompt to study civics",
                                icon = Icons.Outlined.Notifications,
                                checked = uiState.dailyReminder,
                                onCheckedChange = onDailyReminderChange,
                                testTag = "settings_daily_reminder"
                            )

                            SettingsRow(
                                title = "Naturalization Test Date",
                                subtitle = uiState.testDate ?: "Not scheduled yet",
                                icon = Icons.Outlined.CalendarToday,
                                trailingText = if (uiState.testDate != null) "Set" else "Set Date",
                                onClick = { showTestDateDialog = true },
                                testTag = "settings_test_date"
                            )
                        }
                    }

                    // ===================================================
                    // SECTION 2 — AUDIO & SPEAKING
                    // ===================================================
                    Column {
                        SectionHeader(
                            title = "Audio & Speech",
                            subtitle = "TTS voice playback, speech recognition and speeds"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppCard(
                            shape = RoundedCornerShape(14.dp),
                            elevation = 0.5.dp
                        ) {
                            SettingsSwitchRow(
                                title = "Question Audio",
                                subtitle = "Hear civics questions spoken aloud",
                                icon = Icons.Outlined.Headphones,
                                checked = uiState.questionAudio,
                                onCheckedChange = onQuestionAudioChange,
                                testTag = "settings_question_audio"
                            )

                            SettingsSwitchRow(
                                title = "Answer Audio",
                                subtitle = "Read accepted answers aloud after evaluation",
                                icon = Icons.Outlined.Headphones,
                                checked = uiState.answerAudio,
                                onCheckedChange = onAnswerAudioChange,
                                testTag = "settings_answer_audio"
                            )

                            SettingsSwitchRow(
                                title = "Auto-play Question Audio",
                                subtitle = "Automatically read questions when opened",
                                icon = Icons.Outlined.Headphones,
                                checked = uiState.audioAutoplay,
                                onCheckedChange = onAudioAutoplayChange,
                                testTag = "settings_audio_autoplay"
                            )

                            val speedLabel = when (uiState.playbackSpeed) {
                                0.8f -> "Slow (0.8x)"
                                1.15f -> "Fast (1.15x)"
                                else -> "Normal (1.0x)"
                            }

                            SettingsRow(
                                title = "TTS Playback Speed",
                                subtitle = "Adjust spoken speech rate",
                                icon = Icons.Outlined.Speed,
                                trailingText = speedLabel,
                                onClick = { showSpeedDialog = true },
                                testTag = "settings_playback_speed"
                            )
                        }
                    }

                    // ===================================================
                    // SECTION 3 — APPEARANCE
                    // ===================================================
                    Column {
                        SectionHeader(
                            title = "Appearance",
                            subtitle = "Customize application theme"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppCard(
                            shape = RoundedCornerShape(14.dp),
                            elevation = 0.5.dp
                        ) {
                            SettingsRow(
                                title = "Theme Mode",
                                subtitle = when (uiState.themeMode) {
                                    "LIGHT" -> "Light Mode"
                                    "DARK" -> "Dark Mode"
                                    else -> "System Default"
                                },
                                icon = Icons.Outlined.DarkMode,
                                trailingText = when (uiState.themeMode) {
                                    "LIGHT" -> "Light"
                                    "DARK" -> "Dark"
                                    else -> "System"
                                },
                                onClick = { showThemeDialog = true },
                                testTag = "settings_theme_row"
                            )
                        }
                    }

                    // ===================================================
                    // SECTION 4 — DATA MANAGEMENT (Destructive Actions Prominent)
                    // ===================================================
                    Column {
                        SectionHeader(
                            title = "Data Management",
                            subtitle = "Reset progress and walkthrough preferences"
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Destructive Reset Card - Clearly Identifiable
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.40f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SettingsRow(
                                title = "Reset Learning Progress",
                                subtitle = "Clear question progress, flashcard status, and mastered counts (preserves 128 questions, favorites, & test history)",
                                icon = Icons.Outlined.DeleteOutline,
                                iconTint = MaterialTheme.colorScheme.error,
                                onClick = { showResetProgressDialog = true },
                                testTag = "settings_reset_progress"
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        AppCard(
                            shape = RoundedCornerShape(14.dp),
                            elevation = 0.5.dp
                        ) {
                            SettingsRow(
                                title = "Reset Onboarding",
                                subtitle = "Show introduction walkthrough on next app start",
                                icon = Icons.Outlined.RestartAlt,
                                onClick = { showResetOnboardingDialog = true },
                                testTag = "settings_reset_onboarding"
                            )
                        }
                    }

                    // ===================================================
                    // SECTION 5 — ABOUT & LEGAL
                    // ===================================================
                    Column {
                        SectionHeader(
                            title = "About & Disclaimer",
                            subtitle = "Legal disclaimers and USCIS source documentation"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppCard(
                            shape = RoundedCornerShape(14.dp),
                            elevation = 0.5.dp
                        ) {
                            SettingsRow(
                                title = "U.S. Citizenship Test",
                                subtitle = "Version ${com.example.BuildConfig.VERSION_NAME} • Independent civics preparation tool",
                                icon = Icons.Outlined.Info,
                                onClick = onNavigateToAbout,
                                testTag = "settings_about"
                            )

                            SettingsRow(
                                title = "Official USCIS Sources",
                                subtitle = "Based on official public USCIS naturalization materials",
                                icon = Icons.AutoMirrored.Outlined.MenuBook,
                                onClick = { showSourcesDialog = true },
                                testTag = "settings_sources"
                            )

                            SettingsRow(
                                title = "Independent Tool Disclaimer",
                                subtitle = "Not affiliated with or endorsed by USCIS or US Government",
                                icon = Icons.Outlined.Gavel,
                                onClick = onNavigateToDisclaimer,
                                testTag = "settings_disclaimer"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Dialog: Theme Selection
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    val themes = listOf(
                        "SYSTEM" to "System Default",
                        "LIGHT" to "Light Mode",
                        "DARK" to "Dark Mode"
                    )
                    themes.forEach { (mode, label) ->
                        TextButton(
                            onClick = {
                                onThemeChange(mode)
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (uiState.themeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Dialog: Playback Speed
    if (showSpeedDialog) {
        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("TTS Playback Speed") },
            text = {
                Column {
                    val speeds = listOf(
                        0.8f to "Slow (0.8x)",
                        1.0f to "Normal (1.0x)",
                        1.15f to "Fast (1.15x)"
                    )
                    speeds.forEach { (speed, label) ->
                        TextButton(
                            onClick = {
                                onPlaybackSpeedChange(speed)
                                showSpeedDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (uiState.playbackSpeed == speed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Daily Study Goal
    if (showDailyGoalDialog) {
        val goals = listOf(
            10 to "10 questions / day",
            20 to "20 questions / day",
            30 to "30 questions / day",
            0 to "Off"
        )
        AlertDialog(
            onDismissRequest = { showDailyGoalDialog = false },
            title = { Text("Daily Study Goal") },
            text = {
                Column {
                    goals.forEach { (goal, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDailyStudyGoalChange(goal)
                                    showDailyGoalDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.dailyStudyGoal == goal,
                                onClick = {
                                    onDailyStudyGoalChange(goal)
                                    showDailyGoalDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDailyGoalDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Dialog: Test Date
    if (showTestDateDialog) {
        AlertDialog(
            onDismissRequest = { showTestDateDialog = false },
            title = { Text("Test Date") },
            text = {
                Text("Track countdown to your scheduled naturalization interview appointment date.")
            },
            confirmButton = {
                TextButton(onClick = { showTestDateDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // Dialog: Sources
    if (showSourcesDialog) {
        AlertDialog(
            onDismissRequest = { showSourcesDialog = false },
            title = { Text("Official USCIS Sources") },
            text = {
                Text(
                    "All civics study questions and answers are based on the official public naturalization test materials published by the U.S. Citizenship and Immigration Services (USCIS).\n\n" +
                        "For official guidelines and materials, visit:\nhttps://www.uscis.gov/citizenship"
                )
            },
            confirmButton = {
                TextButton(onClick = { showSourcesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Dialog: Reset Progress (Destructive Confirmation Dialog)
    if (showResetProgressDialog) {
        AlertDialog(
            onDismissRequest = { showResetProgressDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Reset Learning Progress?") },
            text = {
                Text(
                    "This will reset your question progress, civics vocabulary learning progress, mastery, and daily streak.\n\n" +
                        "Your 128 questions, saved favorites, practice history, and settings will remain preserved."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetProgress()
                        showResetProgressDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Learning progress reset")
                        }
                    },
                    modifier = Modifier.testTag("confirm_reset_progress_button")
                ) {
                    Text("Reset Progress", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetProgressDialog = false },
                    modifier = Modifier.testTag("cancel_reset_progress_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Reset Onboarding
    if (showResetOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { showResetOnboardingDialog = false },
            title = { Text("Reset Onboarding?") },
            text = {
                Text("The introductory walkthrough will be displayed next time you launch the application.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetOnboarding()
                        showResetOnboardingDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Onboarding walkthrough reset for next launch")
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetOnboardingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
