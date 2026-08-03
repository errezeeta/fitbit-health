package dev.javier.fitbithealth

import dev.javier.fitbithealth.data.api.DashboardResponse
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.MetricPoint
import dev.javier.fitbithealth.data.api.SleepSession
import dev.javier.fitbithealth.data.api.SyncJobResponse
import dev.javier.fitbithealth.ui.sleep.SleepState
import dev.javier.fitbithealth.ui.sleep.SleepViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SleepViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadReturnsSessions() = runTest {
        val expected = SleepSession("2026-08-03T01:34:00+02:00", "2026-08-03T07:22:00+02:00", 342, 6, 111, 150, 81, 5)
        val viewModel = SleepViewModel(FakeApi(expected))
        viewModel.load("2026-08-03", "2026-08-03")
        dispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.state.value as SleepState.Ready
        assertEquals(expected, state.sessions.single())
    }

    @Test
    fun apiFailureProducesError() = runTest {
        val viewModel = SleepViewModel(FakeApi(fail = true))
        viewModel.load("2026-08-03", "2026-08-03")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value is SleepState.Error)
    }

    private class FakeApi(
        private val expected: SleepSession = SleepSession("a", "b", 1, 0),
        private val fail: Boolean = false,
    ) : HealthApi {
        override suspend fun health() = emptyMap<String, String>()
        override suspend fun dashboard(day: String) = DashboardResponse()
        override suspend fun sleep(start: String, end: String): List<SleepSession> {
            if (fail) error("offline")
            return listOf(expected)
        }
        override suspend fun metric(metric: String, start: String, end: String) = emptyList<MetricPoint>()
        override suspend fun sync() = SyncJobResponse("job", "queued")
        override suspend fun trends(start: String, end: String) = emptyMap<String, List<TrendPoint>>()
    }
}
