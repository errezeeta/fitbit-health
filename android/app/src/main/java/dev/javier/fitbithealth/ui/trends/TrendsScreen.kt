package dev.javier.fitbithealth.ui.trends

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.data.api.TrendPoint
import dev.javier.fitbithealth.ui.charts.HealthLineChart
import dev.javier.fitbithealth.ui.metrics.HealthRange
import dev.javier.fitbithealth.ui.metrics.RangeSelector
import dev.javier.fitbithealth.ui.theme.MetricColors

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
    val latest = points.lastOrNull()?.value
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(meta.label, style = MaterialTheme.typography.titleMedium)
                    Text(meta.unit.ifBlank { "tendencia" }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                HealthLineChart(values, color = meta.color, modifier = Modifier.fillMaxWidth().height(160.dp))
            } else {
                Text("Sin datos suficientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
