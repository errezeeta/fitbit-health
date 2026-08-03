package dev.javier.fitbithealth.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.TrendsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface TrendsState {
    data object Loading : TrendsState
    data class Ready(val trends: TrendsResponse) : TrendsState
    data class Error(val message: String) : TrendsState
}

class TrendsViewModel(private val api: HealthApi?) : ViewModel() {
    private val _state = MutableStateFlow<TrendsState>(TrendsState.Loading)
    val state: StateFlow<TrendsState> = _state.asStateFlow()

    fun load(start: String, end: String) {
        val gateway = api ?: run {
            _state.value = TrendsState.Error("Configura el gateway en Ajustes")
            return
        }
        viewModelScope.launch {
            runCatching { gateway.trends(start, end) }
                .onSuccess { _state.value = TrendsState.Ready(it) }
                .onFailure { _state.value = TrendsState.Error(it.message ?: "No se pudieron cargar las tendencias") }
        }
    }
}
