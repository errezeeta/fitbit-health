package dev.javier.fitbithealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import dev.javier.fitbithealth.ui.theme.LightThemeColors
import dev.javier.fitbithealth.ui.theme.DarkThemeColors
import dev.javier.fitbithealth.ui.theme.AppFonts
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.ui.dashboard.DashboardViewModel
import dev.javier.fitbithealth.ui.chat.HealthChatScreen
import dev.javier.fitbithealth.ui.chat.HealthChatViewModel
import dev.javier.fitbithealth.ui.dashboard.DashboardScreen
import dev.javier.fitbithealth.ui.navigation.AppNavigation
import dev.javier.fitbithealth.ui.settings.SettingsScreen
import dev.javier.fitbithealth.ui.sleep.SleepScreen
import dev.javier.fitbithealth.ui.sleep.SleepViewModel
import dev.javier.fitbithealth.ui.trends.TrendsScreen
import dev.javier.fitbithealth.ui.trends.TrendsViewModel
import dev.javier.fitbithealth.ui.metricdetail.MetricDetailScreen
import dev.javier.fitbithealth.ui.metrics.HealthRange
import dev.javier.fitbithealth.data.api.HealthApiFactory

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var sleepViewModel: SleepViewModel
    private lateinit var chatViewModel: HealthChatViewModel
    private lateinit var trendsViewModel: TrendsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        initViewModels()
        setContent {
            val dark = isSystemInDarkTheme()
            MaterialTheme(
                colorScheme = if (dark) DarkThemeColors else LightThemeColors,
                typography = AppFonts,
            ) {
                AppNavigation(
                    dashboardContent = { onMetricClick ->
                        DashboardScreen(
                            state = dashboardViewModel.state.collectAsState().value,
                            onRetry = { dashboardViewModel.load(java.time.LocalDate.now().toString()) },
                            onSync = { dashboardViewModel.syncNow() },
                            onMetricClick = onMetricClick,
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
                        SettingsScreen(
                            initialUrl = settings.gatewayUrl,
                            initialToken = settings.gatewayToken,
                            onSave = { url, token ->
                                settings.gatewayUrl = url
                                settings.gatewayToken = token
                                val newApi = container.apiOrNull()
                                dashboardViewModel.updateApi(newApi)
                                sleepViewModel.updateApi(newApi)
                                chatViewModel.updateApi(newApi)
                                trendsViewModel.updateApi(newApi)
                                loadAll()
                            },
                            onTestConnection = { url, token ->
                                runCatching {
                                    val api = HealthApiFactory().create(url, token)
                                    api.health()
                                    true
                                }.getOrDefault(false)
                            },
                        )
                    },
                    metricDetailContent = { metric, onBack ->
                        MetricDetailScreen(
                            metric = metric,
                            api = container.apiOrNull(),
                            onBack = onBack,
                        )
                    },
                )
            }
        }
        loadAll()
    }

    private fun initViewModels() {
        val api = container.apiOrNull()
        dashboardViewModel = DashboardViewModel(api)
        sleepViewModel = SleepViewModel(api)
        chatViewModel = HealthChatViewModel(api)
        trendsViewModel = TrendsViewModel(api)
    }

    private fun loadAll() {
        val today = java.time.LocalDate.now().toString()
        dashboardViewModel.load(today)
        sleepViewModel.load(today, today)
        trendsViewModel.load(today, today)
    }
}
