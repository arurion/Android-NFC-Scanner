package com.nfcreader.universal.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.nfcreader.universal.data.db.CustomKeyEntity
import com.nfcreader.universal.ui.KeyInputError
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxBlueDeep
import com.nfcreader.universal.ui.theme.StatusRose

@Composable
fun KeysTab(
    keys: List<CustomKeyEntity>,
    inputError: KeyInputError?,
    onAddKey: (label: String, hex: String) -> Boolean,
    onDeleteKey: (CustomKeyEntity) -> Unit,
    onClearInputError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var label by remember { mutableStateOf("") }
    var hexInput by remember { mutableStateOf("") }

    val normalizedLength = HexUtils.normalizeHex(hexInput).length

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "任意の鍵でMIFARE Classicを読む",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "セクタ認証に使う鍵（Key A / Key B）をご自身で登録できます。登録した鍵は、既定の公開鍵と合わせて次回のスキャンから自動的に試行されます。鍵を知らないカードの解析（鍵の推測・総当たり）は行いません。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = SaxBlueDeep,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "鍵を追加",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("key_label_input"),
                        label = { Text("ラベル（任意・例: 自宅の合鍵タグ）") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = {
                            hexInput = it
                            if (inputError != null) onClearInputError()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("key_hex_input"),
                        label = { Text("鍵（16進数 12桁 / 6バイト）") },
                        placeholder = { Text("例: FFFFFFFFFFFF") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true,
                        isError = inputError != null,
                        supportingText = {
                            val message = when (inputError) {
                                KeyInputError.InvalidLength ->
                                    "鍵は16進数で12桁（6バイト）である必要があります（現在 $normalizedLength 桁）。"
                                KeyInputError.InvalidChars ->
                                    "0-9, A-F の16進数文字だけを入力してください。"
                                null -> "区切り記号（: や -）は自動的に無視されます。"
                            }
                            Text(
                                text = message,
                                color = if (inputError != null) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val success = onAddKey(label, hexInput)
                            if (success) {
                                label = ""
                                hexInput = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_custom_key_button")
                    ) {
                        Text("この鍵を登録する")
                    }
                }
            }
        }

        item {
            Text(
                text = "登録済みの鍵 (${keys.size}件)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (keys.isEmpty()) {
            item {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "登録済みの鍵はまだありません",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                        keys.forEachIndexed { index, key ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = GlassTokens.edgeStroke
                                )
                            }
                            KeyRow(key = key, onDelete = { onDeleteKey(key) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyRow(key: CustomKeyEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("custom_key_row_${key.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = key.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = key.keyHex,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = SaxBlueDeep
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_custom_key_${key.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "鍵を削除",
                tint = StatusRose
            )
        }
    }
}
