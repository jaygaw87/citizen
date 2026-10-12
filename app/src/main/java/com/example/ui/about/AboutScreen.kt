package com.example.ui.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestConstants
import com.example.ui.components.AppTopBar
import com.example.ui.components.CivicEmblem
import com.example.ui.components.SettingsRow

@Composable
fun AboutScreen(
    onNavigateToDisclaimer: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOfficialResourcesDialog by remember { mutableStateOf(false) }
    var showSourcesDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("about_screen"),
        topBar = {
            AppTopBar(
                title = "About This App",
                showCivicEmblem = false,
                showBackButton = true,
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
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CivicEmblem(
                        size = 64.dp,
                        tint = MaterialTheme.colorScheme.primary,
                        accentTint = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "U.S. Citizenship Test",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Version ${com.example.BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Notice card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "U.S. Citizenship Test is an independent educational tool designed to help users prepare for the U.S. naturalization civics test.\n\n" +
                                   "Not affiliated with or endorsed by USCIS or any U.S. government agency.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(18.dp),
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action links
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingsRow(
                            title = "Official USCIS Resources",
                            subtitle = "Visit uscis.gov/citizenship",
                            icon = Icons.Outlined.OpenInBrowser,
                            onClick = { showOfficialResourcesDialog = true },
                            testTag = "about_official_resources"
                        )

                        SettingsRow(
                            title = "Sources & References",
                            subtitle = "128 public civics test questions",
                            icon = Icons.AutoMirrored.Outlined.MenuBook,
                            onClick = { showSourcesDialog = true },
                            testTag = "about_sources"
                        )

                        SettingsRow(
                            title = "Privacy Policy",
                            subtitle = "Offline-first application privacy information",
                            icon = Icons.Outlined.PrivacyTip,
                            onClick = { showPrivacyDialog = true },
                            testTag = "about_privacy"
                        )

                        SettingsRow(
                            title = "Disclaimer",
                            subtitle = "Independent educational tool notice",
                            icon = Icons.Outlined.Gavel,
                            onClick = onNavigateToDisclaimer,
                            testTag = "about_disclaimer"
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (showOfficialResourcesDialog) {
        AlertDialog(
            onDismissRequest = { showOfficialResourcesDialog = false },
            title = { Text("Official USCIS Resources") },
            text = {
                Text(
                    "For official application filing, test dates, and comprehensive immigration guidelines, please consult the official USCIS website at:\n\n" +
                    "https://www.uscis.gov/citizenship"
                )
            },
            confirmButton = {
                TextButton(onClick = { showOfficialResourcesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showSourcesDialog) {
        AlertDialog(
            onDismissRequest = { showSourcesDialog = false },
            title = { Text("Sources") },
            text = {
                Text(
                    "Study materials are based upon public domain naturalization civics questions and answers provided by the U.S. Citizenship and Immigration Services (USCIS)."
                )
            },
            confirmButton = {
                TextButton(onClick = { showSourcesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    "This application is designed to operate completely offline. We do not collect, track, store on remote servers, or transmit any personal data, study progress, or test scores to third parties."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
