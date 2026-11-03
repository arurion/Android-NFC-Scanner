package com.nfcreader.universal.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Tinted-glass palette (HSV H≈200-215°, V=100%, S controls depth) ----

/** Sax white: near-white background tint (HSV 210°,10%,100%). */
val SaxWhite = Color(0xFFEAF5FF)

/** Sax blue: the glass tint color (HSV 215°,50%,100%). */
val SaxBlue = Color(0xFF88BBFF)

/** Deeper, more saturated sax blue for the active/current-location state. */
val SaxBlueDeep = Color(0xFF4A86E8)

/** Pink accent, +120° from sax blue, same tone — used sparingly for rare highlights. */
val AccentPink = Color(0xFFFF99DD)

/** Deep navy text (never pure black). */
val TextNavy = Color(0xFF0F172A)
val TextNavyMuted = Color(0xFF47587A)

/** Smoky silver-grey edge stroke (not pure white, avoids a "ギラつく" look). */
val EdgeSilver = Color(0xFF64748B)

/** Status colors, kept in the same low-saturation pastel family as the rest of the UI. */
val StatusGreen = Color(0xFF2E9E6B)
val StatusAmber = Color(0xFFC98A1A)
val StatusRose = Color(0xFFD1527A)
val StatusPurple = Color(0xFF8B6FD6)

// ---- Dark variant: a deep, color-tinted navy (never a washed-out grey), reserved
// for when the system is explicitly in dark mode. Highlights are sharper against it. ----
val NightSurface = Color(0xFF111A2E)
val NightSurfaceElevated = Color(0xFF17213A)
val NightBackground = Color(0xFF0B1220)
val NightGlassTint = Color(0xFF3B6FD4)
val NightText = Color(0xFFE7EEFC)
val NightTextMuted = Color(0xFFA9B8D6)
