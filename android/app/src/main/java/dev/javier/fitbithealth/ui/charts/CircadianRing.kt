package dev.javier.fitbithealth.ui.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ring circadiano — el reloj de 24h de tu cuerpo.
 * Muestra cuándo dormiste (arco ámbar) dentro del día real.
 * El elemento firma de la app: tu ritmo, dibujado como un día.
 */
@Composable
fun CircadianRing(
    sleepStartMinutes: Int?,      // minutos desde 00:00 del inicio del sueño
    sleepEndMinutes: Int?,        // minutos desde 00:00 del final del sueño
    modifier: Modifier = Modifier,
    accent: Color = Color(0xFFD9A962),
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900),
        label = "circadianReveal",
    )
    Canvas(modifier) {
        val strokeW = 18f
        val diameter = size.minDimension - strokeW
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Dial base: 24h con marcas de hora
        val tickColor = Color.White.copy(alpha = 0.12f)
        val hourColor = Color.White.copy(alpha = 0.28f)
        for (h in 0 until 24) {
            val angle = (h / 24f) * 360f - 90f
            val isHour = h % 3 == 0
            val inner = radius - (if (isHour) 10f else 6f)
            val outer = radius - (if (isHour) 2f else 4f)
            val start = Offset(
                center.x + inner * cos(angle * PI / 180f).toFloat(),
                center.y + inner * sin(angle * PI / 180f).toFloat(),
            )
            val end = Offset(
                center.x + outer * cos(angle * PI / 180f).toFloat(),
                center.y + outer * sin(angle * PI / 180f).toFloat(),
            )
            drawLine(if (isHour) hourColor else tickColor, start, end, strokeWidth = 2f)
        }

        // Track suave
        drawCircle(color = Color.White.copy(alpha = 0.05f), radius = radius, center = center, style = Stroke(width = strokeW - 4f))

        // Arco de sueño real (maneja paso de medianoche)
        if (sleepStartMinutes != null && sleepEndMinutes != null && sleepEndMinutes > sleepStartMinutes) {
            val startAngle = -90f + (sleepStartMinutes / 1440f) * 360f
            val sweep = ((sleepEndMinutes - sleepStartMinutes) / 1440f) * 360f * progress
            if (sweep > 0.5f) {
                drawArc(
                    color = accent,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )
            }
        } else if (sleepStartMinutes != null && sleepEndMinutes != null && sleepEndMinutes < sleepStartMinutes) {
            // Sueño que cruza medianoche: dos arcos
            val start1 = -90f + (sleepStartMinutes / 1440f) * 360f
            val sweep1 = ((1440 - sleepStartMinutes) / 1440f) * 360f * progress
            val sweep2 = (sleepEndMinutes / 1440f) * 360f * progress
            if (sweep1 > 0.5f) {
                drawArc(
                    color = accent,
                    startAngle = start1,
                    sweepAngle = sweep1,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )
            }
            if (sweep2 > 0.5f) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = sweep2,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )
            }
        }
    }
}
