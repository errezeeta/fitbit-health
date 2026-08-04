package dev.javier.fitbithealth.ui.metricdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.TrendPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MetricDetailState {
    data object Idle : MetricDetailState
    data object Loading : MetricDetailState
    data class Ready(val points: List<TrendPoint>) : MetricDetailState
    data class Error(val message: String) : MetricDetailState
}

class MetricDetailViewModel(private var api: HealthApi?) : ViewModel() {
    private val _state = MutableStateFlow<MetricDetailState>(MetricDetailState.Idle)
    val state: StateFlow<MetricDetailState> = _state.asStateFlow()

    fun load(metric: String) {
        val gateway = api ?: run {
            _state.value = MetricDetailState.Error("Configura el gateway en Ajustes")
            return
        }
        _state.value = MetricDetailState.Loading
        viewModelScope.launch {
            runCatching {
                val end = java.time.LocalDate.now()
                val start = end.minusDays(29)
                gateway.trends(start.toString(), end.toString())[metric] ?: emptyList()
            }.onSuccess { points ->
                _state.value = MetricDetailState.Ready(points)
            }.onFailure { error ->
                _state.value = MetricDetailState.Error(error.message ?: "Error al cargar")
            }
        }
    }
}
