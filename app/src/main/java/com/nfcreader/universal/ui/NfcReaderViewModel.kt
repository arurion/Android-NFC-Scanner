package com.nfcreader.universal.ui

import android.app.Application
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nfcreader.universal.data.DemoCards
import com.nfcreader.universal.data.HexUtils
import com.nfcreader.universal.data.NfcTagReader
import com.nfcreader.universal.data.TagConverter
import com.nfcreader.universal.data.db.AppDatabase
import com.nfcreader.universal.data.db.CustomKeyEntity
import com.nfcreader.universal.data.db.TagScanEntity
import com.nfcreader.universal.model.NfcTagData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NfcHardwareState {
    AVAILABLE_AND_ENABLED,
    DISABLED,
    NOT_SUPPORTED
}

data class NfcUiState(
    val hardwareState: NfcHardwareState = NfcHardwareState.AVAILABLE_AND_ENABLED,
    val isScanning: Boolean = true,
    val currentTag: NfcTagData? = null,
    val selectedTab: Int = 0, // 0: Scanner, 1: Transit, 2: Specs, 3: Binary Dump, 4: History, 5: Keys
    val isLoading: Boolean = false,
    val statusMessage: String = "スマートフォン背面にNFCカードをかざしてください",
    val errorBanner: String? = null
)

sealed class KeyInputError {
    object InvalidLength : KeyInputError()
    object InvalidChars : KeyInputError()
}

class NfcReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.tagScanDao()
    private val keyDao = db.customKeyDao()

    private val _uiState = MutableStateFlow(NfcUiState())
    val uiState: StateFlow<NfcUiState> = _uiState.asStateFlow()

    private val _keyInputError = MutableStateFlow<KeyInputError?>(null)
    val keyInputError: StateFlow<KeyInputError?> = _keyInputError.asStateFlow()

    val scanHistory: StateFlow<List<TagScanEntity>> = dao.getAllScans()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val customKeys: StateFlow<List<CustomKeyEntity>> = keyDao.getAllKeys()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            com.nfcreader.universal.data.StationDatabase.initializeFromAssets(application)
        }
        viewModelScope.launch {
            val defaultDemo = DemoCards.createSuicaDemo()
            _uiState.value = _uiState.value.copy(
                currentTag = defaultDemo,
                statusMessage = "デモ用Suicaデータを読込済み（全国約6,000駅辞書搭載）"
            )
        }
    }

    fun updateNfcStatus(adapter: NfcAdapter?) {
        val hw = when {
            adapter == null -> NfcHardwareState.NOT_SUPPORTED
            !adapter.isEnabled -> NfcHardwareState.DISABLED
            else -> NfcHardwareState.AVAILABLE_AND_ENABLED
        }
        val msg = when (hw) {
            NfcHardwareState.NOT_SUPPORTED -> "本端末はNFC非搭載またはエミュレータです。デモデータ機能でお試しいただけます。"
            NfcHardwareState.DISABLED -> "NFCが無効化されています。端末の設定でNFCを有効にしてください。"
            NfcHardwareState.AVAILABLE_AND_ENABLED -> "NFC待機中：スマートフォン背面にカードをかざしてください"
        }
        _uiState.value = _uiState.value.copy(
            hardwareState = hw,
            statusMessage = msg
        )
    }

    fun onTagDiscovered(tag: Tag, onVibrate: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                statusMessage = "NFCタグを検出しました。バイナリと規格情報を読み込み中..."
            )
            onVibrate()
            try {
                val customKeyBytes = keyDao.getAllKeysOnce().mapNotNull {
                    HexUtils.hexToBytesOrNull(it.keyHex)
                }
                val parsedTag = NfcTagReader.readTag(tag, customKeyBytes)
                val nextTab = if (parsedTag.transitCardInfo != null) 1 else 2

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentTag = parsedTag,
                    selectedTab = nextTab,
                    statusMessage = "読取完了: ${parsedTag.standardLabel} (UID: ${parsedTag.idHex})"
                )

                val entity = TagConverter.toEntity(parsedTag)
                dao.insertScan(entity)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorBanner = "読み取り中にエラーが発生しました: ${e.localizedMessage ?: e.message}",
                    statusMessage = "カードの接触が途切れました。もう一度かざしてください。"
                )
            }
        }
    }

    fun loadDemoSuica() {
        val tag = DemoCards.createSuicaDemo()
        _uiState.value = _uiState.value.copy(
            currentTag = tag,
            selectedTab = 1,
            statusMessage = "Suica (交通系IC) デモデータを読み込みました"
        )
        viewModelScope.launch {
            dao.insertScan(TagConverter.toEntity(tag))
        }
    }

    fun loadDemoMifare() {
        val tag = DemoCards.createMifareClassicDemo()
        _uiState.value = _uiState.value.copy(
            currentTag = tag,
            selectedTab = 3,
            statusMessage = "MIFARE Classic 1K デモデータを読み込みました"
        )
        viewModelScope.launch {
            dao.insertScan(TagConverter.toEntity(tag))
        }
    }

    fun loadDemoNdef() {
        val tag = DemoCards.createNdefDemo()
        _uiState.value = _uiState.value.copy(
            currentTag = tag,
            selectedTab = 2,
            statusMessage = "NDEF Smart Poster / URL デモデータを読み込みました"
        )
        viewModelScope.launch {
            dao.insertScan(TagConverter.toEntity(tag))
        }
    }

    fun selectHistoryItem(entity: TagScanEntity) {
        val tag = TagConverter.toData(entity)
        val tab = if (tag.transitCardInfo != null) 1 else 2
        _uiState.value = _uiState.value.copy(
            currentTag = tag,
            selectedTab = tab,
            statusMessage = "保存履歴から読み込みました: ${tag.standardLabel}"
        )
    }

    fun deleteHistoryItem(entity: TagScanEntity) {
        viewModelScope.launch {
            dao.deleteScan(entity)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun clearErrorBanner() {
        _uiState.value = _uiState.value.copy(errorBanner = null)
    }

    fun addCustomKey(label: String, hexInput: String): Boolean {
        val normalized = HexUtils.normalizeHex(hexInput)
        if (normalized.length != HexUtils.MIFARE_KEY_HEX_LENGTH) {
            _keyInputError.value = KeyInputError.InvalidLength
            return false
        }
        if (!HexUtils.isValidMifareKeyHex(normalized)) {
            _keyInputError.value = KeyInputError.InvalidChars
            return false
        }
        _keyInputError.value = null
        val finalLabel = label.trim().ifBlank { "カスタム鍵" }
        viewModelScope.launch {
            keyDao.insertKey(CustomKeyEntity(label = finalLabel, keyHex = normalized))
        }
        return true
    }

    fun deleteCustomKey(entity: CustomKeyEntity) {
        viewModelScope.launch {
            keyDao.deleteKey(entity)
        }
    }

    fun clearKeyInputError() {
        _keyInputError.value = null
    }
}
