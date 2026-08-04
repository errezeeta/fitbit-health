package dev.javier.fitbithealth.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.javier.fitbithealth.ui.theme.NeoSurface
import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute
@Serializable data object SleepRoute
@Serializable data object TrendsRoute
@Serializable data object ChatRoute
@Serializable data object SettingsRoute
@Serializable data class MetricDetailRoute(val metric: String)

private data class NavItem<T : Any>(val route: T, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation(
    dashboardContent: @Composable (onMetricClick: (String) -> Unit) -> Unit = { _ -> Text("Dashboard") },
    sleepContent: @Composable () -> Unit = { Text("Sueño") },
    trendsContent: @Composable () -> Unit = { Text("Tendencias") },
    chatContent: @Composable () -> Unit = { Text("Chat de salud") },
    settingsContent: @Composable () -> Unit = { Text("Ajustes") },
    metricDetailContent: @Composable (String, onBack: () -> Unit) -> Unit = { _, _ -> },
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = NeoSurface,
                tonalElevation = 0.dp,
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = entry?.destination?.hasRoute(item.route::class) == true,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(padding),
        ) {
        composable<DashboardRoute> {
            dashboardContent { metric ->
                navController.navigate(MetricDetailRoute(metric)) {
                    launchSingleTop = true
                }
            }
        }
            composable<SleepRoute> { sleepContent() }
            composable<TrendsRoute> { trendsContent() }
            composable<ChatRoute> { chatContent() }
            composable<SettingsRoute> { settingsContent() }
            composable<MetricDetailRoute> { entry ->
                val route = entry.toRoute<MetricDetailRoute>()
                metricDetailContent(route.metric) {
                    navController.popBackStack()
                }
            }
        }
    }
}
