package dev.javier.fitbithealth.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max

/** Lightweight chart primitives with no personal data or web dependencies. */
@Composable
fun HealthLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6750A4),
) {
    Canvas(modifier.fillMaxWidth().height(220.dp)) {
        if (values.size < 2) return@Canvas
        val min = values.minOrNull() ?: return@Canvas
        val maxValue = values.maxOrNull() ?: return@Canvas
        val span = max(maxValue - min, 1f)
        val step = size.width / (values.size - 1)
        val points = values.mapIndexed { index, value ->
            Offset(index * step, size.height - ((value - min) / span * size.height))
        }
        points.zipWithNext().forEach { (start, end) ->
            drawLine(color = color, start = start, end = end, strokeWidth = 5f)
        }
    }
}

@Composable
fun HealthBarChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6750A4),
) {
    Canvas(modifier.fillMaxWidth().height(220.dp)) {
        if (values.isEmpty()) return@Canvas
        val maxValue = max(values.maxOrNull() ?: 1f, 1f)
        val width = size.width / values.size
        values.forEachIndexed { index, value ->
            val barHeight = value / maxValue * size.height
            drawRect(
                color = color,
                topLeft = Offset(index * width + width * .15f, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(width * .7f, barHeight),
            )
        }
    }
}
