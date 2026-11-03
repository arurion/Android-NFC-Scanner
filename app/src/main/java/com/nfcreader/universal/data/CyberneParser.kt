package com.nfcreader.universal.data

import com.nfcreader.universal.model.TransitCardInfo
import com.nfcreader.universal.model.TransitTransaction
import java.util.Locale

object CyberneParser {

    private val terminalNames = mapOf(
        0x03 to "精算機",
        0x04 to "携帯端末",
        0x05 to "車載機 (バス/路面電車)",
        0x07 to "POS端末 (物販)",
        0x08 to "券売機",
        0x09 to "入金機",
        0x12 to "駅務機器",
        0x14 to "定期券発売機",
        0x15 to "定期券発売機",
        0x16 to "自動改札機",
        0x17 to "簡易改札機",
        0x18 to "窓口端末",
        0x19 to "窓口端末",
        0x1A to "改札窓口",
        0x1C to "乗継精算機",
        0x1D to "連絡改札機",
        0x1F to "簡易入金機",
        0x46 to "VIEW ALTTE (ATM)",
        0x48 to "ポイントチャージ機"
    )

    private val processNames = mapOf(
        0x01 to "運賃支払 (乗車)",
        0x02 to "チャージ (入金)",
        0x03 to "乗継精算",
        0x04 to "精算",
        0x05 to "入場",
        0x06 to "窓口精算",
        0x07 to "新規発行",
        0x08 to "控除",
        0x0D to "バス等均一運賃",
        0x0F to "バス等運賃",
        0x11 to "再発行",
        0x13 to "支払 (物販)",
        0x14 to "オートチャージ",
        0x1F to "バスチャージ",
        0x46 to "物販取消 (返金)",
        0x48 to "ポイントチャージ"
    )

    fun parseCyberneHistory(rawBlocks: List<ByteArray>, systemCode: Int = 0x0003): TransitCardInfo {
        val parsedList = mutableListOf<TransitTransaction>()

        for ((index, block) in rawBlocks.withIndex()) {
            if (block.size < 16) continue
            // Check if block is all zeros (empty/unwritten block)
            if (block.all { it == 0.toByte() }) continue

            val terminalCode = block[0].toInt() and 0xFF
            val processCode = block[1].toInt() and 0xFF

            val terminalType = terminalNames[terminalCode] ?: "端末 [0x%02X]".format(terminalCode)
            val processType = processNames[processCode] ?: "処理 [0x%02X]".format(processCode)

            val isCharge = processCode in listOf(0x02, 0x14, 0x1F, 0x48)
            val isShopping = processCode in listOf(0x13, 0x46) || terminalCode == 0x07
            val isBus = processCode in listOf(0x0D, 0x0F) || terminalCode == 0x05

            // Date bytes: 4 and 5
            val dateRaw = ((block[4].toInt() and 0xFF) shl 8) or (block[5].toInt() and 0xFF)
            val year = 2000 + ((dateRaw ushr 9) and 0x7F)
            val month = (dateRaw ushr 5) and 0x0F
            val day = dateRaw and 0x1F
            val dateString = String.format(Locale.JAPAN, "%04d/%02d/%02d", year, month, day)

            // Time if shopping
            var timeString: String? = null
            if (isShopping) {
                val timeRaw = ((block[6].toInt() and 0xFF) shl 8) or (block[7].toInt() and 0xFF)
                val hour = (timeRaw ushr 11) and 0x1F
                val min = (timeRaw ushr 5) and 0x3F
                val sec = (timeRaw and 0x1F) * 2
                timeString = String.format(Locale.JAPAN, "%02d:%02d:%02d", hour, min, sec)
            }

            // Stations
            var entryLineName: String? = null
            var entryStationName: String? = null
            var exitLineName: String? = null
            var exitStationName: String? = null

            if (!isShopping && !isBus) {
                val entryLine = block[6].toInt() and 0xFF
                val entryStation = block[7].toInt() and 0xFF
                val exitLine = block[8].toInt() and 0xFF
                val exitStation = block[9].toInt() and 0xFF

                if (entryLine != 0 || entryStation != 0) {
                    val entryInfo = StationDatabase.resolveStation(entryLine, entryStation)
                    entryLineName = entryInfo.lineName
                    entryStationName = entryInfo.stationName
                }

                if (exitLine != 0 || exitStation != 0) {
                    val exitInfo = StationDatabase.resolveStation(exitLine, exitStation)
                    exitLineName = exitInfo.lineName
                    exitStationName = exitInfo.stationName
                }
            } else if (isBus) {
                val busLineCode = ((block[6].toInt() and 0xFF) shl 8) or (block[7].toInt() and 0xFF)
                val busStopCode = ((block[8].toInt() and 0xFF) shl 8) or (block[9].toInt() and 0xFF)
                entryLineName = "バス事業者 [0x%04X]".format(busLineCode)
                entryStationName = "停留所 [0x%04X]".format(busStopCode)
            }

            // Balance bytes: 10 and 11 (Little Endian!)
            val balance = ((block[11].toInt() and 0xFF) shl 8) or (block[10].toInt() and 0xFF)

            // Sequence number: bytes 12, 13, 14
            val seqNumber = (((block[12].toLong() and 0xFF) shl 16) or
                    ((block[13].toLong() and 0xFF) shl 8) or
                    (block[14].toLong() and 0xFF))

            val hexString = block.joinToString(" ") { "%02X".format(it) }

            parsedList.add(
                TransitTransaction(
                    id = index,
                    terminalType = terminalType,
                    processType = processType,
                    isCharge = isCharge,
                    isShopping = isShopping,
                    isBus = isBus,
                    dateString = dateString,
                    timeString = timeString,
                    entryLineName = entryLineName,
                    entryStationName = entryStationName,
                    exitLineName = exitLineName,
                    exitStationName = exitStationName,
                    balance = balance,
                    amountChanged = null, // Will calculate relative to adjacent record
                    seqNumber = seqNumber,
                    rawHex = hexString
                )
            )
        }

        // Calculate amount changed by comparing successive transactions
        // Note: Cyberne history blocks are stored newest first (index 0 is newest)
        for (i in 0 until parsedList.size) {
            val curr = parsedList[i]
            val prev = if (i + 1 < parsedList.size) parsedList[i + 1] else null
            val diff = if (prev != null) curr.balance - prev.balance else null
            parsedList[i] = curr.copy(amountChanged = diff)
        }

        val cardName = when (systemCode) {
            0x0003 -> "交通系IC (Suica / PASMO / ICOCA / etc.)"
            0xfe00 -> "楽天Edy"
            0x86a7 -> "nanaco"
            0x8008 -> "WAON"
            0x88b4 -> "OKICA (沖縄)"
            else -> "FeliCa サイバネ準拠カード"
        }

        val currentBalance = parsedList.firstOrNull()?.balance ?: 0

        return TransitCardInfo(
            cardType = cardName,
            currentBalance = currentBalance,
            transactions = parsedList,
            rawBlocks = rawBlocks
        )
    }
}
