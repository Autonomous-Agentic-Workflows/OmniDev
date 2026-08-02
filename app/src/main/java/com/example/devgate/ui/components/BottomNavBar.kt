package com.example.devgate.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.devgate.ui.viewmodel.DevGateScreen

data class NavTabItem(
    val screen: DevGateScreen,
    val title: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun BottomNavBar(
    currentScreen: DevGateScreen,
    onSelectScreen: (DevGateScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavTabItem(DevGateScreen.DASHBOARD, "Gate", Icons.Default.Dashboard, "nav_dashboard"),
        NavTabItem(DevGateScreen.GIT, "Git", Icons.Default.AccountTree, "nav_git"),
        NavTabItem(DevGateScreen.GEMINI_CLI, "CLI", Icons.Default.Terminal, "nav_cli"),
        NavTabItem(DevGateScreen.GEMMA, "Gemma", Icons.Default.Memory, "nav_gemma"),
        NavTabItem(DevGateScreen.SPARK, "Spark", Icons.Default.Bolt, "nav_spark"),
        NavTabItem(DevGateScreen.JULES, "Jules", Icons.Default.SmartToy, "nav_jules"),
        NavTabItem(DevGateScreen.SETTINGS, "Config", Icons.Default.Settings, "nav_settings")
    )

    NavigationBar(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectScreen(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(item.tag)
            )
        }
    }
}
