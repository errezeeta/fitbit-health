package dev.javier.fitbithealth.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetricPoint(
    val timestamp: String,
    val value: Double? = null,
)

@Serializable
data class SleepSession(
    @SerialName("id") val id: Long? = null,
    @SerialName("google_id") val googleId: String? = null,
    @SerialName("date_of_sleep") val dateOfSleep: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("minutes_in_sleep_period") val minutesInSleepPeriod: Int? = null,
    @SerialName("minutes_asleep") val minutesAsleep: Int? = null,
    @SerialName("minutes_awake") val minutesAwake: Int? = null,
    @SerialName("deep_minutes") val deepMinutes: Int? = null,
    @SerialName("light_minutes") val lightMinutes: Int? = null,
    @SerialName("rem_minutes") val remMinutes: Int? = null,
    @SerialName("awake_minutes") val awakeMinutes: Int? = null,
    @SerialName("deep_count") val deepCount: Int? = null,
    @SerialName("light_count") val lightCount: Int? = null,
    @SerialName("rem_count") val remCount: Int? = null,
    @SerialName("awake_count") val awakeCount: Int? = null,
    @SerialName("minutes_to_fall_asleep") val minutesToFallAsleep: Int? = null,
    @SerialName("minutes_after_wakeup") val minutesAfterWakeup: Int? = null,
)

@Serializable
data class DashboardResponse(
    val date: String? = null,
    @SerialName("resting_heart_rate") val restingHeartRate: Int? = null,
    val steps: Int? = null,
    val hrv: Double? = null,
    val spo2: Double? = null,
    val sleep: SleepSession? = null,
)

@Serializable
data class SyncJobResponse(
    @SerialName("job_id") val jobId: String,
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
    @SerialName("medical_disclaimer") val medicalDisclaimer: Boolean = true,
)
