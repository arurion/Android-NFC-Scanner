package com.nfcreader.universal.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassTokens {
    val radiusSmall: Dp = 6.dp
    val radiusMedium: Dp = 10.dp
    val radiusLarge: Dp = 14.dp

    const val alphaChromeActive = 0.48f
    const val alphaChromeInactive = 0.30f
    const val alphaChromeBase = 0.38f

    const val alphaContent = 0.50f
    const val alphaContentSubtle = 0.32f

    val edgeStroke: Color = EdgeSilver.copy(alpha = 0.22f)
    val highlightColor: Color = Color.White.copy(alpha = 0.65f)
    val shadowTint: Color = Color(0xFF0F2850)

    fun shape(radius: Dp = radiusMedium): Shape = RoundedCornerShape(radius)
}

/**
 * 枠(chrome)用モディファイア: ヘッダーやサイドバー、ボトムナビなどの外枠にティントガラスを適用
 */
fun Modifier.glassChrome(
    tint: Color = SaxBlue,
    alpha: Float = GlassTokens.alphaChromeBase
): Modifier = this
    .background(tint.copy(alpha = alpha))
    .background(
        Brush.verticalGradient(
            colors = listOf(GlassTokens.highlightColor, Color.Transparent),
            endY = 3f
        )
    )

/**
 * 統合ガラスパネル: Bento Grid的なカード分割を避け、関連情報を1つのガラス枠にまとめる
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.shape(GlassTokens.radiusMedium),
    tint: Color = MaterialTheme.colorScheme.surface,
    alpha: Float = GlassTokens.alphaContent,
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val base = if (elevated) {
        modifier.shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = GlassTokens.shadowTint.copy(alpha = 0.10f),
            spotColor = GlassTokens.shadowTint.copy(alpha = 0.10f)
        )
    } else modifier

    Column(
        modifier = base
            .clip(shape)
            .background(tint.copy(alpha = alpha))
            .border(1.dp, GlassTokens.edgeStroke, shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(GlassTokens.highlightColor, Color.Transparent),
                    endY = 2f
                )
            )
    ) {
        content()
    }
}
