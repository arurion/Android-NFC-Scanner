package com.nfcreader.universal.ui.theme

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    REAL_REFRACT("リアル屈折ガラス (AGSL / OSシェーダー)", "光の屈折と散乱をリアルタイム演算 (Android 13+推奨)"),
    STANDARD("標準ティントガラス (斜め反射光 + 半透明)", "115°斜め反射光帯とサックスブルーの透明感 (全端末対応)"),
    LITE("軽量・省電力モード", "エフェクトを最小限に抑えたバッテリー優先表示")
}

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

    // 115° 斜め反射ハイライト帯 (特許 US 7,412,663 由来: 全Androidバージョン対応)
    val diagonalHighlightBrush = Brush.linearGradient(
        0.0f to Color.Transparent,
        0.25f to Color.Transparent,
        0.35f to Color.White.copy(alpha = 0.16f),
        0.45f to Color.White.copy(alpha = 0.24f),
        0.55f to Color.White.copy(alpha = 0.16f),
        0.65f to Color.Transparent,
        1.0f to Color.Transparent,
        start = Offset(0f, 0f),
        end = Offset(450f, 950f)
    )
}

// Android 13+ (API 33) 向け AGSL 屈折シェーダーコード
private const val AGSL_REFRACT_SRC = """
    uniform shader content;
    uniform float2 resolution;

    half4 main(float2 coord) {
        float2 uv = coord / resolution;
        // わずかな法線変位による光の屈折 (右下方向へ数px)
        float dist = sin(uv.y * 30.0 + uv.x * 20.0) * 0.003;
        float2 offsetCoord = coord + float2(dist * resolution.x, dist * resolution.y);
        half4 color = content.eval(offsetCoord);
        // ティントの彩度ブースト
        color.rgb = mix(color.rgb, color.rgb * 1.15, 0.3);
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
            endY = 3f
        )
    )

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = GlassTokens.shape(GlassTokens.radiusMedium),
    tint: Color = MaterialTheme.colorScheme.surface,
    alpha: Float = GlassTokens.alphaContent,
    elevated: Boolean = false,
    mode: GlassDrawMode = GlassDrawMode.STANDARD,
    content: @Composable ColumnScope.() -> Unit
) {
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
            ambientColor = GlassTokens.shadowTint.copy(alpha = 0.10f),
            spotColor = GlassTokens.shadowTint.copy(alpha = 0.10f)
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
                endY = 2f
            )
        )

    Column(modifier = baseModifier) {
        content()
    }
}
