package dev.javier.fitbithealth.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute
@Serializable data object SleepRoute
@Serializable data object TrendsRoute
@Serializable data object ChatRoute
@Serializable data object SettingsRoute

private data class NavItem<T : Any>(val route: T, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation(
    dashboardContent: @Composable () -> Unit = { Text("Dashboard") },
    sleepContent: @Composable () -> Unit = { Text("Sueño") },
    trendsContent: @Composable () -> Unit = { Text("Tendencias") },
    chatContent: @Composable () -> Unit = { Text("Chat de salud") },
    settingsContent: @Composable () -> Unit = { Text("Ajustes") },
) {
    val navController = rememberNavController()
    val items = listOf(
        NavItem(DashboardRoute, "Inicio", Icons.Default.Home),
        NavItem(SleepRoute, "Sueño", Icons.Default.Bedtime),
        NavItem(TrendsRoute, "Tendencias", Icons.Default.Timeline),
        NavItem(ChatRoute, "Chat", Icons.Default.Chat),
        NavItem(SettingsRoute, "Ajustes", Icons.Default.Settings),
    )
    val entry by navController.currentBackStackEntryAsState()
    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = entry?.destination?.hasRoute(item.route::class) == true,
                        onClick = { navController.navigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { _ ->
        NavHost(
            navController,
            startDestination = DashboardRoute,
        ) {
            composable<DashboardRoute> { dashboardContent() }
            composable<SleepRoute> { sleepContent() }
            composable<TrendsRoute> { trendsContent() }
            composable<ChatRoute> { chatContent() }
            composable<SettingsRoute> { settingsContent() }
        }
    }
}
