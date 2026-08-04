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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Interactive line chart with neon glow, gradient area and touch inspection.
 */
@Composable
fun InteractiveLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4C8DFF),
    glow: Color = color,
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

        // Grid sutil
        val gridColor = Color.White.copy(alpha = 0.04f)
        for (i in 1..3) {
            val y = size.height * i / 4f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }

        // Área con gradiente vertical
        val areaPath = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.30f), color.copy(alpha = 0.0f)),
                startY = 0f,
                endY = size.height,
            ),
        )

        // Glow bajo la línea
        points.zipWithNext().forEach { (start, end) ->
            drawLine(
                color = glow.copy(alpha = 0.35f),
                start = start,
                end = end,
                strokeWidth = 10f,
                cap = StrokeCap.Round,
            )
        }

        // Línea principal con gradiente horizontal
        val lineBrush = Brush.horizontalGradient(
            colors = listOf(color.copy(alpha = 0.6f), color),
        )
        points.zipWithNext().forEachIndexed { i, (start, end) ->
            if (i == 0) {
                drawLine(color = color, start = start, end = end, strokeWidth = 4f, cap = StrokeCap.Round)
            } else {
                drawLine(
                    brush = lineBrush,
                    start = start,
                    end = end,
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                )
            }
        }

        // Punto seleccionado con crosshair
        if (selectedIndex in points.indices) {
            val p = points[selectedIndex]
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(p.x, 0f),
                end = Offset(p.x, size.height),
                strokeWidth = 1.5f,
            )
            drawCircle(color = color.copy(alpha = 0.25f), radius = 16f, center = p)
            drawCircle(color = Color.White, radius = 8f, center = p)
            drawCircle(color = color, radius = 6f, center = p)
        } else {
            drawCircle(color = Color.White, radius = 6f, center = points.last())
            drawCircle(color = color, radius = 4.5f, center = points.last())
        }
    }
}

/** Donut chart con gap entre segmentos — para fases de sueño. */
@Composable
fun DonutChart(
    segments: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 22f,
    centerLabel: String = "",
    centerSub: String = "",
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900),
        label = "donutReveal",
    )
    Canvas(modifier) {
        val total = segments.sumOf { it.first.toDouble() }.toFloat().coerceAtLeast(1f)
        val diameter = size.minDimension - strokeWidth
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val gap = 0.06f // fracción del arco como separación

        var startAngle = -90f
        segments.forEach { (value, color) ->
            val sweep = (value / total) * 360f * progress
            val gapSweep = if (segments.size > 1) gap * (360f / segments.size) else 0f
            if (sweep > gapSweep) {
                drawArc(
                    color = color,
                    startAngle = startAngle + gapSweep / 2f,
                    sweepAngle = (sweep - gapSweep).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            startAngle += sweep
        }

        // Track de fondo
        drawArc(
            color = Color.White.copy(alpha = 0.06f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(diameter, diameter),
            style = Stroke(width = strokeWidth - 4f),
        )

        // Etiqueta central
        if (centerLabel.isNotEmpty()) {
            val labelPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                color = android.graphics.Color.WHITE
                textSize = 32f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            val subPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                color = android.graphics.Color.argb(150, 255, 255, 255)
                textSize = 14f
            }
            drawContext.canvas.nativeCanvas.apply {
                drawText(centerLabel, center.x, center.y + 6f, labelPaint)
                if (centerSub.isNotEmpty()) {
                    drawText(centerSub, center.x, center.y + 30f, subPaint)
                }
            }
        }
    }
}

/** Segmented horizontal bar — composición de fases. */
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
