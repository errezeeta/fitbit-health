package dev.javier.fitbithealth.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.SleepSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SleepState {
    data object Loading : SleepState
    data class Ready(val sessions: List<SleepSession>) : SleepState
    data class Error(val message: String) : SleepState
}

class SleepViewModel(private var api: HealthApi?) : ViewModel() {
    fun updateApi(newApi: HealthApi?) { api = newApi }
    private val _state = MutableStateFlow<SleepState>(SleepState.Loading)
    val state: StateFlow<SleepState> = _state.asStateFlow()

    fun load(start: String, end: String) {
        _state.value = SleepState.Loading
        val gateway = api ?: run {
            _state.value = SleepState.Error("Configura el gateway en Ajustes")
            return
        }
        viewModelScope.launch {
            runCatching { gateway.sleep(start, end) }
                .onSuccess { _state.value = SleepState.Ready(it) }
                .onFailure { _state.value = SleepState.Error(it.message ?: "No se pudo cargar el sueño") }
        }
    }
}
