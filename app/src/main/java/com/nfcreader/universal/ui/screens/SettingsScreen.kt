package com.nfcreader.universal.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcreader.universal.data.HexUtils
import com.nfcreader.universal.data.StationDatabase
import com.nfcreader.universal.data.db.CustomKeyEntity
import com.nfcreader.universal.ui.KeyInputError
import com.nfcreader.universal.ui.theme.CustomGlassInputField
import com.nfcreader.universal.ui.theme.EdgeSilver
import com.nfcreader.universal.ui.theme.GlassButton
import com.nfcreader.universal.ui.theme.GlassButtonSubtle
import com.nfcreader.universal.ui.theme.GlassDrawMode
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxBlue
import com.nfcreader.universal.ui.theme.SaxBlueDeep
import com.nfcreader.universal.ui.theme.SaxWhite
import com.nfcreader.universal.ui.theme.StatusRose
import com.nfcreader.universal.ui.theme.TextNavy
import com.nfcreader.universal.ui.theme.glassChrome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    keys: List<CustomKeyEntity>,
    inputError: KeyInputError?,
    currentGlassMode: GlassDrawMode,
    onGlassModeChange: (GlassDrawMode) -> Unit,
    onAddKey: (label: String, hex: String) -> Boolean,
    onDeleteKey: (CustomKeyEntity) -> Unit,
    onClearInputError: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var label by remember { mutableStateOf("") }
    var hexInput by remember { mutableStateOf("") }
    val captionGlow = Shadow(color = Color.White.copy(alpha = 0.85f), blurRadius = 10f)

    // 最背面オーバーレイ: サックスブルー(0.45f) + 115°斜め反射光帯 (背面のメイン画面が透けて見える)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SaxBlue.copy(alpha = 0.45f))
            .background(GlassTokens.diagonalHighlightBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "設定 & 暗号鍵マネージャー",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge.copy(shadow = captionGlow)
                        )
                    }
                },
                actions = {
                    GlassButtonSubtle(onClick = onClose, modifier = Modifier.padding(end = 12.dp)) {
                        Text("完了", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.glassChrome(
                    tint = SaxBlue,
                    alpha = GlassTokens.alphaChromeActive
                ),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // セクション窓1: 登録済み鍵マネージャー (サックスホワイト0.48fの独立ティントガラス窓)
                item {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        tint = SaxWhite,
                        alpha = 0.48f,
                        elevated = true
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "登録済み MIFARE 鍵一覧 (${keys.size}件)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "ご自身で把握されているセクタ認証鍵を登録すると自動試行されます。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            if (keys.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(GlassTokens.radiusSmall))
                                        .background(SaxWhite.copy(alpha = 0.75f))
                                        .border(1.dp, EdgeSilver.copy(alpha = 0.3f), RoundedCornerShape(GlassTokens.radiusSmall))
                                        .padding(12.dp)
                                ) {
                                    Text("登録済みのカスタム鍵はありません（下から追加できます）", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                keys.forEachIndexed { idx, key ->
                                    if (idx > 0) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = GlassTokens.edgeStroke)
                                    }
                                    // 読ませる最小単位: .key-box (サックスホワイト0.78f薄塗り + 1pxエッジ)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(GlassTokens.radiusSmall))
                                            .background(SaxWhite.copy(alpha = 0.78f))
                                            .border(1.dp, EdgeSilver.copy(alpha = 0.35f), RoundedCornerShape(GlassTokens.radiusSmall))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(key.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = key.keyHex,
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                color = SaxBlueDeep,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        IconButton(onClick = { onDeleteKey(key) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "削除", tint = StatusRose, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = GlassTokens.edgeStroke)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("新しい鍵を追加 (HEX 12桁 / 6バイト)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            CustomGlassInputField(
                                value = label,
                                onValueChange = { label = it },
                                placeholder = "ラベル (例: 自宅の合鍵タグ)"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            CustomGlassInputField(
                                value = hexInput,
                                onValueChange = {
                                    hexInput = it
                                    if (inputError != null) onClearInputError()
                                },
                                placeholder = "鍵 (例: FFFFFFFFFFFF)",
                                textStyle = TextStyle(fontFamily = FontFamily.Monospace, color = TextNavy, fontSize = 13.sp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            GlassButton(
                                onClick = {
                                    val ok = onAddKey(label, hexInput)
                                    if (ok) {
                                        label = ""
                                        hexInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("add_custom_key_button")
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("この鍵を登録する", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // セクション窓2: ティントガラス描画設定 (サックスホワイト0.48fの独立ティントガラス窓)
                item {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        tint = SaxWhite,
                        alpha = 0.48f,
                        elevated = true
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ティントガラス描画エフェクト設定", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "お使いの端末環境: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            GlassDrawMode.values().forEach { mode ->
                                val isSelected = mode == currentGlassMode
                                val isSupported = mode != GlassDrawMode.REAL_REFRACT || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                                // プロトタイプの行リスト準拠: 選択時はサックスホワイト0.85f+青枠、非選択時は0.35f
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(GlassTokens.radiusSmall))
                                        .background(if (isSelected) SaxWhite.copy(alpha = 0.85f) else SaxWhite.copy(alpha = 0.35f))
                                        .border(
                                            1.dp,
                                            if (isSelected) SaxBlueDeep.copy(alpha = 0.6f) else EdgeSilver.copy(alpha = 0.2f),
                                            RoundedCornerShape(GlassTokens.radiusSmall)
                                        )
                                        .clickable(enabled = isSupported) { onGlassModeChange(mode) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) SaxBlueDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSupported) TextNavy else TextNavy.copy(alpha = 0.45f)
                                        )
                                        Text(
                                            text = if (isSupported) mode.description else "※Android 13 (API 33) 以上で有効化可能",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }

                // セクション窓3: データベース情報
                item {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        tint = SaxWhite,
                        alpha = 0.48f
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("全国サイバネ駅名辞書データベース", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("収録駅数: 約${StationDatabase.getLoadedStationCount()}駅 (サイバネ規格 0x0003 完全準拠)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
