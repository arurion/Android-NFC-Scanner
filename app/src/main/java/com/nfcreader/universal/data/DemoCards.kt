package com.nfcreader.universal.data

import com.nfcreader.universal.model.MemoryBlock
import com.nfcreader.universal.model.NdefParsedRecord
import com.nfcreader.universal.model.NfcTagData
import com.nfcreader.universal.model.TagStandard

object DemoCards {

    fun createSuicaDemo(): NfcTagData {
        // Construct realistic Cyberne raw 16-byte blocks
        // Record 0 (Newest): Ride Shibuya (0x01, 0x0B) -> Yokohama (0x03, 0x05) Fare 410, Balance 1,540, Date 2024/05/10
        val b0 = makeCyberneBlock(
            terminal = 0x16, // 自動改札機
            process = 0x01,  // 運賃支払
            year = 2024, month = 5, day = 10,
            entryLine = 0x01, entryStation = 0x0B, // JR東日本 渋谷
            exitLine = 0x03, exitStation = 0x05,   // JR東日本 横浜
            balance = 1540,
            seq = 2045
        )

        // Record 1: Shopping at 7-Eleven, -350, Balance 1,950, Date 2024/05/10 08:30:14
        val b1 = makeCyberneBlockShopping(
            terminal = 0x07, // POS端末 (物販)
            process = 0x13,  // 支払 (物販)
            year = 2024, month = 5, day = 10,
            hour = 8, min = 30, sec = 14,
            balance = 1950,
            seq = 2044
        )

        // Record 2: Ride Tokyo (0x01, 0x01) -> Shinjuku (0x02, 0x0A) Fare 210, Balance 2,300, Date 2024/05/09
        val b2 = makeCyberneBlock(
            terminal = 0x16, // 自動改札機
            process = 0x01,  // 運賃支払
            year = 2024, month = 5, day = 9,
            entryLine = 0x01, entryStation = 0x01, // JR東日本 東京
            exitLine = 0x02, exitStation = 0x0A,   // JR東日本 新宿
            balance = 2300,
            seq = 2043
        )

        // Record 3: Charge +2,000 at Ticket Vending Machine, Balance 2,510, Date 2024/05/08
        val b3 = makeCyberneBlock(
            terminal = 0x08, // 券売機
            process = 0x02,  // チャージ (入金)
            year = 2024, month = 5, day = 8,
            entryLine = 0, entryStation = 0,
            exitLine = 0, exitStation = 0,
            balance = 2510,
            seq = 2042
        )

        // Record 4: Ride Ikebukuro (0x01, 0x12) -> Ueno (0x01, 0x1A) Fare 180, Balance 510, Date 2024/05/06
        val b4 = makeCyberneBlock(
            terminal = 0x16, // 自動改札機
            process = 0x01,  // 運賃支払
            year = 2024, month = 5, day = 6,
            entryLine = 0x01, entryStation = 0x12, // JR東日本 池袋
            exitLine = 0x01, exitStation = 0x1A,   // JR東日本 上野
            balance = 510,
            seq = 2041
        )

        val rawBlocks = listOf(b0, b1, b2, b3, b4)
        val parsedTransitInfo = CyberneParser.parseCyberneHistory(rawBlocks, systemCode = 0x0003)
        val transitInfo = parsedTransitInfo.copy(
            commuterPass = com.nfcreader.universal.model.CommuterPassInfo(
                validStartDate = "2024/04/01",
                validEndDate = "2024/09/30",
                fromStation = "渋谷",
                toStation = "横浜",
                viaDescription = "東急東横線 経由"
            )
        )

        val memoryBlocks = rawBlocks.mapIndexed { idx, blk ->
            MemoryBlock(
                index = idx,
                hex = blk.joinToString(" ") { "%02X".format(it) },
                ascii = blk.map { c -> if ((c.toInt() and 0xFF) in 32..126) c.toInt().toChar() else '.' }.joinToString(""),
                isProtected = false,
                description = "FeliCa Service 0x090F (履歴ブロック #$idx)"
            )
        }

        val details = mapOf(
            "NFC-F システムコード" to "0x0003",
            "FeliCa IDm (製造ID)" to "01:2E:4B:99:A1:02:88:F3",
            "FeliCa PMm (製造パラメータ)" to "10:20:00:01:4B:00:4F:88",
            "カード種別" to "交通系ICカード (サイバネ規格: Suica / PASMO)",
            "カード残高" to "¥1,540",
            "直近履歴件数" to "5 件"
        )

        return NfcTagData(
            idHex = "01:2E:4B:99:A1:02:88:F3",
            standard = TagStandard.FELICA_TRANSIT,
            standardLabel = "FeliCa 交通系IC (Suica/PASMO)",
            techList = listOf("NfcF", "NdefFormatable"),
            techDetails = details,
            transitCardInfo = transitInfo,
            ndefRecords = emptyList(),
            memoryBlocks = memoryBlocks,
            allHexDump = buildString {
                appendLine("=== NFC TAG DUMP (FeliCa Suica デモ) ===")
                appendLine("UID (IDm): 01:2E:4B:99:A1:02:88:F3")
                appendLine("System Code: 0x0003 (Cyberne Transit Standard)")
                appendLine("Current Balance: ¥1,540")
                appendLine("Memory Blocks (Service 0x090F):")
                memoryBlocks.forEach {
                    appendLine("  Block #%02d: %s | %s".format(it.index, it.hex, it.ascii))
                }
            },
            scanNotes = "Suica デモカード (渋谷-横浜, セブンイレブン物販, 東京-新宿, チャージ履歴)"
        )
    }

