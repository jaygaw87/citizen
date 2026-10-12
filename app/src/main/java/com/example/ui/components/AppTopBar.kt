package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String = "U.S. Citizenship Test",
    subtitle: String? = null,
    showCivicEmblem: Boolean = true,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    backButtonTestTag: String = "top_bar_back_button",
    showSettingsButton: Boolean = false,
    onSettingsClick: (() -> Unit)? = null,
    settingsButtonTestTag: String = "top_bar_settings_button",
    actions: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        navigationIcon = {
            if (showBackButton && onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .testTag(backButtonTestTag)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else if (showCivicEmblem) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                ) {
                    CivicEmblem(
                        size = 30.dp,
                        tint = MaterialTheme.colorScheme.primary,
                        accentTint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        },
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            actions?.invoke()
            if (showSettingsButton && onSettingsClick != null) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .testTag(settingsButtonTestTag)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Open settings",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    )
}
