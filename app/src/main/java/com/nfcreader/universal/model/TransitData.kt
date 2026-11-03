package com.nfcreader.universal.model

/**
 * Information extracted from Japanese Transit IC cards (Suica, PASMO, ICOCA, Kitaca, etc.)
 * based on the Cyberne (サイバネ) standard.
 */
data class TransitCardInfo(
    val cardType: String, // Suica, PASMO, ICOCA, TOICA, manaca, SUGOCA, nimoca, はやかけん, Edy, nanaco, WAON, etc.
    val currentBalance: Int,
    val transactions: List<TransitTransaction> = emptyList(),
    val commuterPass: CommuterPassInfo? = null,
    val rawBlocks: List<ByteArray> = emptyList()
)

data class CommuterPassInfo(
    val validStartDate: String,
    val validEndDate: String,
    val fromStation: String,
    val toStation: String,
    val viaDescription: String? = null
)

data class TransitTransaction(
    val id: Int,
    val terminalType: String, // 自動改札機, 券売機, 車載機(バス), 精算機, etc.
    val processType: String,  // 運賃支払, チャージ, 物販, 出場精算, etc.
    val isCharge: Boolean,
    val isShopping: Boolean,
    val isBus: Boolean,
    val dateString: String,   // 2024/05/12
    val timeString: String?,  // 14:30:22 (only for shopping/certain machines)
    val entryLineName: String?,
    val entryStationName: String?,
    val exitLineName: String?,
    val exitStationName: String?,
    val balance: Int,
    val amountChanged: Int?,  // +1000 or -210
    val seqNumber: Long,
    val rawHex: String
)

data class StationInfo(
    val lineCode: Int,
    val stationCode: Int,
    val lineName: String,
    val stationName: String,
    val companyName: String,
    val areaCode: Int = 0
)
