package dev.javier.fitbithealth.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HealthChatScreen(
    state: ChatState,
    onSend: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val messages = when (state) {
        ChatState.Idle -> emptyList()
        is ChatState.Ready -> state.messages
        is ChatState.Sending -> state.messages
        is ChatState.Error -> state.messages
    }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Chat de salud Fitbit")
        Text("No es un diagnóstico médico. Consulta a un profesional ante cualquier preocupación.")
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { message ->
                Text(if (message.fromUser) "Tú: ${message.text}" else "Salud: ${message.text}")
                if (message.sources.isNotEmpty()) Text("Datos usados: ${message.sources.joinToString()}")
            }
        }
        if (state is ChatState.Sending) CircularProgressIndicator()
        if (state is ChatState.Error) {
            Text(state.message)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it.take(5000) },
                modifier = Modifier.weight(1f),
                label = { Text("Pregunta sobre tus datos") },
            )
            Button(onClick = { onSend(draft); draft = "" }, enabled = draft.isNotBlank()) {
                Text("Enviar")
            }
        }
    }
}
