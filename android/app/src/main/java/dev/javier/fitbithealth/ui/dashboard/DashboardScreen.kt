package dev.javier.fitbithealth.ui.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.ui.charts.InteractiveLineChart
import dev.javier.fitbithealth.ui.theme.MetricColors
import dev.javier.fitbithealth.ui.theme.NeoOnSurfaceMuted
import dev.javier.fitbithealth.ui.theme.NeoOutline
import dev.javier.fitbithealth.ui.theme.NeoSurface

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
    val rhr = dashboard.restingHeartRate
    val stats = buildList {
        dashboard.hrv?.let { add("HRV" to "${it.toInt()} ms") }
        dashboard.spo2?.let { add("SpO₂" to "$it%") }
        dashboard.steps?.let { add("Pasos" to it.toString()) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        dashboard.date?.let { "Salud · $it" } ?: "Tu salud de hoy",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeoOnSurfaceMuted,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = onSync,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Sincronizar ahora",
                            tint = NeoOnSurfaceMuted,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                // ── Hero: RHR con pulso ──
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        rhr?.toString() ?: "—",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(12.dp))
                    PulseDot()
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "bpm en reposo",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeoOnSurfaceMuted,
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }

        // ── Gráfico protagonista: la forma de tu día ──
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Ritmo cardíaco · hoy",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeoOnSurfaceMuted,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        heartRateStats(heartRateValues),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeoOnSurfaceMuted,
                    )
                }
                Spacer(Modifier.height(10.dp))
                if (heartRateValues.size >= 2) {
                    InteractiveLineChart(
                        values = heartRateValues,
                        color = MetricColors.HeartRate,
                        modifier = Modifier.fillMaxWidth().height(170.dp),
                    )
                } else {
                    Text(
                        "Sin datos de HR para hoy",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoOnSurfaceMuted,
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Tabla de stats con hairlines (sin cajas) ──
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                dashboard.sleep?.let { sleep ->
                    StatRow("Sueño", "${(sleep.minutesAsleep ?: 0) / 60}h ${(sleep.minutesAsleep ?: 0) % 60}m")
                }
                stats.forEach { (label, value) ->
                    StatRow(label, value)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    HorizontalDivider(color = NeoOutline, thickness = 0.5.dp)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = NeoOnSurfaceMuted, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Signature: el punto de pulso — el latido de la app. */
@Composable
private fun PulseDot() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    Box(
        Modifier
            .size((14 * scale).dp)
            .clip(CircleShape)
            .background(MetricColors.HeartRate),
    )
}

private fun heartRateStats(values: List<Float>): String {
    if (values.isEmpty()) return ""
    val min = values.minOrNull()?.toInt()
    val max = values.maxOrNull()?.toInt()
    return if (min != null && max != null) "$min – $max bpm" else ""
}
