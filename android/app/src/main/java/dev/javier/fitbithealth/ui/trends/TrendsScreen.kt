package dev.javier.fitbithealth.ui.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.data.api.TrendPoint
import dev.javier.fitbithealth.ui.charts.InteractiveLineChart
import dev.javier.fitbithealth.ui.metrics.HealthRange
import dev.javier.fitbithealth.ui.metrics.MetricInfoMap
import dev.javier.fitbithealth.ui.metrics.RangeSelector
import dev.javier.fitbithealth.ui.theme.MetricColors
import dev.javier.fitbithealth.ui.theme.NeoSurface

private data class TrendMeta(val label: String, val color: Color, val unit: String)

private val TrendNames: Map<String, TrendMeta> = mapOf(
    "rhr" to TrendMeta("Ritmo en reposo", MetricColors.HeartRate, "bpm"),
    "hrv" to TrendMeta("HRV", MetricColors.HRV, "ms"),
    "spo2" to TrendMeta("SpO₂", MetricColors.Spo2, "%"),
    "steps" to TrendMeta("Pasos", MetricColors.Steps, ""),
)

@Composable
fun TrendsScreen(
    state: TrendsState,
    selectedRange: HealthRange,
    onRangeSelected: (HealthRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(selectedRange) {
        onRangeSelected(selectedRange)
    }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Tendencias", style = MaterialTheme.typography.headlineMedium)
        RangeSelector(selectedRange, onRangeSelected)
        when (state) {
            TrendsState.Loading -> Column(
                Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }
            is TrendsState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is TrendsState.Ready -> state.trends.forEach { (metric, points) ->
                TrendCard(metric, points)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TrendCard(metric: String, points: List<TrendPoint>) {
    val meta = TrendNames[metric] ?: TrendMeta(metric, Color(0xFF00897B), "")
    val values = points.mapNotNull { it.value }.map { it.toFloat() }
    val dates = points.mapNotNull { it.date }
    val latest = points.lastOrNull()?.value
    var selectedValue by remember { mutableStateOf<String?>(null) }
    var showInfo by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = NeoSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(meta.label, style = MaterialTheme.typography.titleMedium)
                    Text(meta.unit.ifBlank { "tendencia" }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showInfo = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Info sobre ${meta.label}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (latest != null) {
                    Text(
                        "${latest} ${meta.unit}",
                        style = MaterialTheme.typography.titleLarge,
                        color = meta.color,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (values.size >= 2) {
                InteractiveLineChart(
                    values = values,
                    color = meta.color,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    onValueSelected = { index, value ->
                        selectedValue = if (index < dates.size) {
                            "${dates[index]} · ${value} ${meta.unit}"
                        } else {
                            "${value} ${meta.unit}"
                        }
                    },
                    onSelectionCleared = { selectedValue = null },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    selectedValue ?: "Mantén el dedo en la gráfica para ver valores",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selectedValue != null) FontWeight.SemiBold else FontWeight.Normal,
                )
            } else {
                Text("Sin datos suficientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showInfo) {
        val info = MetricInfoMap[metric]
        if (info != null) {
            AlertDialog(
                onDismissRequest = { showInfo = false },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                title = { Text(info.title) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(info.short, fontWeight = FontWeight.Medium)
                        Text(info.explanation)
                        Text("Rango habitual: ${info.normalRange}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfo = false }) { Text("Entendido") }
                },
            )
        }
    }
}
