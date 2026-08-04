package dev.javier.fitbithealth.ui.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.javier.fitbithealth.data.api.SleepSession
import dev.javier.fitbithealth.ui.charts.HealthBarChart

@Composable
fun SleepScreen(state: SleepState, modifier: Modifier = Modifier) {
    when (state) {
        SleepState.Loading -> Column(modifier.fillMaxSize().padding(24.dp)) { CircularProgressIndicator() }
        is SleepState.Error -> Text(
            state.message,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(24.dp),
        )
        is SleepState.Ready -> {
            val latest = state.sessions.firstOrNull()
            Column(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (latest == null) {
                    Text("No hay datos de sueño para este periodo.")
                } else {
                    SleepSummary(latest)
                    HealthBarChart(
                        values = listOf(
                            (latest.deepMinutes ?: 0).toFloat(),
                            (latest.lightMinutes ?: 0).toFloat(),
                            (latest.remMinutes ?: 0).toFloat(),
                            (latest.awakeMinutes ?: 0).toFloat(),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                state.sessions.drop(1).forEach { session -> SleepSummary(session) }
            }
        }
    }
}

@Composable
private fun SleepSummary(session: SleepSession) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sueño", style = MaterialTheme.typography.titleLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(session.startTime ?: "—")
                Text(session.endTime ?: "—")
            }
            Text("Dormido: ${session.minutesAsleep ?: 0} min")
            Text("Despierto: ${session.minutesAwake ?: 0} min")
            Text("Fases: profundo ${session.deepMinutes ?: 0} · ligero ${session.lightMinutes ?: 0} · REM ${session.remMinutes ?: 0}")
        }
    }
}
