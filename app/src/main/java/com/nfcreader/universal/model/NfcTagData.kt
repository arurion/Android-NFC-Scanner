package com.nfcreader.universal.model

enum class TagStandard(val displayName: String, val category: String) {
    FELICA_TRANSIT("FeliCa (交通系IC / サイバネ規格)", "JIS 6319-4 / NFC-F"),
    FELICA_GENERAL("FeliCa 一般 / 電子マネー", "JIS 6319-4 / NFC-F"),
    MIFARE_CLASSIC("MIFARE Classic", "ISO/IEC 14443-3A / NXP"),
    MIFARE_ULTRALIGHT("MIFARE Ultralight / NTAG", "ISO/IEC 14443-3A / NXP"),
    ISO_DEP("ISO-DEP (スマートカード/パスポート/マイナンバー)", "ISO/IEC 14443-4"),
    NDEF("NFC Forum NDEF Tag", "NFC Forum"),
    NFC_A("NFC-A (ISO 14443-3A 一般)", "ISO/IEC 14443-3A"),
    NFC_B("NFC-B (ISO 14443-3B 一般)", "ISO/IEC 14443-3B"),
    NFC_V("NFC-V (ISO 15693 近傍型)", "ISO/IEC 15693"),
    UNKNOWN("未知のNFCタグ", "Generic NFC")
}

data class MemoryBlock(
    val index: Int,
    val hex: String,
    val ascii: String,
    val isProtected: Boolean = false,
    val description: String? = null
)

data class NdefParsedRecord(
    val index: Int,
    val tnf: String,
    val type: String,
    val text: String? = null,
    val uri: String? = null,
    val mimeType: String? = null,
    val payloadHex: String
)

data class NfcTagData(
    val idHex: String,
    val standard: TagStandard,
    val standardLabel: String,
    val techList: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val techDetails: Map<String, String> = emptyMap(),
    val transitCardInfo: TransitCardInfo? = null,
    val ndefRecords: List<NdefParsedRecord> = emptyList(),
    val memoryBlocks: List<MemoryBlock> = emptyList(),
    val allHexDump: String = "",
    val scanNotes: String? = null
)
