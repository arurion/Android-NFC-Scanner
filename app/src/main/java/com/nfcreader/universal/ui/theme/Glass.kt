package com.nfcreader.universal.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassTokens {
    val radiusSmall: Dp = 4.dp
    val radiusMedium: Dp = 6.dp
    val radiusLarge: Dp = 10.dp

    // alphaは0.3〜0.5に保ち、不透明パネル化を防ぐ
    const val alphaChromeActive = 0.52f
    const val alphaChromeInactive = 0.32f
    const val alphaChromeBase = 0.42f

    const val alphaContent = 0.46f
    const val alphaContentSubtle = 0.30f

    val edgeStroke: Color = EdgeSilver.copy(alpha = 0.24f)
    val highlightColor: Color = Color.White.copy(alpha = 0.70f)
    val shadowTint: Color = Color(0xFF0F2850)

    fun shape(radius: Dp = radiusMedium): Shape = RoundedCornerShape(radius)

    // 115° 斜め反射光帯 (特許 US 7,412,663 由来: 全端末共通)
    val diagonalHighlightBrush = Brush.linearGradient(
        0.0f to Color.Transparent,
        0.20f to Color.Transparent,
        0.32f to Color.White.copy(alpha = 0.16f),
        0.42f to Color.White.copy(alpha = 0.26f),
        0.52f to Color.White.copy(alpha = 0.16f),
        0.65f to Color.Transparent,
        1.0f to Color.Transparent,
        start = Offset(0f, 0f),
        end = Offset(450f, 950f)
    )

    // 画面最背面に透けさせる環境光レイヤー (HTMLプロトタイプ完全準拠)
    val ambientBackdropBrush = Brush.linearGradient(
        0.0f to Color(0xFFD8EAFC),
        0.4f to SaxWhite,
        0.7f to Color(0xFFE0EFFD),
        1.0f to Color(0xFFC8E0F8),
        start = Offset(0f, 0f),
        end = Offset(600f, 1200f)
    )
}

/**
 * 枠(Chrome)用: サックスブルーティント + 115°斜め反射光 + 上端1px白ハイライト
 */
fun Modifier.glassChrome(
    tint: Color = SaxBlue,
    alpha: Float = GlassTokens.alphaChromeBase
): Modifier = this
    .background(tint.copy(alpha = alpha))
    .background(GlassTokens.diagonalHighlightBrush)
    .background(
        Brush.verticalGradient(
            colors = listOf(GlassTokens.highlightColor, Color.Transparent),
            endY = 3f
        )
    )

/**
 * 統合ガラスパネル: 本文は白に近いサックスホワイト。余計な反射光は載せず視認性を死守
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
    val shadowModifier = if (elevated) {
        modifier.shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = GlassTokens.shadowTint.copy(alpha = 0.10f),
            spotColor = GlassTokens.shadowTint.copy(alpha = 0.10f)
        )
    } else modifier

    Column(
        modifier = shadowModifier
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

/**
 * SKILL準拠: ぷっくり立体化プライマリボタン (足し算の応答性)
 * ゲーム機のような張りのあるハイライト＋深いブルーグラデーション
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(GlassTokens.radiusSmall),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1.0f, label = "press_scale")

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4B8FFA),
            Color(0xFF2563EB),
            Color(0xFF1D4ED8)
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = shape,
                ambientColor = GlassTokens.shadowTint.copy(alpha = 0.18f),
                spotColor = GlassTokens.shadowTint.copy(alpha = 0.18f)
            )
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, Color.White.copy(alpha = 0.55f), shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isPressed) 0.3f else 0.75f),
                        Color.Transparent
                    ),
                    endY = 2.5f
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * サックスブルー系のぷっくり立体化サブボタン
 */
@Composable
fun GlassButtonSubtle(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(GlassTokens.radiusSmall),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1.0f, label = "press_scale")

    val bgBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFAFDFF),
            Color(0xFFDCEBFF)
        )
    )

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 44.dp, minHeight = 38.dp)
            .shadow(
                elevation = if (isPressed) 1.dp else 2.dp,
                shape = shape,
                ambientColor = GlassTokens.shadowTint.copy(alpha = 0.08f),
                spotColor = GlassTokens.shadowTint.copy(alpha = 0.08f)
            )
            .clip(shape)
            .background(bgBrush)
            .border(1.dp, GlassTokens.edgeStroke, shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    endY = 2f
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
