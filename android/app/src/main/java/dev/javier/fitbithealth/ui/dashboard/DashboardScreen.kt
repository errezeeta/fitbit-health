package dev.javier.fitbithealth.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.ui.theme.MetricColors

private data class MetricCardData(
    val key: String,
    val title: String,
    val value: String,
    val subtitle: String,
    val color: Color,
    val icon: ImageVector,
)

@Composable
fun DashboardScreen(
    state: DashboardState,
    onRetry: () -> Unit,
    onSync: () -> Unit,
    onMetricClick: (String) -> Unit = {},
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
        is DashboardState.Ready -> DashboardContent(state, onSync, onMetricClick)
    }
}

@Composable
private fun DashboardContent(state: DashboardState.Ready, onSync: () -> Unit, onMetricClick: (String) -> Unit) {
    val dashboard = state.dashboard
    val cards = buildList {
        dashboard.restingHeartRate?.let {
            add(MetricCardData("rhr", "Ritmo en reposo", "$it", "bpm", MetricColors.HeartRate, Icons.Default.Favorite))
        }
        dashboard.hrv?.let {
            add(MetricCardData("hrv", "HRV", "${it.toInt()}", "ms", MetricColors.HRV, Icons.Default.MonitorHeart))
        }
        dashboard.spo2?.let {
            add(MetricCardData("spo2", "SpO₂", "$it", "%", MetricColors.Spo2, Icons.Default.FlashOn))
        }
        dashboard.steps?.let {
            add(MetricCardData("steps", "Pasos", it.toString(), "hoy", MetricColors.Steps, Icons.Default.Timeline))
        }
        dashboard.sleep?.let {
            add(MetricCardData(
                "sleep",
                "Sueño",
                "${(it.minutesAsleep ?: 0) / 60}h ${(it.minutesAsleep ?: 0) % 60}m",
                "anoche",
                MetricColors.Sleep,
                Icons.Default.Bedtime,
            ))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text(
                    dashboard.date?.let { "Salud · $it" } ?: "Tu salud de hoy",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            IconButton(
                onClick = onSync,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Sincronizar ahora", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Hero de rendimiento — no una card de marketing: datos clave con escala propia
        PerformanceHero(dashboard = dashboard, onSync = onSync)

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(cards) { card ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { it / 4 }),
                ) {
                    MetricCard(card, onClick = { onMetricClick(card.key) })
                }
            }
        }
    }
}

/** Composición monitor: ritmo en reposo grande + SpO₂ y HRV como contexto, sin decoración falsa. */
@Composable
private fun PerformanceHero(
    dashboard: dev.javier.fitbithealth.data.api.DashboardResponse,
    onSync: () -> Unit,
) {
    val rhr = dashboard.restingHeartRate
    val hrv = dashboard.hrv
    val spo2 = dashboard.spo2

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("Ritmo en reposo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            rhr?.toString() ?: "—",
                            style = MaterialTheme.typography.displaySmall,
                            color = MetricColors.HeartRate,
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("bpm", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                    }
                }
                Spacer(Modifier.weight(0.3f))
                MiniStat("HRV", hrv?.let { "${it.toInt()} ms" } ?: "—", MetricColors.HRV)
                Spacer(Modifier.weight(0.3f))
                MiniStat("SpO₂", spo2?.let { "$it%" } ?: "—", MetricColors.Spo2)
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MetricCard(card: MetricCardData, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "${card.title}: ${card.value} ${card.subtitle}" },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(card.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(card.icon, contentDescription = null, tint = card.color, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Ver detalle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(card.value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(card.subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(card.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
