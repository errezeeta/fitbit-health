package dev.javier.fitbithealth.data.api

import kotlinx.serialization.Serializable

@Serializable
data class TrendPoint(
    val date: String,
    val value: Double? = null,
)

typealias TrendsResponse = Map<String, List<TrendPoint>>
