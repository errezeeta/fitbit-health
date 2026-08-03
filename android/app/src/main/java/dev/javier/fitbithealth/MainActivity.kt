package dev.javier.fitbithealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import dev.javier.fitbithealth.ui.dashboard.DashboardViewModel
import dev.javier.fitbithealth.ui.chat.HealthChatScreen
import dev.javier.fitbithealth.ui.chat.ChatState
import dev.javier.fitbithealth.ui.chat.HealthChatViewModel
import dev.javier.fitbithealth.ui.dashboard.DashboardScreen
import dev.javier.fitbithealth.ui.dashboard.DashboardState
import dev.javier.fitbithealth.ui.navigation.AppNavigation
import dev.javier.fitbithealth.ui.settings.SettingsScreen
import dev.javier.fitbithealth.ui.sleep.SleepScreen
import dev.javier.fitbithealth.ui.sleep.SleepState
import dev.javier.fitbithealth.ui.sleep.SleepViewModel
import dev.javier.fitbithealth.ui.trends.TrendsScreen
import dev.javier.fitbithealth.ui.trends.TrendsState
import dev.javier.fitbithealth.ui.trends.TrendsViewModel
import dev.javier.fitbithealth.ui.metrics.HealthRange

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        val api = container.apiOrNull()
        val dashboardViewModel = DashboardViewModel(api)
        val sleepViewModel = SleepViewModel(api)
        val chatViewModel = HealthChatViewModel(api)
        val trendsViewModel = TrendsViewModel(api)
        setContent {
            MaterialTheme {
                AppNavigation(
                    dashboardContent = {
                        DashboardScreen(
                            state = dashboardViewModel.state.collectAsState().value,
                            onRetry = { dashboardViewModel.load(java.time.LocalDate.now().toString()) },
                            onSync = { dashboardViewModel.syncNow() },
                        )
                    },
                    sleepContent = {
                        SleepScreen(sleepViewModel.state.collectAsState().value)
                    },
                    trendsContent = {
                        TrendsScreen(
                            trendsViewModel.state.collectAsState().value,
                            HealthRange.SevenDays,
                            { range ->
                                val end = java.time.LocalDate.now()
                                val dates = dev.javier.fitbithealth.ui.metrics.rangeQuery(range, end)
                                trendsViewModel.load(dates.first, dates.second)
                            },
                        )
                    },
                    chatContent = {
                        val chatState = chatViewModel.state.collectAsState().value
                        HealthChatScreen(chatState, chatViewModel::send, chatViewModel::retryLast)
                    },
                    settingsContent = {
                        val settings = container.currentSettings()
                        SettingsScreen(settings.gatewayUrl, { url, token ->
                            settings.gatewayUrl = url
                            settings.gatewayToken = token
                        }, { _, _ -> false })
                    },
                )
            }
        }
        dashboardViewModel.load(java.time.LocalDate.now().toString())
        val today = java.time.LocalDate.now().toString()
        sleepViewModel.load(today, today)
        trendsViewModel.load(today, today)
    }
}
