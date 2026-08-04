package dev.javier.fitbithealth.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.javier.fitbithealth.data.api.DashboardResponse
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.SyncJobResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardState {
    data object Loading : DashboardState
    data class Ready(val dashboard: DashboardResponse, val sync: SyncJobResponse? = null) : DashboardState
    data class Error(val message: String, val canRetry: Boolean = true) : DashboardState
}

class DashboardViewModel(private var api: HealthApi?) : ViewModel() {
    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun updateApi(newApi: HealthApi?) {
        api = newApi
    }

    fun load(day: String) {
        _state.value = DashboardState.Loading
        val gateway = api ?: run {
            _state.value = DashboardState.Error("Configura el gateway en Ajustes")
            return
        }
        viewModelScope.launch {
            runCatching { gateway.dashboard(day) }
                .onSuccess { _state.value = DashboardState.Ready(it) }
                .onFailure { _state.value = DashboardState.Error(it.message ?: "No se pudo cargar el dashboard") }
        }
    }

    fun syncNow() {
        val gateway = api ?: run {
            _state.value = DashboardState.Error("Configura el gateway en Ajustes")
            return
        }
        viewModelScope.launch {
            runCatching { gateway.sync() }
                .onSuccess { current ->
                    val currentState = _state.value
                    if (currentState is DashboardState.Ready) {
                        _state.value = currentState.copy(sync = current)
                    }
                }
                .onFailure { _state.value = DashboardState.Error(it.message ?: "No se pudo sincronizar") }
        }
    }
}
