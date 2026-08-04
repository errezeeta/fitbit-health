package dev.javier.fitbithealth.ui.metricdetail

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.data.api.HealthApi
import dev.javier.fitbithealth.ui.charts.InteractiveLineChart
import dev.javier.fitbithealth.ui.metrics.MetricInfoMap
import dev.javier.fitbithealth.ui.metrics.metricInfo
import dev.javier.fitbithealth.ui.theme.MetricColors

@Composable
fun MetricDetailScreen(
    metric: String,
    api: HealthApi?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        MetricDetailViewModel(api)
    }
    val state by viewModel.state.collectAsState()
    val info = MetricInfoMap[metric] ?: metricInfo(metric)
    val color = when (metric) {
        "sleep" -> MetricColors.Sleep
        "rhr" -> MetricColors.HeartRate
        "hrv" -> MetricColors.HRV
        "spo2" -> MetricColors.Spo2
        "steps" -> MetricColors.Steps
        "breathing" -> MetricColors.Breathing
        else -> MetricColors.HRV
    }
    val unit = when (metric) {
        "rhr" -> "bpm"
        "hrv" -> "ms"
        "spo2" -> "%"
        "sleep" -> "min"
        "steps" -> ""
        "breathing" -> "rpm"
        else -> ""
    }

    LaunchedEffect(metric) {
        if (state is MetricDetailState.Idle) viewModel.load(metric)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.weight(1f))
            Text(info.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f))
        }

        // Tarjeta de info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(info.short, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Info, contentDescription = null, tint = color)
                }
                Text(info.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Rango habitual: ${info.normalRange}", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Medium)
            }
        }

        // Gráfica de tendencia
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Tendencia (30 días)", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                when (val s = state) {
                    is MetricDetailState.Loading -> CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    is MetricDetailState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    is MetricDetailState.Ready -> {
                        val values = s.points.mapNotNull { it.value }.map { it.toFloat() }
                        val dates = s.points.mapNotNull { it.date }
                        if (values.size >= 2) {
                            var selected by remember { mutableStateOf<String?>(null) }
                            InteractiveLineChart(
                                values = values,
                                color = color,
                                modifier = Modifier.fillMaxWidth().height(180.dp),
                                onValueSelected = { index, value ->
                                    selected = if (index < dates.size) {
                                        "${dates[index]} · ${value} ${unit}"
                                    } else {
                                        "${value} ${unit}"
                                    }
                                },
                                onSelectionCleared = { selected = null },
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                selected ?: "Mantén el dedo para explorar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected != null) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        } else {
                            Text("Sin datos suficientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
