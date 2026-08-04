package dev.javier.fitbithealth.ui.sleep

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.data.api.SleepSession
import dev.javier.fitbithealth.ui.charts.CircadianRing
import dev.javier.fitbithealth.ui.theme.DataFace
import dev.javier.fitbithealth.ui.theme.MetricColors
import dev.javier.fitbithealth.ui.theme.NeoOnSurfaceMuted
import dev.javier.fitbithealth.ui.theme.NeoOutline
import dev.javier.fitbithealth.ui.theme.NeoSurface

private val DeepColor = Color(0xFF8E8E93)
private val LightColor = Color(0xFFB0B0B5)
private val AwakeColor = Color(0xFF4A4A4F)

@Composable
fun SleepScreen(state: SleepState, modifier: Modifier = Modifier) {
    when (state) {
        SleepState.Loading -> Column(
            modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { CircularProgressIndicator() }
        is SleepState.Error -> Text(
            state.message,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(24.dp),
        )
        is SleepState.Ready -> {
            val latest = state.sessions.firstOrNull()
            Column(
                modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                Text("Sueño", style = MaterialTheme.typography.headlineMedium)
                if (latest == null) {
                    Text("No hay datos de sueño para este periodo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    SleepDetail(latest)
                    state.sessions.drop(1).forEach { session -> SleepSummary(session) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SleepDetail(session: SleepSession) {
    val asleep = session.minutesAsleep ?: 0
    val deep = session.deepMinutes ?: 0
    val light = session.lightMinutes ?: 0
    val rem = session.remMinutes ?: 0
    val awake = session.awakeMinutes ?: 0
    val total = (deep + light + rem + awake).coerceAtLeast(1)
    val accent = MaterialTheme.colorScheme.primary

    val start = parseMinutes(session.startTime)
    val end = parseMinutes(session.endTime)

    // Insight editorial calculado
    val insight = when {
        deep >= 80 -> "Noche profunda: ${deep}m en sueño profundo, el 30% ideal."
        rem >= 90 -> "Buen REM: ${rem}m de sueño soñado."
        asleep >= 420 -> "Noche completa: ${asleep / 60}h ${asleep % 60}m."
        else -> "${asleep / 60}h ${asleep % 60}m de sueño. Descansa más hoy."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = NeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Anoche", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(
                    "${asleep / 60}h ${asleep % 60}m",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MetricColors.Sleep,
                )
            }

            // Ring circadiano: tu noche dentro del día
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircadianRing(
                    sleepStartMinutes = start,
                    sleepEndMinutes = end,
                    modifier = Modifier.fillMaxSize(),
                    accent = accent,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${asleep / 60}h ${asleep % 60}m",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "en el día",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeoOnSurfaceMuted,
                    )
                }
            }

            // Insight editorial
            Text(
                insight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Tabla de fases con lecturas de instrumento
            PhaseRow("Profundo", deep, deep * 100 / total, DeepColor)
            PhaseRow("Ligero", light, light * 100 / total, LightColor)
            PhaseRow("REM", rem, rem * 100 / total, accent)
            PhaseRow("Despierto", awake, awake * 100 / total, AwakeColor)

            HorizontalDivider(color = NeoOutline, thickness = 0.5.dp)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Acostado", style = MaterialTheme.typography.bodySmall, color = NeoOnSurfaceMuted)
                Text("${formatTime(session.startTime)} → ${formatTime(session.endTime)}", style = DataFace.ValueSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun PhaseRow(label: String, minutes: Int, percent: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).padding(0.dp)) {
            Canvas8(color)
        }
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text("$percent%", style = DataFace.ValueSmall, color = NeoOnSurfaceMuted)
        Spacer(Modifier.width(12.dp))
        Text("${minutes}m", style = DataFace.Value, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun Canvas8(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.size(8.dp)) {
        drawCircle(color = color, radius = 4f)
    }
}

@Composable
private fun SleepSummary(session: SleepSession) {
    val asleep = session.minutesAsleep ?: 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = NeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${formatTime(session.startTime)} → ${formatTime(session.endTime)}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "P ${session.deepMinutes ?: 0} · L ${session.lightMinutes ?: 0} · R ${session.remMinutes ?: 0}",
                    style = DataFace.ValueSmall,
                    color = NeoOnSurfaceMuted,
                )
            }
            Text("${asleep / 60}h ${asleep % 60}m", style = MaterialTheme.typography.titleLarge, color = MetricColors.Sleep)
        }
    }
}

private fun parseMinutes(iso: String?): Int? {
    if (iso.isNullOrBlank()) return null
    return runCatching {
        val t = java.time.OffsetDateTime.parse(iso)
        val local = t.atZoneSameInstant(java.time.ZoneId.systemDefault())
        local.hour * 60 + local.minute
    }.getOrNull()
}

private fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return runCatching {
        val t = java.time.OffsetDateTime.parse(iso)
        val local = t.atZoneSameInstant(java.time.ZoneId.systemDefault())
        "%02d:%02d".format(local.hour, local.minute)
    }.getOrDefault(iso.substring(11, 16))
}
