package com.nfcreader.universal.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nfcreader.universal.model.NfcTagData
import com.nfcreader.universal.ui.components.StationTimeline
import com.nfcreader.universal.ui.theme.GlassButton
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxWhite

@Composable
fun TransitTab(
    tag: NfcTagData?,
    onLoadSuicaDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (tag?.transitCardInfo != null) {
            StationTimeline(transitInfo = tag.transitCardInfo)
        } else {
            GlassPanel(modifier = Modifier.fillMaxWidth().align(Alignment.Center).testTag("no_transit_card_card"), shape = GlassTokens.shape(GlassTokens.radiusLarge), tint = SaxWhite) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(imageVector = Icons.Default.Train, contentDescription = "Transit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "交通系ICカード未検出", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Suica、PASMO等の交通系ICカードを背面にタッチしてください。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    GlassButton(onClick = onLoadSuicaDemo, modifier = Modifier.testTag("transit_empty_load_demo_button")) {
                        Text("Suica デモデータを読み込む", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
