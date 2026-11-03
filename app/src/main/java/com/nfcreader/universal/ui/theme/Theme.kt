package com.nfcreader.universal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme: the default. Sax-blue glass on a near-white sax background, per the
// tinted-glass design language. Deliberately not neon/cyberpunk, not a frosted grey.
private val LightColorScheme = lightColorScheme(
    primary = SaxBlueDeep,
    onPrimary = Color.White,
    primaryContainer = SaxBlue.copy(alpha = 0.28f).compositeOverWhite(),
    onPrimaryContainer = Color(0xFF17407A),
    secondary = SaxBlue,
    onSecondary = TextNavy,
    secondaryContainer = SaxWhite,
    onSecondaryContainer = TextNavyMuted,
    tertiary = AccentPink,
    onTertiary = Color.White,
    background = SaxWhite,
    onBackground = TextNavy,
    surface = Color(0xFFF5FAFF),
    onSurface = TextNavy,
    surfaceVariant = Color(0xFFE4F0FF),
    onSurfaceVariant = TextNavyMuted,
    outline = EdgeSilver.copy(alpha = 0.45f),
    error = StatusRose,
    onError = Color.White,
    errorContainer = StatusRose.copy(alpha = 0.16f).compositeOverWhite()
)

// Dark theme: used only when the system is explicitly in dark mode. A deep, color-tinted
// navy (never a washed-out grey) with sharper highlights, staying in the same blue family
// rather than switching to an unrelated neon palette.
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FB8FF),
    onPrimary = Color(0xFF0B1A33),
    primaryContainer = NightSurfaceElevated,
    onPrimaryContainer = Color(0xFFCFE1FF),
    secondary = NightGlassTint,
    onSecondary = Color.White,
    secondaryContainer = NightSurfaceElevated,
    onSecondaryContainer = NightText,
    tertiary = AccentPink,
    onTertiary = Color(0xFF3A0C29),
    background = NightBackground,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = NightSurfaceElevated,
    onSurfaceVariant = NightTextMuted,
    outline = NightTextMuted.copy(alpha = 0.35f),
    error = Color(0xFFFF8FA3),
    onError = Color(0xFF3A0C29)
)

// Fixed-radius shapes — never scaled proportionally to a component's size.
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
fun NfcReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic (Material You / wallpaper-derived) color is intentionally not offered:
    // it would override the curated sax-blue glass palette this app is designed around.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = GlassShapes,
        content = content
    )
}
