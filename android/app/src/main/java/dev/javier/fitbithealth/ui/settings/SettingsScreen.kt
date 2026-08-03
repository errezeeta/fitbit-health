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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    initialUrl: String,
    initialToken: String,
    onSave: (String, String) -> Unit,
    onTestConnection: suspend (String, String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var url by remember { mutableStateOf(initialUrl) }
    var token by remember { mutableStateOf(initialToken) }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Configuración del gateway")
        OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("URL del gateway") })
        OutlinedTextField(token, { token = it }, Modifier.fillMaxWidth(), label = { Text("Token") })
        Button(onClick = {
            onSave(url, token)
            status = "✓ Configuración guardada"
        }) { Text("Guardar") }
        Button(onClick = {
            status = "Probando conexión..."
            scope.launch {
                val ok = onTestConnection(url, token)
                status = if (ok) "✓ Conexión exitosa" else "✗ No se pudo conectar"
            }
        }) { Text("Probar conexión") }
        status?.let { Text(it) }
    }
}
