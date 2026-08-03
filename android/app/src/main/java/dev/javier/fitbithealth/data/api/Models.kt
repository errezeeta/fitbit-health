package dev.javier.fitbithealth.data.api

import kotlinx.serialization.Serializable

@Serializable
data class MetricPoint(
    val timestamp: String,
    val value: Double? = null,
)

@Serializable
data class SleepSession(
    val startTime: String,
    val endTime: String,
    val minutesAsleep: Int,
    val minutesAwake: Int,
    val deepMinutes: Int = 0,
    val lightMinutes: Int = 0,
    val remMinutes: Int = 0,
    val awakeMinutes: Int = 0,
)

@Serializable
data class DashboardResponse(
    val metrics: Map<String, MetricPoint> = emptyMap(),
    val sleep: SleepSession? = null,
    val steps: Int? = null,
    val restingHeartRate: Int? = null,
    val hrv: Double? = null,
    val spo2: Double? = null,
)

@Serializable
data class SyncJobResponse(
    val jobId: String,
    val status: String,
    val detail: String? = null,
)

@Serializable
data class ChatRequest(
    val message: String,
    val context: Map<String, String> = emptyMap(),
)

@Serializable
data class ChatResponse(
    val answer: String,
    val sources: List<String> = emptyList(),
    val medicalDisclaimer: Boolean = true,
)
