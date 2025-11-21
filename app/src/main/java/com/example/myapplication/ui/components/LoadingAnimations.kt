package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Professional loading animations for BizEng
 */

// ==================== TYPING INDICATOR (Chat/Message Loading) ====================
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = 600
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    val dots = listOf(0, 1, 2)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        dots.forEach { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )

            Canvas(
                modifier = Modifier.size(dotSize)
            ) {
                drawCircle(
                    color = dotColor,
                    radius = size.minDimension / 2 * scale
                )
            }
        }
    }
}

// ==================== CIRCULAR LOADING (General Processing) ====================
@Composable
fun CircularLoading(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(
        modifier = modifier.size(size)
    ) {
        val canvasSize = size.toPx()
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = 280f,
            useCenter = false,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            ),
            size = androidx.compose.ui.geometry.Size(canvasSize, canvasSize)
        )
    }
}

// ==================== PULSING DOTS (Processing) ====================
@Composable
fun PulsingDots(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 800,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha_$index"
            )

            Canvas(
                modifier = Modifier.size(dotSize)
            ) {
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = size.minDimension / 2
                )
            }
        }
    }
}

// ==================== LOADING WITH TEXT ====================
@Composable
fun LoadingWithText(
    text: String,
    modifier: Modifier = Modifier,
    loadingType: LoadingType = LoadingType.TYPING
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (loadingType) {
            LoadingType.TYPING -> TypingIndicator()
            LoadingType.CIRCULAR -> CircularLoading()
            LoadingType.PULSING -> PulsingDots()
        }

        if (text.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

enum class LoadingType {
    TYPING,
    CIRCULAR,
    PULSING
}