    fun createMifareClassicDemo(): NfcTagData {
        val blocks = mutableListOf<MemoryBlock>()
        for (sector in 0 until 4) {
            val start = sector * 4
            for (b in 0 until 4) {
                val idx = start + b
                val isTrailer = (b == 3)
                val hex = if (isTrailer) {
                    "FF FF FF FF FF FF FF 07 80 69 FF FF FF FF FF FF"
                } else if (idx == 0) {
                    "A4 92 1B 89 D0 08 04 00 62 63 64 65 66 67 68 69"
                } else {
                    "44 4F 4F 52 2D 4B 45 59 2D 32 30 32 34 2D 30 31"
                }
                val ascii = if (isTrailer) "[セクタトレーラ (Key A/B)]" else if (idx == 0) "[Manufacturer Block]" else "DOOR-KEY-2024-01"
                blocks.add(
                    MemoryBlock(
                        index = idx,
                        hex = hex,
                        ascii = ascii,
                        isProtected = isTrailer,
                        description = "Sector $sector, Block $b ${if (isTrailer) "(Sector Trailer)" else "(Data)"}"
                    )
                )
            }
        }

        return NfcTagData(
            idHex = "A4:92:1B:89",
            standard = TagStandard.MIFARE_CLASSIC,
            standardLabel = "MIFARE Classic 1K",
            techList = listOf("NfcA", "MifareClassic", "NdefFormatable"),
            techDetails = mapOf(
                "NFC-A ATQA" to "00:04",
                "NFC-A SAK" to "0x08 (MIFARE Classic 1K)",
                "容量 / セクタ数" to "1024 bytes / 16 セクタ (64 ブロック)",
                "認証状態" to "Key A (Default FFFFFFFFFFFF) 認証成功"
            ),
            memoryBlocks = blocks,
            allHexDump = buildString {
                appendLine("=== NFC TAG DUMP (MIFARE Classic 1K) ===")
                appendLine("UID: A4:92:1B:89")
                appendLine("ATQA: 00:04, SAK: 0x08")
                blocks.forEach {
                    appendLine("  Block #%02d: %s | %s".format(it.index, it.hex, it.ascii))
                }
            },
            scanNotes = "オフィスの入退室カード / MIFARE Classic 1K サンプル"
        )
    }

