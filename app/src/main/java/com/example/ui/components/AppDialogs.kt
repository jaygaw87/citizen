package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.appColors
import com.example.ui.theme.appSpacing

/**
 * Standard confirmation dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    icon: ImageVector = Icons.AutoMirrored.Outlined.HelpOutline,
    testTag: String = "app_confirmation_dialog"
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag(testTag),
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppTertiaryButton(
                        text = dismissButtonText,
                        onClick = onDismissRequest
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppPrimaryButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        }
    }
}

/**
 * Informational dialog with single dismiss action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppInformationDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    buttonText: String = "Got it",
    icon: ImageVector = Icons.Outlined.Info,
    testTag: String = "app_information_dialog"
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag(testTag)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppPrimaryButton(
                        text = buttonText,
                        onClick = onDismissRequest,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }
    }
}

/**
 * Destructive confirmation dialog (e.g. for Reset Learning Progress)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDestructiveConfirmationDialog(
    onDismissRequest: () -> Unit,
    title: String = "Reset Progress?",
    message: String,
    confirmButtonText: String = "Reset Everything",
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    testTag: String = "app_destructive_dialog"
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.testTag(testTag)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.appColors.error,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.appColors.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppTertiaryButton(
                        text = dismissButtonText,
                        onClick = onDismissRequest
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AppDestructiveButton(
                        text = confirmButtonText,
                        onClick = onConfirm,
                        modifier = Modifier.defaultMinSize(minWidth = 140.dp)
                    )
                }
            }
        }
    }
}
