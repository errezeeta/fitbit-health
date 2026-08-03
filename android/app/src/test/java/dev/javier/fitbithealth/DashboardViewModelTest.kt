package dev.javier.fitbithealth

import dev.javier.fitbithealth.data.api.DashboardResponse
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.MetricPoint
import dev.javier.fitbithealth.data.api.SleepSession
import dev.javier.fitbithealth.data.api.SyncJobResponse
import dev.javier.fitbithealth.ui.dashboard.DashboardState
import dev.javier.fitbithealth.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun successfulLoadProducesReadyState() = runTest {
        val api = FakeApi()
        val viewModel = DashboardViewModel(api)
        viewModel.load("2026-08-03")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value is DashboardState.Ready)
    }

    @Test
    fun failedLoadProducesErrorState() = runTest {
        val viewModel = DashboardViewModel(FakeApi(shouldFail = true))
        viewModel.load("2026-08-03")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value is DashboardState.Error)
    }

    private class FakeApi(private val shouldFail: Boolean = false) : HealthApi {
        override suspend fun health() = emptyMap<String, String>()
        override suspend fun dashboard(day: String): DashboardResponse {
            if (shouldFail) error("offline")
            return DashboardResponse()
        }
        override suspend fun sleep(start: String, end: String) = emptyList<SleepSession>()
        override suspend fun metric(metric: String, start: String, end: String) = emptyList<MetricPoint>()
        override suspend fun sync() = SyncJobResponse("job", "queued")
        override suspend fun trends(start: String, end: String) = emptyMap<String, List<TrendPoint>>()
    }
}
