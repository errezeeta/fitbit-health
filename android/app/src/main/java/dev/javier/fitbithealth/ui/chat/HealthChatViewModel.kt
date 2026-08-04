package dev.javier.fitbithealth.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.javier.fitbithealth.data.api.ChatRequest
import dev.javier.fitbithealth.data.api.ChatResponse
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.data.api.HealthApiFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class ChatMessage(val text: String, val fromUser: Boolean, val sources: List<String> = emptyList())

sealed interface ChatState {
    data object Idle : ChatState
    data class Ready(val messages: List<ChatMessage>) : ChatState
    data class Streaming(val messages: List<ChatMessage>, val partial: String) : ChatState
    data class Sending(val messages: List<ChatMessage>) : ChatState
    data class Error(val messages: List<ChatMessage>, val message: String) : ChatState
}

class HealthChatViewModel(
    private var api: HealthApi?,
    private var factory: HealthApiFactory? = null,
    private var baseUrl: String? = null,
    private var token: String? = null,
) : ViewModel() {
    fun updateApi(newApi: HealthApi?) {
        api = newApi
        factory = null
        baseUrl = null
        token = null
    }

    /** Actualiza también las credenciales para el streaming SSE. */
    fun updateCredentials(factory: HealthApiFactory, baseUrl: String, token: String) {
        this.factory = factory
        this.baseUrl = baseUrl
        this.token = token
    }
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
            val streamFactory = factory
            val streamUrl = baseUrl
            val streamToken = token
            runCatching {
                // Streaming preferido; fallback a POST normal si no hay credenciales de stream
                if (streamFactory != null && !streamUrl.isNullOrBlank() && !streamToken.isNullOrBlank()) {
                    val sb = StringBuilder()
                    _state.value = ChatState.Streaming(current, "")
                    streamFactory.chatStream(streamUrl, streamToken, ChatRequest(text)) { piece ->
                        sb.append(piece)
                        _state.value = ChatState.Streaming(current, sb.toString())
                    }
                    sb.toString()
                } else {
                    gateway.chat(ChatRequest(text)).answer
                }
            }
            .onSuccess { full ->
                _state.value = ChatState.Ready(current + ChatMessage(full, fromUser = false))
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
        is ChatState.Streaming -> current.messages
        is ChatState.Sending -> current.messages
        is ChatState.Error -> current.messages
    }

    private fun ChatResponse.toMessage() = ChatMessage(answer, fromUser = false, sources = sources)
}
