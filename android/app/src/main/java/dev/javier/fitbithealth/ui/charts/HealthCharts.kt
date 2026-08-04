package dev.javier.fitbithealth.ui.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * Line chart with gradient area fill, rounded caps and animated reveal.
 * Pure Canvas — no external chart library, no personal data baked in.
 */
@Composable
fun HealthLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00897B),
    showArea: Boolean = true,
    strokeWidth: Float = 4f,
) {
    if (values.size < 2) {
        Canvas(modifier.fillMaxWidth().height(220.dp)) {}
        return
    }
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900),
        label = "chartReveal",
    )

    val visible = values.take(((values.size - 1) * progress).toInt().coerceAtLeast(2))
    Canvas(modifier.fillMaxWidth().height(220.dp)) {
        val min = values.minOrNull() ?: return@Canvas
        val maxValue = values.maxOrNull() ?: return@Canvas
        val span = max(maxValue - min, 1f)
        val step = size.width / (visible.size - 1).coerceAtLeast(1)
        val points = visible.mapIndexed { index, value ->
            Offset(index * step, size.height - ((value - min) / span * (size.height - 24f)) - 8f)
        }

        if (showArea && points.size >= 2) {
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
        }

        // Grid lines (subtle)
        val gridColor = color.copy(alpha = 0.08f)
        for (i in 1..3) {
            val y = size.height * i / 4f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }

        // Baseline
        points.forEachIndexed { i, p ->
            if (i == 0) {
                drawCircle(color = color, radius = strokeWidth * 0.9f, center = p)
            } else {
                drawLine(
                    color = color,
                    start = points[i - 1],
                    end = p,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }

        // End dot
        drawCircle(color = Color.White, radius = strokeWidth + 3f, center = points.last())
        drawCircle(color = color, radius = strokeWidth, center = points.last())
    }
}

/** Rounded horizontal bars (sleep stages, comparisons). */
@Composable
fun HealthBarChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    colors: List<Color> = emptyList(),
    color: Color = Color(0xFF00897B),
    barHeight: Float = 220f,
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 700),
        label = "barReveal",
    )
    Canvas(modifier.fillMaxWidth().height(barHeight.dp)) {
        if (values.isEmpty()) return@Canvas
        val total = values.sum().coerceAtLeast(1f)
        val gap = size.width * 0.02f
        val barW = (size.width - gap * (values.size - 1)) / values.size
        values.forEachIndexed { index, value ->
            val fraction = value / total
            val barH = (size.height * fraction * progress).coerceAtLeast(0f)
            val fill = colors.getOrNull(index) ?: color
            drawRoundRect(
                color = fill,
                topLeft = Offset(index * (barW + gap), size.height - barH),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(barW * 0.35f, barW * 0.35f),
            )
            // Track behind
            drawRoundRect(
                color = fill.copy(alpha = 0.12f),
                topLeft = Offset(index * (barW + gap), 0f),
                size = Size(barW, size.height),
                cornerRadius = CornerRadius(barW * 0.35f, barW * 0.35f),
            )
        }
    }
}

/** Horizontal stacked bar — for sleep stages proportions. */
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

/** Small round "chip" showing a colored dot + label — for legends. */
@Composable
fun LegendDot(color: Color) {
    Canvas(Modifier.height(10.dp)) {
        drawCircle(color = color, radius = size.height / 2)
    }
}
