package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.NavRoutes

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val BottomNavItems = listOf(
    BottomNavItem(
        route = NavRoutes.Home.route,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    BottomNavItem(
        route = NavRoutes.Study.createRoute("ALL"),
        label = "Study",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        testTag = "nav_tab_study"
    ),
    BottomNavItem(
        route = NavRoutes.Practice.route,
        label = "Practice",
        selectedIcon = Icons.AutoMirrored.Filled.Assignment,
        unselectedIcon = Icons.AutoMirrored.Outlined.Assignment,
        testTag = "nav_tab_practice"
    ),
    BottomNavItem(
        route = NavRoutes.Progress.route,
        label = "Progress",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        testTag = "nav_tab_progress"
    ),
    BottomNavItem(
        route = NavRoutes.Settings.route,
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_tab_settings"
    )
)

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        BottomNavItems.forEach { item ->
            val selected = when (item.testTag) {
                "nav_tab_home" -> currentRoute == NavRoutes.Home.route
                "nav_tab_study" -> currentRoute == NavRoutes.Study.route || currentRoute?.startsWith("study") == true
                "nav_tab_practice" -> currentRoute == NavRoutes.Practice.route
                "nav_tab_progress" -> currentRoute == NavRoutes.Progress.route
                "nav_tab_settings" -> currentRoute == NavRoutes.Settings.route
                else -> currentRoute == item.route
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToRoute(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
