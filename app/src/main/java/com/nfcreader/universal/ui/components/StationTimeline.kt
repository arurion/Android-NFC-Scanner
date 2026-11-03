package com.nfcreader.universal.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import com.nfcreader.universal.data.StationDatabase
import com.nfcreader.universal.model.StationInfo
import com.nfcreader.universal.model.TransitCardInfo
import com.nfcreader.universal.model.TransitTransaction
import com.nfcreader.universal.ui.theme.GlassButton
import com.nfcreader.universal.ui.theme.GlassPanel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxBlue
import com.nfcreader.universal.ui.theme.SaxBlueDeep
import com.nfcreader.universal.ui.theme.StatusAmber
import com.nfcreader.universal.ui.theme.StatusGreen
import com.nfcreader.universal.ui.theme.StatusPurple
import com.nfcreader.universal.ui.theme.StatusRose
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationTimeline(
    transitInfo: TransitCardInfo,
    modifier: Modifier = Modifier
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale.JAPAN) }
    var showStationDictSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Hero Transit Summary Panel (フルガラス・サックスブルーティント)
        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("transit_card_hero"),
            shape = GlassTokens.shape(GlassTokens.radiusLarge),
            tint = SaxBlue.copy(alpha = 0.50f),
            elevated = true
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = "IC Card",
                                tint = SaxBlueDeep,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = transitInfo.cardType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(GlassTokens.radiusSmall),
                        color = Color.White.copy(alpha = 0.40f)
                    ) {
                        Text(
                            text = "履歴 ${transitInfo.transactions.size}件",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaxBlueDeep,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "カード残高",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatter.format(transitInfo.currentBalance),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                if (transitInfo.commuterPass != null) {
                    val cp = transitInfo.commuterPass
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = GlassTokens.edgeStroke)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().testTag("commuter_pass_card"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = "定期券",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "定期券区間: ${cp.fromStation} ⇔ ${cp.toStation}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "有効期間: ${cp.validStartDate} 〜 ${cp.validEndDate}${if (cp.viaDescription != null) " (${cp.viaDescription})" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section header with GlassButton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "利用・乗降・精算明細",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // ぷっくり立体化 GlassButton
            GlassButton(
                onClick = { showStationDictSheet = true },
                modifier = Modifier.testTag("open_station_dictionary_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "全国駅辞書 (${StationDatabase.getLoadedStationCount()}駅)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (transitInfo.transactions.isEmpty()) {
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "このカードには利用履歴が記録されていないか、暗号化領域です",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            GlassPanel(
                modifier = Modifier.fillMaxWidth(),
                shape = GlassTokens.shape(GlassTokens.radiusLarge)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(transitInfo.transactions, key = { _, tx -> "${tx.id}_${tx.seqNumber}" }) { idx, tx ->
                        if (idx > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = GlassTokens.edgeStroke
                            )
                        }
                        TransitTransactionRow(tx = tx, formatter = formatter)
                    }
                }
            }
        }
    }

    if (showStationDictSheet) {
        StationDictionarySheet(onDismiss = { showStationDictSheet = false })
    }
}

@Composable
fun TransitTransactionRow(
    tx: TransitTransaction,
    formatter: NumberFormat
) {
    var isExpanded by remember { mutableStateOf(false) }

    val icon = when {
        tx.isShopping -> Icons.Default.ShoppingBag
        tx.isCharge -> Icons.Default.AddCircle
        tx.isBus -> Icons.Default.DirectionsBus
        tx.terminalType.contains("ATM") -> Icons.Default.LocalAtm
        else -> Icons.Default.Train
    }

    val iconColor = when {
        tx.isCharge -> StatusGreen
        tx.isShopping -> StatusAmber
        tx.isBus -> StatusPurple
        else -> SaxBlueDeep
    }

    val isTrainRide = !tx.isShopping && !tx.isBus && !tx.isCharge &&
            !tx.entryStationName.isNullOrBlank() && !tx.exitStationName.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("transaction_item_${tx.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(iconColor.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tx.processType,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.processType,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tx.terminalType,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.dateString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (tx.timeString != null) {
                            Text(
                                text = " ${tx.timeString}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatter.format(tx.balance),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (tx.amountChanged != null) {
                    val isPositive = tx.amountChanged > 0
                    Text(
                        text = if (isPositive) "+${formatter.format(tx.amountChanged)}" else formatter.format(tx.amountChanged),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) StatusGreen else StatusRose
                    )
                }
            }
        }

        if (isTrainRide) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(GlassTokens.radiusSmall)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.entryStationName!!,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (tx.entryLineName != null) {
                        Text(
                            text = tx.entryLineName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "to",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp).size(16.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tx.exitStationName!!,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (tx.exitLineName != null) {
                        Text(
                            text = tx.exitLineName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (tx.isCharge || !tx.entryStationName.isNullOrBlank()) {
            val stationLabel = tx.entryStationName ?: tx.exitStationName
            val lineLabel = tx.entryLineName ?: tx.exitLineName
            if (!stationLabel.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White.copy(alpha = 0.40f),
                            shape = RoundedCornerShape(GlassTokens.radiusSmall)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (tx.isCharge) "チャージ取扱: $stationLabel" else "利用場所: $stationLabel",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (lineLabel != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($lineLabel)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else if (tx.isBus && !tx.entryLineName.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.White.copy(alpha = 0.40f),
                        shape = RoundedCornerShape(GlassTokens.radiusSmall)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = StatusPurple,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${tx.entryLineName} / ${tx.entryStationName ?: "停留所"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "連番 #%d".format(tx.seqNumber),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isExpanded) "生データ非表示" else "16byte 生データ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(GlassTokens.radiusSmall)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = tx.rawHex,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDictionarySheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }

    val results = remember(query) {
        StationDatabase.searchStations(query, limit = 50)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        GlassPanel(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
            tint = SaxBlue.copy(alpha = 0.52f),
            elevated = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "全国サイバネ駅コード辞書",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    GlassButton(onClick = onDismiss) {
                        Text("閉じる", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "約6,000駅の線区コード・駅順コードを収録しています",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("station_dict_search_input"),
                    placeholder = { Text("駅名、路線名、会社名 (例: 新宿, 山手)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "検索結果: ${results.size}件",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                GlassPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    tint = Color.White.copy(alpha = 0.60f)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(results) { idx, item ->
                            if (idx > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    color = GlassTokens.edgeStroke
                                )
                            }
                            StationDictItemRow(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StationDictItemRow(item: StationInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.stationName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(GlassTokens.radiusSmall),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = item.companyName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = item.lineName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "線 [0x%02X]".format(item.lineCode),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "駅 [0x%02X]".format(item.stationCode),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
