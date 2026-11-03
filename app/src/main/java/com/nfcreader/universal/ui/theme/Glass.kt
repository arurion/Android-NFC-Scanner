package com.nfcreader.universal.ui.theme

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
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
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class GlassDrawMode(val label: String, val description: String) {
    REAL_REFRACT("リアル屈折ガラス (AGSL / OSシェーダー)", "光の散乱と屈折をリアルタイム演算 (Android 13+推奨)"),
    STANDARD("標準ティントガラス (115°斜め光 + 透明ティント)", "斜め反射光帯とサックスブルーの透明感 (全端末対応)"),
    LITE("軽量・省電力モード", "エフェクトを最小限に抑えたバッテリー優先表示")
}

val LocalGlassDrawMode = compositionLocalOf { GlassDrawMode.STANDARD }

object GlassTokens {
    val radiusSmall: Dp = 6.dp
    val radiusMedium: Dp = 10.dp
    val radiusLarge: Dp = 14.dp

    // alphaは0.3〜0.5に保ち、不透明パネル化を防ぐ
    const val alphaChromeActive = 0.46f
    const val alphaChromeInactive = 0.28f
    const val alphaChromeBase = 0.36f

    const val alphaContent = 0.45f
    const val alphaContentSubtle = 0.28f

    val edgeStroke: Color = EdgeSilver.copy(alpha = 0.26f)
    val highlightColor: Color = Color.White.copy(alpha = 0.70f)
    val shadowTint: Color = Color(0xFF0F2850)

    fun shape(radius: Dp = radiusMedium): Shape = RoundedCornerShape(radius)

    // 115° 斜め反射光帯 (特許 US 7,412,663 由来: 全端末共通)
    val diagonalHighlightBrush = Brush.linearGradient(
        0.0f to Color.Transparent,
        0.20f to Color.Transparent,
        0.32f to Color.White.copy(alpha = 0.18f),
        0.42f to Color.White.copy(alpha = 0.30f),
        0.52f to Color.White.copy(alpha = 0.18f),
        0.65f to Color.Transparent,
        1.0f to Color.Transparent,
        start = Offset(0f, 0f),
        end = Offset(450f, 950f)
    )

    // 画面最背面に透けさせる環境光レイヤー (向こうが透けて見える理由を作る)
    val ambientBackdropBrush = Brush.linearGradient(
        0.0f to Color(0xFFD6EBFF),
        0.4f to SaxWhite,
        0.7f to Color(0xFFE2F0FD),
        1.0f to Color(0xFFCCE4FF),
        start = Offset(0f, 0f),
        end = Offset(600f, 1200f)
    )
}

// Android 13+ (API 33) 向け AGSL 屈折シェーダー
private const val AGSL_REFRACT_SRC = """
    uniform shader content;
    uniform float2 resolution;

    half4 main(float2 coord) {
        float2 uv = coord / resolution;
        float dist = sin(uv.y * 28.0 + uv.x * 18.0) * 0.0035;
        float2 offsetCoord = coord + float2(dist * resolution.x, dist * resolution.y);
        half4 color = content.eval(offsetCoord);
        color.rgb = mix(color.rgb, color.rgb * 1.18, 0.25);
        return color;
    }
"""

fun Modifier.glassChrome(
    tint: Color = SaxBlue,
    alpha: Float = GlassTokens.alphaChromeBase
): Modifier = this
    .background(tint.copy(alpha = alpha))
    .background(GlassTokens.diagonalHighlightBrush)
    .background(
        Brush.verticalGradient(
            colors = listOf(GlassTokens.highlightColor, Color.Transparent),
            endY = 3.5f
        )
    )

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.shape(GlassTokens.radiusMedium),
    tint: Color = MaterialTheme.colorScheme.surface,
    alpha: Float = GlassTokens.alphaContent,
    elevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val mode = LocalGlassDrawMode.current
    val shaderEffect = remember(mode) {
        if (mode == GlassDrawMode.REAL_REFRACT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val shader = RuntimeShader(AGSL_REFRACT_SRC)
                RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
            } catch (_: Exception) {
                null
            }
        } else null
    }

    val shadowModifier = if (elevated) {
        modifier.shadow(
            elevation = 6.dp,
            shape = shape,
            ambientColor = GlassTokens.shadowTint.copy(alpha = 0.12f),
            spotColor = GlassTokens.shadowTint.copy(alpha = 0.12f)
        )
    } else modifier

    val baseModifier = shadowModifier
        .clip(shape)
        .then(
            if (shaderEffect != null) {
                Modifier.graphicsLayer { renderEffect = shaderEffect }
            } else Modifier
        )
        .background(tint.copy(alpha = alpha))
        .then(
            if (mode != GlassDrawMode.LITE) {
                Modifier.background(GlassTokens.diagonalHighlightBrush)
            } else Modifier
        )
        .border(1.dp, GlassTokens.edgeStroke, shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(GlassTokens.highlightColor, Color.Transparent),
                endY = 2.5f
            )
        )

    Column(modifier = baseModifier) {
        content()
    }
}

/**
 * SKILL準拠の立体化ボタン (引き算の清潔感 + 足し算の応答性)
 * ぷっくりとした上端白ハイライトと下端の繊細な影、タップ時の沈み込み応答
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(GlassTokens.radiusSmall),
    tint: Color = SaxBlueDeep,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1.0f, label = "press_scale")
    val alpha by animateFloatAsState(targetValue = if (isPressed) 0.90f else 0.78f, label = "press_alpha")

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = shape,
                ambientColor = GlassTokens.shadowTint.copy(alpha = 0.20f),
                spotColor = GlassTokens.shadowTint.copy(alpha = 0.20f)
            )
            .clip(shape)
            .background(tint.copy(alpha = if (enabled) alpha else 0.35f))
            .border(1.dp, Color.White.copy(alpha = 0.40f), shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isPressed) 0.25f else 0.55f),
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
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}
