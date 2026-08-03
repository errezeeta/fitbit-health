package dev.javier.fitbithealth.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun SettingsScreen(
    initialUrl: String,
    onSave: (String, String) -> Unit,
    onTestConnection: suspend (String, String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var url by remember { mutableStateOf(initialUrl) }
    var token by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Configuración del gateway")
        OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("URL Tailscale") })
        OutlinedTextField(token, { token = it }, Modifier.fillMaxWidth(), label = { Text("Token") })
        Button(onClick = { onSave(url, token) }) { Text("Guardar") }
        Button(onClick = { status = "Usa Guardar para probar la conexión." }) { Text("Probar conexión") }
        status?.let { Text(it) }
    }
}
