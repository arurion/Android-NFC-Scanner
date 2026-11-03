package com.nfcreader.universal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TintedGlassColorScheme = lightColorScheme(
    primary = SaxBlueDeep,
    onPrimary = Color.White,
    primaryContainer = SaxBlue.copy(alpha = 0.25f).compositeOverWhite(),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = SaxBlue,
    onSecondary = TextNavy,
    secondaryContainer = SaxWhite,
    onSecondaryContainer = TextNavyMuted,
    tertiary = AccentPink,
    onTertiary = Color.White,
    background = SaxWhite,
    onBackground = TextNavy,
    surface = Color(0xFFF8FAFD),
    onSurface = TextNavy,
    surfaceVariant = Color(0xFFE8F1FA),
    onSurfaceVariant = TextNavyMuted,
    outline = EdgeSilver.copy(alpha = 0.35f),
    error = StatusRose,
    onError = Color.White,
    errorContainer = StatusRose.copy(alpha = 0.14f).compositeOverWhite()
)

private val GlassShapes = Shapes(
    extraSmall = RoundedCornerShape(GlassTokens.radiusSmall),
    small = RoundedCornerShape(GlassTokens.radiusSmall),
    medium = RoundedCornerShape(GlassTokens.radiusMedium),
    large = RoundedCornerShape(GlassTokens.radiusLarge),
    extraLarge = RoundedCornerShape(GlassTokens.radiusLarge)
)

private fun Color.compositeOverWhite(): Color {
    val a = this.alpha
    return Color(
        red = this.red * a + 1f * (1f - a),
        green = this.green * a + 1f * (1f - a),
        blue = this.blue * a + 1f * (1f - a),
        alpha = 1f
    )
}

@Composable
fun NfcReaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TintedGlassColorScheme,
        typography = Typography,
        shapes = GlassShapes,
        content = content
    )
}