    fun createNdefDemo(): NfcTagData {
        val records = listOf(
            NdefParsedRecord(
                index = 0,
                tnf = "Well-Known (RTD)",
                type = "U",
                uri = "https://www.jreast.co.jp/suica/",
                payloadHex = "04 77 77 77 2E 6A 72 65 61 73 74 2E 63 6F 2E 6A 70 2F 73 75 69 63 61 2F"
            ),
            NdefParsedRecord(
                index = 1,
                tnf = "Well-Known (RTD)",
                type = "T",
                text = "JR東日本 Suica 公式Webサイト",
                payloadHex = "02 6A 61 4A 52 E6 9D B1 E6 97 A5 E6 9C AC 20 53 75 69 63 61"
            )
        )

        return NfcTagData(
            idHex = "04:3A:82:C9:F1:68:80",
            standard = TagStandard.NDEF,
            standardLabel = "NTAG215 (NFC Forum Type 2)",
            techList = listOf("NfcA", "MifareUltralight", "Ndef"),
            techDetails = mapOf(
                "NFC-A ATQA" to "00:44",
                "NFC-A SAK" to "0x00",
                "NDEF タイプ" to "org.nfcforum.ndef.type2",
                "NDEF 最大サイズ" to "504 bytes",
                "NDEF 書き込み可能" to "はい"
            ),
            ndefRecords = records,
            allHexDump = buildString {
                appendLine("=== NFC TAG DUMP (NDEF URL & Text) ===")
                appendLine("UID: 04:3A:82:C9:F1:68:80")
                appendLine("Record 0: URI -> https://www.jreast.co.jp/suica/")
                appendLine("Record 1: Text -> JR東日本 Suica 公式Webサイト")
            },
            scanNotes = "スマートポスター / URL + 日本語テキストタグ"
        )
    }

    private fun makeCyberneBlock(
        terminal: Int, process: Int,
        year: Int, month: Int, day: Int,
        entryLine: Int, entryStation: Int,
        exitLine: Int, exitStation: Int,
        balance: Int, seq: Long
    ): ByteArray {
        val b = ByteArray(16)
        b[0] = terminal.toByte()
        b[1] = process.toByte()
        b[2] = 0 // reserved
        b[3] = 0 // reserved

        val dateVal = (((year - 2000) and 0x7F) shl 9) or ((month and 0x0F) shl 5) or (day and 0x1F)
        b[4] = ((dateVal ushr 8) and 0xFF).toByte()
        b[5] = (dateVal and 0xFF).toByte()

        b[6] = entryLine.toByte()
        b[7] = entryStation.toByte()
        b[8] = exitLine.toByte()
        b[9] = exitStation.toByte()

        // Balance Little Endian
        b[10] = (balance and 0xFF).toByte()
        b[11] = ((balance ushr 8) and 0xFF).toByte()

        // Sequence 3 bytes
        b[12] = ((seq ushr 16) and 0xFF).toByte()
        b[13] = ((seq ushr 8) and 0xFF).toByte()
        b[14] = (seq and 0xFF).toByte()

        b[15] = 0 // Region
        return b
    }

    private fun makeCyberneBlockShopping(
        terminal: Int, process: Int,
        year: Int, month: Int, day: Int,
        hour: Int, min: Int, sec: Int,
        balance: Int, seq: Long
    ): ByteArray {
        val b = ByteArray(16)
        b[0] = terminal.toByte()
        b[1] = process.toByte()
        b[2] = 0
        b[3] = 0

        val dateVal = (((year - 2000) and 0x7F) shl 9) or ((month and 0x0F) shl 5) or (day and 0x1F)
        b[4] = ((dateVal ushr 8) and 0xFF).toByte()
        b[5] = (dateVal and 0xFF).toByte()

        // Time bytes
        val timeVal = ((hour and 0x1F) shl 11) or ((min and 0x3F) shl 5) or ((sec / 2) and 0x1F)
        b[6] = ((timeVal ushr 8) and 0xFF).toByte()
        b[7] = (timeVal and 0xFF).toByte()

        b[8] = 0
        b[9] = 0

        b[10] = (balance and 0xFF).toByte()
        b[11] = ((balance ushr 8) and 0xFF).toByte()

        b[12] = ((seq ushr 16) and 0xFF).toByte()
        b[13] = ((seq ushr 8) and 0xFF).toByte()
        b[14] = (seq and 0xFF).toByte()

        b[15] = 0
        return b
    }
}
