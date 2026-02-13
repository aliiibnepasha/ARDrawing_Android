package com.example.ardrawing.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WaterWaveBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "sea_wave")

    // Horizontal flow (kept smooth)
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // 🌊 SURGE (fast up, slow down)
    val surge by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 6000

                // Fast wave arrival
                0f at 0 using FastOutLinearInEasing
                1f at 1800 using FastOutLinearInEasing

                // Slow retreat
                0.85f at 3800 using LinearOutSlowInEasing
                0f at 6000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "surge"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FBFE))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            val width = size.width
            val height = size.height

            // 🌊 Water level starts from top (small offset)
            val baseWaterLevel = height * 0.02f - surge * 20f

            drawSeaWave(
                width = width,
                height = height,
                baseY = baseWaterLevel + 30f,
                amplitude = 34f + surge * 18f,
                frequency = 1.1f,
                phase = phase * 0.6f,
                color = Color(0xFFF3F8FC)
            )

            drawSeaWave(
                width = width,
                height = height,
                baseY = baseWaterLevel + 14f,
                amplitude = 26f + surge * 14f,
                frequency = 1.45f,
                phase = phase,
                color = Color(0xFFEEF4FA)
            )

            drawSeaWave(
                width = width,
                height = height,
                baseY = baseWaterLevel,
                amplitude = 18f + surge * 10f,
                frequency = 1.8f,
                phase = phase * 1.3f,
                color = Color(0xFFE8F1F8)
            )
        }
    }
}

private fun DrawScope.drawSeaWave(
    width: Float,
    height: Float,
    baseY: Float,
    amplitude: Float,
    frequency: Float,
    phase: Float,
    color: Color
) {
    val path = Path()
    path.moveTo(0f, height)
    path.lineTo(0f, baseY)

    val step = 5f
    var x = 0f

    while (x <= width) {
        val progress = x / width
        val edgeFade = sin(progress * PI).toFloat()
        val y = (baseY +
            sin(progress * frequency * 2 * PI + phase) *
            amplitude *
            edgeFade).toFloat()

        path.lineTo(x, y)
        x += step
    }

    path.lineTo(width, height)
    path.close()

    drawPath(path, color)
}
