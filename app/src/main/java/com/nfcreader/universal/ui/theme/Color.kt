package com.nfcreader.universal.ui.theme

import androidx.compose.ui.graphics.Color

// ---- Tinted-glass palette (HSV H≈200-215°, V=100%, S controls depth) ----

/** サックスホワイト: ほぼ白に近い淡い水色の背景基準色 (HSV 210°, 10%, 100%) */
val SaxWhite = Color(0xFFEAF5FF)

/** サックスブルー: ガラスのティント色 (HSV 215°, 50%, 100%) */
val SaxBlue = Color(0xFF88BBFF)

/** 現在地・選択中を示す、彩度を少し高めたスカイブルー */
val SaxBlueDeep = Color(0xFF2563EB)

/** 差し色: 色相+120°の淡いピンク (ごく少量だけ使用) */
val AccentPink = Color(0xFFFF99DD)

/** 文字色: 純黒ではなく深いネイビー (コントラスト比4.5:1以上を確保) */
val TextNavy = Color(0xFF0F172A)
val TextNavyMuted = Color(0xFF47587A)

/** 縁取り: ギラつかないスモーキーなシルバーグレー (#64748B系) */
val EdgeSilver = Color(0xFF64748B)

/** 状態色: トーンを揃えた落ち着いたパステル */
val StatusGreen = Color(0xFF10B981)
val StatusAmber = Color(0xFFF59E0B)
val StatusRose = Color(0xFFE11D48)
val StatusPurple = Color(0xFF8B5CF6)
