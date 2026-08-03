package dev.javier.fitbithealth.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.javier.fitbithealth.data.api.ChatRequest
import dev.javier.fitbithealth.data.api.ChatResponse
import dev.javier.fitbithealth.data.api.HealthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class ChatMessage(val text: String, val fromUser: Boolean, val sources: List<String> = emptyList())

sealed interface ChatState {
    data object Idle : ChatState
    data class Ready(val messages: List<ChatMessage>) : ChatState
    data class Sending(val messages: List<ChatMessage>) : ChatState
    data class Error(val messages: List<ChatMessage>, val message: String) : ChatState
}

class HealthChatViewModel(private val api: HealthApi?) : ViewModel() {
    private val _state = MutableStateFlow<ChatState>(ChatState.Idle)
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun send(message: String) {
        val text = message.trim()
        if (text.isEmpty()) return
        val current = messages() + ChatMessage(text, fromUser = true)
        _state.value = ChatState.Sending(current)
        val gateway = api ?: run {
            _state.value = ChatState.Error(current, "Configura el gateway en Ajustes.")
            return
        }
        viewModelScope.launch {
            runCatching { gateway.chat(ChatRequest(text)) }
                .onSuccess { response ->
                    _state.value = ChatState.Ready(current + response.toMessage())
                }
                .onFailure {
                    _state.value = ChatState.Error(current, "No se pudo conectar con el chat de salud.")
                }
        }
    }

    fun retryLast() {
        val last = messages().lastOrNull { it.fromUser } ?: return
        send(last.text)
    }

    private fun messages(): List<ChatMessage> = when (val current = _state.value) {
        ChatState.Idle -> emptyList()
        is ChatState.Ready -> current.messages
        is ChatState.Sending -> current.messages
        is ChatState.Error -> current.messages
    }

    private fun ChatResponse.toMessage() = ChatMessage(answer, fromUser = false, sources = sources)
}
