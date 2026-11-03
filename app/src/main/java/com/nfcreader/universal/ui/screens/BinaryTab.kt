package com.nfcreader.universal.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcreader.universal.model.NfcTagData
import com.nfcreader.universal.ui.components.HexDumpViewer
import com.nfcreader.universal.ui.theme.GlassButton
import com.nfcreader.universal.ui.theme.GlassButtonSubtle
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxWhite

@Composable
fun BinaryTab(
    tag: NfcTagData?,
    onLoadMifareDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (tag == null || tag.memoryBlocks.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            GlassPanel(shape = GlassTokens.shape(GlassTokens.radiusLarge), tint = SaxWhite) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "バイナリメモリブロック未取得", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "現在選択中のタグには読み取り可能なメモリブロックがないか、暗号化キーによるアクセス保護が適用されています。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(20.dp))
                    GlassButton(onClick = onLoadMifareDemo, modifier = Modifier.testTag("load_mifare_demo_from_binary_button")) {
                        Text("MIFARE Classic デモを読み込む", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        GlassPanel(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), tint = SaxWhite) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "UID: ${tag.idHex}", style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(text = "${tag.standardLabel} | ${tag.memoryBlocks.size} ブロック", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                GlassButtonSubtle(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "NFC Hex Dump (${tag.idHex})")
                            putExtra(Intent.EXTRA_TEXT, tag.allHexDump)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "バイナリダンプを共有"))
                    },
                    modifier = Modifier.testTag("share_hex_dump_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("共有", fontWeight = FontWeight.Bold)
                }
            }
        }

        HexDumpViewer(blocks = tag.memoryBlocks, fullDumpText = tag.allHexDump, modifier = Modifier.weight(1f))
    }
}
