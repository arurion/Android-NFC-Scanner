package com.nfcreader.universal.ui.screens

import android.os.Build
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcreader.universal.data.HexUtils
import com.nfcreader.universal.data.StationDatabase
import com.nfcreader.universal.data.db.CustomKeyEntity
import com.nfcreader.universal.ui.KeyInputError
import com.nfcreader.universal.ui.theme.GlassButton
import com.nfcreader.universal.ui.theme.GlassDrawMode
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxBlue
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
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        // シート全体を美しい半透明ティントガラスで包む
        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
            tint = SaxBlue.copy(alpha = 0.52f),
            elevated = true
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "設定 & 暗号鍵マネージャー",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        GlassButton(onClick = onDismiss) {
                            Text("完了", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 1. 【登録済みの暗号鍵一覧・管理】(探しやすいよう最上部にしっかり配置！)
                item {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        shape = GlassTokens.shape(GlassTokens.radiusLarge),
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "登録済み MIFARE 鍵一覧 (${keys.size}件)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "登録された鍵はスキャン時に既定鍵と合わせて自動試行されます。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (keys.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(GlassTokens.radiusSmall),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "登録済みのカスタム鍵はありません（下のフォームから追加できます）",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                keys.forEachIndexed { idx, key ->
                                    if (idx > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            color = GlassTokens.edgeStroke
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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
                                            Icon(Icons.Default.Delete, contentDescription = "削除", tint = StatusRose, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = GlassTokens.edgeStroke)
                            Spacer(modifier = Modifier.height(10.dp))

                            // 鍵追加フォーム
                            Text("新しい鍵を登録", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

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
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                singleLine = true,
                                isError = inputError != null,
                                supportingText = {
                                    val msg = when (inputError) {
                                        KeyInputError.InvalidLength -> "16進数12桁が必要です（現在 $normalizedLength 桁）。"
                                        KeyInputError.InvalidChars -> "0-9, A-F の文字を入力してください。"
                                        null -> "区切り記号（: や -）は自動的に無視されます。"
                                    }
                                    Text(msg)
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

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

                // 2. 【ティントガラス描画設定】
                item {
                    GlassPanel(
                        modifier = Modifier.fillMaxWidth(),
                        shape = GlassTokens.shape(GlassTokens.radiusLarge),
                        tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ティントガラス描画エフェクト", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = "端末環境: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            GlassDrawMode.values().forEach { mode ->
                                val isSelected = mode == currentGlassMode
                                val isSupported = mode != GlassDrawMode.REAL_REFRACT || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = isSupported) { onGlassModeChange(mode) }
                                        .padding(vertical = 8.dp),
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
                                            color = if (isSupported) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                        )
                                        Text(
                                            text = if (isSupported) mode.description else "※Android 13 (API 33) 以上で利用可能",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. 【駅名辞書情報】
                item {
                    GlassPanel(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("全国駅名辞書データベース", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("約${StationDatabase.getLoadedStationCount()}駅 収録 (サイバネ規格 0x0003 準拠)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
