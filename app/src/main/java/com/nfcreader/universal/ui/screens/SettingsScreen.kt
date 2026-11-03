package com.nfcreader.universal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcreader.universal.data.HexUtils
import com.nfcreader.universal.data.StationDatabase
import com.nfcreader.universal.data.db.CustomKeyEntity
import com.nfcreader.universal.ui.KeyInputError
import com.nfcreader.universal.ui.theme.GlassButton
import com.nfcreader.universal.ui.theme.GlassButtonSubtle
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxBlue
import com.nfcreader.universal.ui.theme.SaxBlueDeep
import com.nfcreader.universal.ui.theme.StatusRose
import com.nfcreader.universal.ui.theme.glassChrome

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    keys: List<CustomKeyEntity>,
    inputError: KeyInputError?,
    onAddKey: (label: String, hex: String) -> Boolean,
    onDeleteKey: (CustomKeyEntity) -> Unit,
    onClearInputError: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var label by remember { mutableStateOf("") }
    var hexInput by remember { mutableStateOf("") }
    val normalizedLength = HexUtils.normalizeHex(hexInput).length
    val captionGlow = Shadow(color = Color.White.copy(alpha = 0.85f), blurRadius = 10f)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "設定 & 暗号鍵マネージャー",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge.copy(shadow = captionGlow)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    GlassButtonSubtle(onClick = onClose, modifier = Modifier.padding(end = 8.dp)) {
                        Text("完了", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.glassChrome(
                    tint = SaxBlue,
                    alpha = GlassTokens.alphaChromeActive
                ),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 【登録済み MIFARE 鍵一覧と管理】(画面全体でゆったり確認・削除・追加！)
            item {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    shape = GlassTokens.shape(GlassTokens.radiusLarge),
                    elevated = true
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "登録済み MIFARE 鍵 (${keys.size}件)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "ご自身で把握されているセクタ認証鍵（Key A / B）を登録すると、スキャン時に既定鍵と合わせて自動照合されます。鍵の推測や総当たりは行いません。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (keys.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(GlassTokens.radiusSmall),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "登録済みのカスタム鍵はまだありません（下のフォームから追加できます）",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            keys.forEachIndexed { idx, key ->
                                if (idx > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = GlassTokens.edgeStroke
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
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

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = GlassTokens.edgeStroke)
                        Spacer(modifier = Modifier.height(12.dp))

                        // 新規鍵追加フォーム
                        Text("新しい鍵を追加", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            modifier = Modifier.fillMaxWidth().testTag("key_label_input"),
                            label = { Text("ラベル (例: 自宅の合鍵タグ)") },
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
                            label = { Text("鍵 (16進数 12桁 / 6バイト)") },
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

            // 2. 【データベース情報】
            item {
                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    shape = GlassTokens.shape(GlassTokens.radiusLarge)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = SaxBlueDeep, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
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
