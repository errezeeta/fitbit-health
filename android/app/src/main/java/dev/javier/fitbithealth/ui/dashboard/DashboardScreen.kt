package dev.javier.fitbithealth.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.ui.charts.InteractiveLineChart
import dev.javier.fitbithealth.ui.theme.MetricColors
import dev.javier.fitbithealth.ui.theme.NeoSurface
import dev.javier.fitbithealth.ui.theme.NeoSurfaceVariant

@Composable
fun DashboardScreen(
    state: DashboardState,
    onRetry: () -> Unit,
    onSync: () -> Unit,
    onMetricClick: (String) -> Unit = {},
    heartRateValues: List<Float> = emptyList(),
    modifier: Modifier = Modifier,
) {
    when (state) {
        DashboardState.Loading -> Column(
            modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }
        is DashboardState.Error -> Column(
            modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("No se han podido cargar los datos", style = MaterialTheme.typography.headlineSmall)
            Text(state.message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
        is DashboardState.Ready -> DashboardContent(state, onSync, onMetricClick, heartRateValues)
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState.Ready,
    onSync: () -> Unit,
    onMetricClick: (String) -> Unit,
    heartRateValues: List<Float>,
) {
    val dashboard = state.dashboard
    val stats = buildList {
        dashboard.hrv?.let { add("HRV" to "${it.toInt()} ms") }
        dashboard.spo2?.let { add("SpO₂" to "$it%") }
        dashboard.steps?.let { add("Pasos" to it.toString()) }
        dashboard.sleep?.let { add("Sueño" to "${(it.minutesAsleep ?: 0) / 60}h ${(it.minutesAsleep ?: 0) % 60}m") }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            dashboard.date?.let { "Salud · $it" } ?: "Tu salud de hoy",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    IconButton(
                        onClick = onSync,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sincronizar ahora", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.height(24.dp))

                // ── Protagonista: ritmo en reposo ──
                Text(
                    "Ritmo en reposo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        dashboard.restingHeartRate?.toString() ?: "—",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "bpm",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── Gráfico protagonista: HR del día ──
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NeoSurface)
                    .padding(16.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ritmo cardíaco de hoy", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(
                            heartRateStats(heartRateValues),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    if (heartRateValues.size >= 2) {
                        InteractiveLineChart(
                            values = heartRateValues,
                            color = MetricColors.HeartRate,
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                        )
                    } else {
                        Text(
                            "Sin datos de HR para hoy",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Stats compactos: fila monocroma, sin iconos ──
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                stats.forEach { (label, value) ->
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeoSurfaceVariant)
                            .padding(14.dp),
                    ) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun heartRateStats(values: List<Float>): String {
    if (values.isEmpty()) return ""
    val min = values.minOrNull()?.toInt()
    val max = values.maxOrNull()?.toInt()
    return if (min != null && max != null) "$min–$max bpm" else ""
}
