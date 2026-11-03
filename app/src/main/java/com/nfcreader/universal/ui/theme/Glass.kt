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

/**
 * Design tokens for the "tinted glass" look: a light, sax-blue glass chrome with a
 * single fixed light source (highlight at top, shadow below), fixed-radius corners,
 * and alpha kept in the 0.3-0.5 range so the UI never collapses into an opaque panel.
 */
object GlassTokens {
    // Fixed corner radii — never scaled to a panel's size.
    val radiusSmall: Dp = 6.dp
    val radiusMedium: Dp = 12.dp
    val radiusLarge: Dp = 18.dp

    // Glass alpha range (0.3–0.5 per the design language).
    const val alphaChromeActive = 0.50f
    const val alphaChromeInactive = 0.34f
    const val alphaChromeBase = 0.42f

    // Content panels sit closer to white (desaturated), slightly more opaque for legibility
    // since this app has no true backdrop blur — but still kept translucent, never opaque.
    const val alphaContent = 0.55f
    const val alphaContentSubtle = 0.40f

    val edgeStroke: Color = EdgeSilver.copy(alpha = 0.22f)
    val highlightColor: Color = Color.White.copy(alpha = 0.65f)
    val shadowTint: Color = Color(0xFF0F2850)

    fun shape(radius: Dp = radiusMedium): Shape = RoundedCornerShape(radius)
}

/**
 * Applies the glass "chrome" treatment (tinted translucent fill + a single top-edge
 * highlight) to docked framing elements such as the top bar and navigation rail/bar.
 * No drop shadow: chrome is docked, not floating; separation comes from the tint itself.
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
 * A grouped content section, styled as a pane of tinted glass rather than a boxed-in
 * card: a soft tint, a faint edge, and — only when [elevated] is true (meant for the
 * handful of panels that are genuinely meant to stand above the page, e.g. the
 * "currently scanned card" summary) — a shadow proportional to that height.
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
            ambientColor = GlassTokens.shadowTint.copy(alpha = 0.18f),
            spotColor = GlassTokens.shadowTint.copy(alpha = 0.18f)
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
