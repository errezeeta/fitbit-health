package dev.javier.fitbithealth.ui.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Interactive line chart: touch / drag to inspect values at each point.
 * Exposes [onValueSelected] with (index, value) and [onSelectionCleared].
 */
@Composable
fun InteractiveLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00897B),
    onValueSelected: (Int, Float) -> Unit = { _, _ -> },
    onSelectionCleared: () -> Unit = {},
) {
    if (values.size < 2) {
        Canvas(modifier.fillMaxWidth().height(220.dp)) {}
        return
    }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    fun indexForX(x: Float, width: Float): Int {
        val step = width / (values.size - 1)
        return (x / step).roundToInt().coerceIn(0, values.size - 1)
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(220.dp)
            .pointerInput(values.size) {
                detectTapGestures { offset ->
                    selectedIndex = indexForX(offset.x, size.width.toFloat())
                    onValueSelected(selectedIndex, values[selectedIndex])
                }
            }
            .pointerInput(values.size) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selectedIndex = indexForX(offset.x, size.width.toFloat())
                        onValueSelected(selectedIndex, values[selectedIndex])
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        selectedIndex = indexForX(change.position.x, size.width.toFloat())
                        onValueSelected(selectedIndex, values[selectedIndex])
                    },
                    onDragEnd = { onSelectionCleared(); selectedIndex = -1 },
                    onDragCancel = { onSelectionCleared(); selectedIndex = -1 },
                )
            },
    ) {
        val min = values.minOrNull() ?: return@Canvas
        val maxValue = values.maxOrNull() ?: return@Canvas
        val span = max(maxValue - min, 1f)
        val step = size.width / (values.size - 1)
        val points = values.mapIndexed { index, value ->
            Offset(index * step, size.height - ((value - min) / span * (size.height - 24f)) - 8f)
        }

        // Grid lines (subtle)
        val gridColor = color.copy(alpha = 0.08f)
        for (i in 1..3) {
            val y = size.height * i / 4f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }

        // Area fill
        val areaPath = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.02f)),
                startY = 0f,
                endY = size.height,
            ),
        )

        // Line
        points.zipWithNext().forEach { (start, end) ->
            drawLine(color = color, start = start, end = end, strokeWidth = 4f, cap = StrokeCap.Round)
        }

        // Selected point: crosshair + dot
        if (selectedIndex in points.indices) {
            val p = points[selectedIndex]
            drawLine(
                color = color.copy(alpha = 0.35f),
                start = Offset(p.x, 0f),
                end = Offset(p.x, size.height),
                strokeWidth = 1.5f,
            )
            drawCircle(color = Color.White, radius = 8f, center = p)
            drawCircle(color = color, radius = 6f, center = p)
            drawCircle(color = Color.White, radius = 2.5f, center = p)
        } else {
            drawCircle(color = Color.White, radius = 6f, center = points.last())
            drawCircle(color = color, radius = 4f, center = points.last())
        }
    }
}

/** Segmented horizontal bar used for sleep stage composition. */
@Composable
fun HealthStackedBar(
    segments: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    height: Int = 22,
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "stackedReveal",
    )
    Canvas(modifier.fillMaxWidth().height(height.dp)) {
        if (segments.isEmpty()) return@Canvas
        val total = segments.sumOf { it.first.toDouble() }.toFloat().coerceAtLeast(1f)
        var x = 0f
        segments.forEachIndexed { i, (value, fill) ->
            val w = size.width * (value / total) * progress
            if (w > 0f) {
                drawRoundRect(
                    color = fill,
                    topLeft = Offset(x, 0f),
                    size = Size(w, size.height),
                    cornerRadius = CornerRadius(
                        x = if (i == 0) size.height / 2 else 0f,
                        y = if (i == segments.lastIndex) size.height / 2 else 0f,
                    ),
                )
            }
            x += w
        }
    }
}

/** Small round dot for legends. */
@Composable
fun LegendDot(color: Color) {
    Canvas(Modifier.height(10.dp)) {
        drawCircle(color = color, radius = size.height / 2)
    }
}
