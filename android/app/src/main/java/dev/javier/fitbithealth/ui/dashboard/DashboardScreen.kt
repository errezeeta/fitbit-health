package dev.javier.fitbithealth.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    state: DashboardState,
    onRetry: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        DashboardState.Loading -> Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) { CircularProgressIndicator() }
        is DashboardState.Error -> Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
            if (state.canRetry) Button(onClick = onRetry) { Text("Reintentar") }
        }
        is DashboardState.Ready -> {
            val cards = buildList {
                state.dashboard.sleep?.let { add("Sueño" to "${it.minutesAsleep} min") }
                state.dashboard.steps?.let { add("Pasos" to it.toString()) }
                state.dashboard.restingHeartRate?.let { add("RHR" to "$it bpm") }
                state.dashboard.hrv?.let { add("HRV" to "$it ms") }
                state.dashboard.spo2?.let { add("SpO₂" to "$it%") }
                state.dashboard.metrics.forEach { (name, point) ->
                    add(name.uppercase() to (point.value?.toString() ?: "—"))
                }
            }
            Column(modifier = modifier.fillMaxSize()) {
                Button(
                    onClick = onSync,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                ) { Text("Sincronizar ahora") }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(cards) { (title, value) ->
                        Card(modifier = Modifier.semantics { contentDescription = "$title: $value" }) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(title, style = MaterialTheme.typography.labelLarge)
                                Text(value, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
