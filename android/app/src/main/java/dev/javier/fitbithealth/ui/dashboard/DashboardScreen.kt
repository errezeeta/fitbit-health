package dev.javier.fitbithealth.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private data class MetricCard(val title: String, val value: String, val color: Color, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun DashboardScreen(
    state: DashboardState,
    onRetry: () -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        DashboardState.Loading -> Column(
            modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) { CircularProgressIndicator() }
        is DashboardState.Error -> Column(
            modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("No se han podido cargar los datos", style = MaterialTheme.typography.headlineSmall)
            Text(state.message, color = MaterialTheme.colorScheme.error)
            Button(onClick = onRetry) { Text("Reintentar") }
        }
        is DashboardState.Ready -> {
            val dashboard = state.dashboard
            val cards = buildList {
                dashboard.sleep?.let { add(MetricCard("Sueño", "${(it.minutesAsleep ?: 0) / 60}h ${(it.minutesAsleep ?: 0) % 60}m", Color(0xFF6750A4), Icons.Default.Bedtime)) }
                dashboard.restingHeartRate?.let { add(MetricCard("Ritmo en reposo", "$it bpm", Color(0xFFB3261E), Icons.Default.Favorite)) }
                dashboard.hrv?.let { add(MetricCard("HRV", "${it.toInt()} ms", Color(0xFF006A6A), Icons.Default.MonitorHeart)) }
                dashboard.spo2?.let { add(MetricCard("SpO₂", "$it%", Color(0xFF386A20), Icons.Default.FlashOn)) }
                dashboard.steps?.let { add(MetricCard("Pasos", it.toString(), Color(0xFF7D5700), Icons.Default.Timeline)) }
            }
            Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Buenos días", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text("Tu resumen de salud", style = MaterialTheme.typography.headlineMedium)
                    }
                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                        IconButton(onClick = onSync) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sincronizar ahora")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onSync, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Sincronizar ahora")
                }
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(cards) { card ->
                        Card(
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "${card.title}: ${card.value}" },
                            colors = CardDefaults.cardColors(containerColor = card.color.copy(alpha = .10f)),
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(card.icon, contentDescription = null, tint = card.color)
                                Text(card.title, style = MaterialTheme.typography.labelLarge)
                                Text(card.value, style = MaterialTheme.typography.headlineSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
