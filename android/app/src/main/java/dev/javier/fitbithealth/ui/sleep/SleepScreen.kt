package dev.javier.fitbithealth.ui.sleep

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.data.api.SleepSession
import dev.javier.fitbithealth.ui.charts.DonutChart
import dev.javier.fitbithealth.ui.charts.HealthStackedBar
import dev.javier.fitbithealth.ui.charts.LegendDot
import dev.javier.fitbithealth.ui.theme.MetricColors
import dev.javier.fitbithealth.ui.theme.NeoBackground
import dev.javier.fitbithealth.ui.theme.NeoSurface

private val DeepColor = Color(0xFF8B5CF6)
private val LightColor = Color(0xFF4C8DFF)
private val RemColor = Color(0xFFFF4FA3)
private val AwakeColor = Color(0xFF5A6478)

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
                modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Sueño", style = MaterialTheme.typography.headlineMedium)
                if (latest == null) {
                    Text("No hay datos de sueño para este periodo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    SleepDetailCard(latest)
                    state.sessions.drop(1).forEach { session -> SleepSummaryCard(session) }
                }
            }
        }
    }
}

@Composable
private fun SleepDetailCard(session: SleepSession) {
    val deep = (session.deepMinutes ?: 0).toFloat()
    val light = (session.lightMinutes ?: 0).toFloat()
    val rem = (session.remMinutes ?: 0).toFloat()
    val awake = (session.awakeMinutes ?: 0).toFloat()
    val asleep = session.minutesAsleep ?: 0
    val total = deep + light + rem + awake

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
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

            // Donut con fases
            DonutChart(
                segments = listOf(
                    deep to DeepColor,
                    light to LightColor,
                    rem to RemColor,
                    awake to AwakeColor,
                ),
                modifier = Modifier.fillMaxWidth().height(200.dp),
                strokeWidth = 24f,
                centerLabel = "${asleep / 60}h ${asleep % 60}m",
                centerSub = "de sueño",
            )

            // Leyenda en 2x2
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                StageLegend(DeepColor, "Profundo", deep.toInt(), Modifier.weight(1f))
                StageLegend(LightColor, "Ligero", light.toInt(), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                StageLegend(RemColor, "REM", rem.toInt(), Modifier.weight(1f))
                StageLegend(AwakeColor, "Despierto", awake.toInt(), Modifier.weight(1f))
            }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Horario", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${formatTime(session.startTime)} → ${formatTime(session.endTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (total > 0) {
                Text(
                    "Composición: ${(deep / total * 100).toInt()}% profundo · ${(light / total * 100).toInt()}% ligero · ${(rem / total * 100).toInt()}% REM",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StageLegend(color: Color, label: String, minutes: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        LegendDot(color)
        Spacer(Modifier.width(6.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$minutes min", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SleepSummaryCard(session: SleepSession) {
    val asleep = session.minutesAsleep ?: 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${formatTime(session.startTime)} → ${formatTime(session.endTime)}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Profundo ${session.deepMinutes ?: 0} · Ligero ${session.lightMinutes ?: 0} · REM ${session.remMinutes ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("${asleep / 60}h ${asleep % 60}m", style = MaterialTheme.typography.titleLarge, color = MetricColors.Sleep)
        }
    }
}

private fun formatTime(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return runCatching {
        val t = java.time.OffsetDateTime.parse(iso)
        val local = t.atZoneSameInstant(java.time.ZoneId.systemDefault())
        "%02d:%02d".format(local.hour, local.minute)
    }.getOrDefault(iso.substring(11, 16))
}
