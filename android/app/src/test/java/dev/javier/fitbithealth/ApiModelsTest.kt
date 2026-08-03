package dev.javier.fitbithealth

import dev.javier.fitbithealth.data.api.MetricPoint
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiModelsTest {
    @Test
    fun parsesUnknownFieldsSafely() {
        val value = Json { ignoreUnknownKeys = true }
            .decodeFromString<MetricPoint>("""{"timestamp":"2026-08-03T07:22:00+02:00","value":71,"private":"ignored"}""")
        assertEquals(71.0, value.value!!, 0.0)
    }
}
