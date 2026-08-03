package dev.javier.fitbithealth.ui.trends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.ui.charts.HealthLineChart
import dev.javier.fitbithealth.ui.metrics.HealthRange
import dev.javier.fitbithealth.ui.metrics.RangeSelector

@Composable
fun TrendsScreen(
    state: TrendsState,
    selectedRange: HealthRange,
    onRangeSelected: (HealthRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Tendencias", style = MaterialTheme.typography.headlineSmall)
        RangeSelector(selectedRange, onRangeSelected)
        when (state) {
            TrendsState.Loading -> CircularProgressIndicator()
            is TrendsState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
            is TrendsState.Ready -> state.trends.forEach { (metric, points) ->
                Text(metric.uppercase(), style = MaterialTheme.typography.titleMedium)
                HealthLineChart(points.mapNotNull { it.value }.map { it.toFloat() })
            }
        }
    }
}
