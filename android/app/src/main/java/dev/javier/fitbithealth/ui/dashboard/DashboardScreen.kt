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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.data.api.MetricPoint
import dev.javier.fitbithealth.ui.charts.InteractiveLineChart
import dev.javier.fitbithealth.ui.theme.DataFace
import dev.javier.fitbithealth.ui.theme.NeoOnSurfaceMuted
import dev.javier.fitbithealth.ui.theme.NeoOutline
import dev.javier.fitbithealth.ui.theme.NeoSurface

@Composable
fun DashboardScreen(
    state: DashboardState,
    onRetry: () -> Unit,
    onSync: () -> Unit,
    onMetricClick: (String) -> Unit = {},
    heartRatePoints: List<MetricPoint> = emptyList(),
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
        is DashboardState.Ready -> DashboardContent(state, onSync, onMetricClick, heartRatePoints)
    }
}

@Composable
private fun DashboardContent(
    state: DashboardState.Ready,
    onSync: () -> Unit,
    onMetricClick: (String) -> Unit,
    heartRatePoints: List<MetricPoint>,
) {
    val dashboard = state.dashboard
    val rhr = dashboard.restingHeartRate
    val heartRateValues = heartRatePoints.mapNotNull { it.value?.toFloat() }
    val stats = buildList {
        dashboard.hrv?.let { add("HRV" to "${it.toInt()} ms") }
        dashboard.spo2?.let { add("SpO₂" to "$it%") }
        dashboard.steps?.let { add("Pasos" to it.toString()) }
    }

    // Punto seleccionado al tocar el gráfico
    var selectedPoint by remember { mutableStateOf<MetricPoint?>(null) }

    Box(Modifier.fillMaxSize()) {
        // Fondo ambiental tipo landing Hermes: glow azul difuso arriba
        Box(
            Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            Color.Transparent,
                        )
                    )
                )
        )

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
                Spacer(Modifier.height(6.dp))
                // Insight editorial calculado
                Text(
                    rhrInsight(rhr, heartRateValues),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeoOnSurfaceMuted,
                )
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().height(170.dp),
                        onValueSelected = { index, _ ->
                            selectedPoint = heartRatePoints.getOrNull(index)
                        },
                        onSelectionCleared = { selectedPoint = null },
                    )
                } else {
                    Text(
                        "Sin datos de HR para hoy",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeoOnSurfaceMuted,
                    )
                }
                Spacer(Modifier.height(10.dp))
                // Detalle del punto tocado
                selectedPoint?.let { point ->
                    HrPointDetail(point, heartRatePoints)
                    Spacer(Modifier.height(4.dp))
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
}

@Composable
private fun StatRow(label: String, value: String) {
    HorizontalDivider(color = NeoOutline, thickness = 0.5.dp)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = NeoOnSurfaceMuted, modifier = Modifier.weight(1f))
        Text(value, style = DataFace.Value, color = MaterialTheme.colorScheme.onSurface)
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
            .background(MaterialTheme.colorScheme.primary),
    )
}

private fun heartRateStats(values: List<Float>): String {
    if (values.isEmpty()) return ""
    val min = values.minOrNull()?.toInt()
    val max = values.maxOrNull()?.toInt()
    return if (min != null && max != null) "$min – $max bpm" else ""
}

/** Detalle enriquecido del punto tocado: hora, valor, delta y contexto del día. */
@Composable
private fun HrPointDetail(point: MetricPoint, all: List<MetricPoint>) {
    val value = point.value?.toInt() ?: 0
    val idx = all.indexOfFirst { it.timestamp == point.timestamp }
    val prev = if (idx > 0) all[idx - 1].value?.toInt() else null
    val delta = if (prev != null) value - prev else null
    val dayMin = all.mapNotNull { it.value?.toInt() }.minOrNull()
    val dayMax = all.mapNotNull { it.value?.toInt() }.maxOrNull()

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeoSurface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                point.timestamp.take(16).replace('T', ' '),
                style = MaterialTheme.typography.labelSmall,
                color = NeoOnSurfaceMuted,
                modifier = Modifier.weight(1f),
            )
            Text(
                "$value bpm",
                style = DataFace.Value,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            delta?.let {
                Text(
                    if (it >= 0) "▲ +$it vs anterior" else "▼ $it vs anterior",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (it >= 0) Color(0xFF4CAF50) else Color(0xFFFF6B5E),
                )
            }
            if (dayMin != null && dayMax != null) {
                Text(
                    "Día: $dayMin–$dayMax",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeoOnSurfaceMuted,
                )
            }
        }
    }
}

/** Insight editorial: una frase calculada con los datos reales del día. */
private fun rhrInsight(rhr: Int?, values: List<Float>): String {
    val resting = rhr ?: return "Sin lectura de reposo hoy."
    val low = values.minOrNull()?.toInt()
    val high = values.maxOrNull()?.toInt()
    val text = when {
        resting < 60 -> "Por debajo de 60 bpm: ritmo de atleta."
        resting in 60..75 -> "Dentro del rango saludable (60–75 bpm)."
        resting in 76..90 -> "Un poco elevado (76–90 bpm). ¿Estrés o falta de sueño?"
        else -> "Elevado (>90 bpm). Consulta a un profesional."
    }
    return if (low != null && high != null) "$text Rango del día: $low–$high bpm." else text
}
