package dev.javier.fitbithealth.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.data.updater.AppUpdater
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    initialUrl: String,
    initialToken: String,
    onSave: (String, String) -> Unit,
    onTestConnection: suspend (String, String) -> Boolean,
    updater: AppUpdater? = null,
    modifier: Modifier = Modifier,
) {
    var url by remember { mutableStateOf(initialUrl) }
    var token by remember { mutableStateOf(initialToken) }
    var status by remember { mutableStateOf<ConnectionStatus?>(null) }
    var updateState by remember { mutableStateOf<UpdateUiState>(UpdateUiState.Idle) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Conexión con el gateway", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL del gateway") },
                    leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Token") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    visualTransformation = if (token.isNotEmpty()) PasswordVisualTransformation() else VisualTransformation.None,
                    singleLine = true,
                )
            }
        }

        // Acciones
        Button(
            onClick = {
                onSave(url, token)
                status = ConnectionStatus.Saved
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Guardar")
        }

        Button(
            onClick = {
                status = ConnectionStatus.Testing
                scope.launch {
                    val ok = onTestConnection(url, token)
                    status = if (ok) ConnectionStatus.Success else ConnectionStatus.Failed
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Default.Link, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Probar conexión")
        }

        status?.let { st ->
            when (st) {
                ConnectionStatus.Testing -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(10.dp))
                    Text("Probando conexión...", style = MaterialTheme.typography.bodyMedium)
                }
                ConnectionStatus.Success -> StatusRow(Color(0xFF2E7D32), "Conexión exitosa ✓")
                ConnectionStatus.Failed -> StatusRow(Color(0xFFB3261E), "No se pudo conectar — revisa URL y token")
                ConnectionStatus.Saved -> StatusRow(MaterialTheme.colorScheme.primary, "Configuración guardada")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Los datos se sincronizan desde tu gateway privado. Guarda las credenciales antes de volver al inicio.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // ── Actualización de la app ─────────────────────────────
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(10.dp))
                    Text("Actualización de la app", style = MaterialTheme.typography.titleMedium)
                }
                updater?.let { up ->
                    when (val s = updateState) {
                        is UpdateUiState.Idle -> Button(
                            onClick = {
                                updateState = UpdateUiState.Checking
                                scope.launch {
                                    val info = up.checkForUpdate()
                                    updateState = if (info == null) {
                                        UpdateUiState.Error("No se pudo comprobar actualizaciones")
                                    } else if (info.isUpdateAvailable) {
                                        UpdateUiState.Available(info)
                                    } else {
                                        UpdateUiState.UpToDate(info.latestVersion)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("Buscar actualizaciones")
                        }
                        is UpdateUiState.Checking -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.size(10.dp))
                            Text("Comprobando...", style = MaterialTheme.typography.bodyMedium)
                        }
                        is UpdateUiState.UpToDate -> StatusRow(Color(0xFF2E7D32), "Tienes la última versión (v${s.version})")
                        is UpdateUiState.Available -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatusRow(MaterialTheme.colorScheme.primary, "Nueva versión disponible: v${s.info.latestVersion}")
                            Text(
                                "Actual: v${s.info.currentVersion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Button(
                                onClick = {
                                    updateState = UpdateUiState.Downloading(s.info)
                                    scope.launch {
                                        val result = up.downloadAndInstall(s.info)
                                        result.onSuccess { apk ->
                                            up.promptInstall(apk)
                                            updateState = UpdateUiState.Idle
                                        }.onFailure {
                                            updateState = UpdateUiState.Error("Descarga fallida: ${it.message}")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(Modifier.size(8.dp))
                                Text("Descargar e instalar v${s.info.latestVersion}")
                            }
                        }
                        is UpdateUiState.Downloading -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.size(10.dp))
                            Text("Descargando v${s.info.latestVersion}...", style = MaterialTheme.typography.bodyMedium)
                        }
                        is UpdateUiState.Error -> StatusRow(Color(0xFFB3261E), s.message)
                    }
                }
            }
        }
    }
}

private sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data class Available(val info: AppUpdater.UpdateInfo) : UpdateUiState
    data class Downloading(val info: AppUpdater.UpdateInfo) : UpdateUiState
    data class UpToDate(val version: String) : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

@Composable
private fun StatusRow(color: Color, message: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

private sealed interface ConnectionStatus {
    data object Saved : ConnectionStatus
    data object Testing : ConnectionStatus
    data object Success : ConnectionStatus
    data object Failed : ConnectionStatus
}
