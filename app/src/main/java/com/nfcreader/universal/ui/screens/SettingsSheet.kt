package com.nfcreader.universal.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcreader.universal.data.HexUtils
import com.nfcreader.universal.data.StationDatabase
import com.nfcreader.universal.data.db.CustomKeyEntity
import com.nfcreader.universal.ui.KeyInputError
import com.nfcreader.universal.ui.theme.GlassDrawMode
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxBlueDeep
import com.nfcreader.universal.ui.theme.StatusRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    keys: List<CustomKeyEntity>,
    inputError: KeyInputError?,
    currentGlassMode: GlassDrawMode,
    onGlassModeChange: (GlassDrawMode) -> Unit,
    onAddKey: (label: String, hex: String) -> Boolean,
    onDeleteKey: (CustomKeyEntity) -> Unit,
    onClearInputError: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var label by remember { mutableStateOf("") }
    var hexInput by remember { mutableStateOf("") }
    val normalizedLength = HexUtils.normalizeHex(hexInput).length

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SaxBlueDeep,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "設定・機能管理",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 1. ガラス描画エフェクト設定 (AGSL / 斜め光 / 省電力)
            item {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ティントガラス描画設定",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "お使いの端末環境: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        GlassDrawMode.values().forEach { mode ->
                            val isSelected = mode == currentGlassMode
                            val isSupported = mode != GlassDrawMode.REAL_REFRACT || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isSupported) { onGlassModeChange(mode) }
                                    .padding(vertical = 6.dp),
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
                                        color = if (isSupported) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = if (isSupported) mode.description else "※Android 13 (API 33) 以上の端末で利用可能です",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. MIFARE Classic 任意鍵管理
            item {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MIFARE Classic 認証鍵の登録",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "ご自身で把握されているセクタ認証鍵（Key A / B）を登録すると、スキャン時に既定鍵と合わせて自動試行されます（鍵の推測・総当たりは行いません）。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            modifier = Modifier.fillMaxWidth().testTag("key_label_input"),
                            label = { Text("ラベル（例: 自宅の合鍵タグ）") },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = {
                                hexInput = it
                                if (inputError != null) onClearInputError()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("key_hex_input"),
                            label = { Text("鍵（16進数 12桁 / 6バイト）") },
                            placeholder = { Text("例: FFFFFFFFFFFF") },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                            singleLine = true,
                            isError = inputError != null,
                            supportingText = {
                                val message = when (inputError) {
                                    KeyInputError.InvalidLength -> "鍵は16進数12桁が必要です（現在 $normalizedLength 桁）。"
                                    KeyInputError.InvalidChars -> "0-9, A-F の16進文字を入力してください。"
                                    null -> "区切り記号（: や -）は自動的に無視されます。"
                                }
                                Text(message)
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val success = onAddKey(label, hexInput)
                                if (success) {
                                    label = ""
                                    hexInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("add_custom_key_button")
                        ) {
                            Text("鍵を登録する")
                        }

                        if (keys.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = GlassTokens.edgeStroke)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "登録済みの鍵 (${keys.size}件)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            keys.forEach { key ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(key.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(key.keyHex, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = SaxBlueDeep)
                                    }
                                    IconButton(onClick = { onDeleteKey(key) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "削除", tint = StatusRose, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. データベース統計情報
            item {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("全国駅名辞書データベース", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("収録駅数: 約${StationDatabase.getLoadedStationCount()}駅 (サイバネ規格 0x0003 準拠)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
