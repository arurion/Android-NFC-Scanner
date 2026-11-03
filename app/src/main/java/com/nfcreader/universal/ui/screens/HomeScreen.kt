package com.nfcreader.universal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nfcreader.universal.ui.NfcReaderViewModel
import com.nfcreader.universal.ui.theme.GlassTokens
import com.nfcreader.universal.ui.theme.SaxBlue
import com.nfcreader.universal.ui.theme.SaxBlueDeep
import com.nfcreader.universal.ui.theme.TextNavyMuted
import com.nfcreader.universal.ui.theme.glassChrome

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NfcReaderViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.scanHistory.collectAsState()
    val customKeys by viewModel.customKeys.collectAsState()
    val keyInputError by viewModel.keyInputError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorBanner) {
        uiState.errorBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearErrorBanner()
        }
    }

    // 主要5タブ (下のギチギチ感を完全解消)
    val tabs = listOf(
        NavTabItem("スキャナー", Icons.Default.Nfc, "nav_tab_scanner"),
        NavTabItem("駅・履歴", Icons.Default.Train, "nav_tab_transit"),
        NavTabItem("規格・仕様", Icons.Default.Layers, "nav_tab_specs"),
        NavTabItem("バイナリ", Icons.Default.DataObject, "nav_tab_binary"),
        NavTabItem("履歴", Icons.Default.History, "nav_tab_history")
    )

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = SaxBlueDeep,
        selectedTextColor = SaxBlueDeep,
        indicatorColor = SaxBlue.copy(alpha = GlassTokens.alphaChromeActive),
        unselectedIconColor = TextNavyMuted,
        unselectedTextColor = TextNavyMuted
    )
    val railItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = SaxBlueDeep,
        selectedTextColor = SaxBlueDeep,
        indicatorColor = SaxBlue.copy(alpha = GlassTokens.alphaChromeActive),
        unselectedIconColor = TextNavyMuted,
        unselectedTextColor = TextNavyMuted
    )

    val captionGlow = Shadow(color = Color.White.copy(alpha = 0.85f), blurRadius = 10f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // 最背面に環境光グラデーションレイヤーを敷く (プロトタイプ完全準拠)
            .background(GlassTokens.ambientBackdropBrush)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        // 【右上の⚙️を押したら画面全体で設定＆鍵マネージャーを開く】
        if (uiState.isSettingsOpen) {
            SettingsScreen(
                keys = customKeys,
                inputError = keyInputError,
                onAddKey = { label, hex -> viewModel.addCustomKey(label, hex) },
                onDeleteKey = { viewModel.deleteCustomKey(it) },
                onClearInputError = { viewModel.clearKeyInputError() },
                onClose = { viewModel.closeSettings() }
            )
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Row {
                                Text(
                                    text = "NFC Reader",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge.copy(shadow = captionGlow)
                                )
                                if (uiState.currentTag != null) {
                                    Text(
                                        text = " - ${uiState.currentTag?.standardLabel}",
                                        fontWeight = FontWeight.Normal,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.openSettings() },
                                modifier = Modifier.testTag("open_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "設定",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        modifier = Modifier.glassChrome(
                            tint = SaxBlue,
                            alpha = GlassTokens.alphaChromeActive
                        ),
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            modifier = Modifier.glassChrome(
                                tint = SaxBlue,
                                alpha = GlassTokens.alphaChromeBase
                            ),
                            containerColor = Color.Transparent
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                NavigationBarItem(
                                    selected = uiState.selectedTab == index,
                                    onClick = { viewModel.selectTab(index) },
                                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                                    label = { Text(tab.title) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isWideScreen) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .glassChrome(
                                    tint = SaxBlue,
                                    alpha = GlassTokens.alphaChromeBase
                                ),
                            containerColor = Color.Transparent
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                NavigationRailItem(
                                    selected = uiState.selectedTab == index,
                                    onClick = { viewModel.selectTab(index) },
                                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                                    label = { Text(tab.title) },
                                    colors = railItemColors,
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (uiState.selectedTab) {
                            0 -> ScannerTab(
                                state = uiState,
                                onLoadSuicaDemo = { viewModel.loadDemoSuica() },
                                onLoadMifareDemo = { viewModel.loadDemoMifare() },
                                onLoadNdefDemo = { viewModel.loadDemoNdef() },
                                onNavigateToTransit = { viewModel.selectTab(1) },
                                onNavigateToSpecs = { viewModel.selectTab(2) }
                            )
                            1 -> TransitTab(
                                tag = uiState.currentTag,
                                onLoadSuicaDemo = { viewModel.loadDemoSuica() }
                            )
                            2 -> SpecsTab(
                                tag = uiState.currentTag
                            )
                            3 -> BinaryTab(
                                tag = uiState.currentTag,
                                onLoadMifareDemo = { viewModel.loadDemoMifare() }
                            )
                            4 -> HistoryTab(
                                historyList = historyList,
                                onSelectTag = { viewModel.selectHistoryItem(it) },
                                onDeleteItem = { viewModel.deleteHistoryItem(it) },
                                onClearAll = { viewModel.clearAllHistory() }
                            )
                        }
                    }
                }
            }
        }
    }
}
