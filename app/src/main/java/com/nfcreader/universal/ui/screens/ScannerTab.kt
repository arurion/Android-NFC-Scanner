package com.nfcreader.universal.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfcreader.universal.ui.NfcHardwareState
import com.nfcreader.universal.ui.NfcUiState
import com.nfcreader.universal.ui.components.NfcRadarAnimation
import com.nfcreader.universal.ui.theme.GlassButton
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxBlue
import com.nfcreader.universal.ui.theme.StatusRose

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScannerTab(
    state: NfcUiState,
    onLoadSuicaDemo: () -> Unit,
    onLoadMifareDemo: () -> Unit,
    onLoadNdefDemo: () -> Unit,
    onNavigateToTransit: () -> Unit,
    onNavigateToSpecs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.hardwareState != NfcHardwareState.AVAILABLE_AND_ENABLED) {
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("hardware_warning_card"),
                tint = if (state.hardwareState == NfcHardwareState.DISABLED)
                    StatusRose.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (state.hardwareState == NfcHardwareState.DISABLED) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = "Status",
                        tint = if (state.hardwareState == NfcHardwareState.DISABLED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.hardwareState == NfcHardwareState.DISABLED) "NFC機能が無効です" else "NFC非搭載 / エミュレータ環境",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.hardwareState == NfcHardwareState.DISABLED)
                                "端末の設定からNFCをONにしてください。"
                            else
                                "物理NFCがない環境でも、下の「デモデータ」ですべての機能（駅名解析・バイナリダンプ等）を体験できます。",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (state.hardwareState == NfcHardwareState.DISABLED) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                                } catch (_: Exception) {
                                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                                }
                            },
                            modifier = Modifier.testTag("open_nfc_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("設定")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        NfcRadarAnimation(
            size = 190.dp,
            isActive = state.hardwareState == NfcHardwareState.AVAILABLE_AND_ENABLED
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "NFC タグ読み取り待機中",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = state.statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (state.currentTag != null) {
            val tag = state.currentTag
            GlassPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scanned_tag_summary_card"),
                shape = GlassTokens.shape(GlassTokens.radiusLarge),
                tint = SaxBlue.copy(alpha = 0.45f),
                elevated = true
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(GlassTokens.radiusSmall),
                            color = Color.White.copy(alpha = 0.45f)
                        ) {
                            Text(
                                text = "検出中のカード",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "UID: ${tag.idHex}",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tag.standardLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (tag.transitCardInfo != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "残高: ¥%,d | 履歴: %d件".format(
                                tag.transitCardInfo.currentBalance,
                                tag.transitCardInfo.transactions.size
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (tag.transitCardInfo != null) {
                            GlassButton(
                                onClick = onNavigateToTransit,
                                modifier = Modifier.weight(1f).testTag("view_transit_analysis_button")
                            ) {
                                Icon(Icons.Default.Train, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("駅・履歴解析", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        GlassButton(
                            onClick = onNavigateToSpecs,
                            modifier = Modifier.weight(1f).testTag("view_specs_analysis_button")
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("規格・ダンプ", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // デモカード読み込みエリア
        GlassPanel(
            modifier = Modifier.fillMaxWidth(),
            shape = GlassTokens.shape(GlassTokens.radiusLarge)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "テスト用デモデータ読み込み",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "エミュレータやカードが手元にない時でも、全機能をお試しいただけます",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = onLoadSuicaDemo,
                        modifier = Modifier.weight(1f).testTag("load_demo_suica_button")
                    ) {
                        Icon(Icons.Default.Train, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Suica", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    GlassButton(
                        onClick = onLoadMifareDemo,
                        modifier = Modifier.weight(1f).testTag("load_demo_mifare_button")
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("MIFARE", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    GlassButton(
                        onClick = onLoadNdefDemo,
                        modifier = Modifier.weight(1f).testTag("load_demo_ndef_button")
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("NDEF", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "対応NFC規格・技術一覧",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val supported = listOf(
                "FeliCa (JIS 6319-4)",
                "交通系IC (サイバネ規格)",
                "NFC-A (ISO 14443-3A)",
                "NFC-B (ISO 14443-3B)",
                "NFC-V (ISO 15693)",
                "ISO-DEP (ISO 14443-4)",
                "NFC Forum NDEF (Type 1-5)",
                "MIFARE Classic 1K/4K",
                "MIFARE Ultralight / NTAG",
                "楽天Edy / nanaco / WAON"
            )
            supported.forEach { item ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(item, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}
