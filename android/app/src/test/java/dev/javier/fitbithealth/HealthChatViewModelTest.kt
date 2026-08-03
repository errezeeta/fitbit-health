package dev.javier.fitbithealth

import dev.javier.fitbithealth.data.api.*
import dev.javier.fitbithealth.ui.chat.ChatState
import dev.javier.fitbithealth.ui.chat.HealthChatViewModel
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
class HealthChatViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun sendsQuestionAndShowsAnswer() = runTest {
        val viewModel = HealthChatViewModel(FakeApi())
        viewModel.send("¿Cómo dormí?")
        dispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.state.value as ChatState.Ready
        assertEquals(2, state.messages.size)
        assertTrue(state.messages.last().text.contains("342"))
    }

    @Test fun failedRequestShowsRetryableError() = runTest {
        val viewModel = HealthChatViewModel(FakeApi(fail = true))
        viewModel.send("¿Cómo dormí?")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value is ChatState.Error)
    }

    private class FakeApi(private val fail: Boolean = false) : HealthApi {
        override suspend fun health() = emptyMap<String, String>()
        override suspend fun dashboard(day: String) = DashboardResponse()
        override suspend fun sleep(start: String, end: String) = emptyList<SleepSession>()
        override suspend fun metric(metric: String, start: String, end: String) = emptyList<MetricPoint>()
        override suspend fun sync() = SyncJobResponse("job", "queued")
        override suspend fun trends(start: String, end: String) = emptyMap<String, List<TrendPoint>>()
        override suspend fun chat(request: ChatRequest): ChatResponse {
            if (fail) error("offline")
            return ChatResponse("Dormiste 342 minutos.", listOf("sleep:2026-08-03"))
        }
    }
}
