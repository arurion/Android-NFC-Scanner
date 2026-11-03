package com.nfcreader.universal.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NfcRadarAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    isActive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse2"
    )

    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, delayMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse3"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Canvas(modifier = Modifier.size(size)) {
                val maxRadius = size.toPx() / 2f

                listOf(pulse1, pulse2, pulse3).forEach { progress ->
                    val radius = maxRadius * (0.35f + progress * 0.65f)
                    val alpha = (1f - progress).coerceIn(0f, 1f) * 0.6f
                    drawCircle(
                        color = primaryColor.copy(alpha = alpha),
                        radius = radius,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = alpha * 0.2f),
                        radius = radius
                    )
                }

                // Core inner circle
                drawCircle(
                    color = primaryColor.copy(alpha = 0.25f),
                    radius = maxRadius * 0.35f
                )
            }
        }

        Icon(
            imageVector = Icons.Default.Nfc,
            contentDescription = "NFC Sensor",
            modifier = Modifier.size(size * 0.32f),
            tint = if (isActive) primaryColor else Color.Gray
        )
    }
}
