package com.nfcreader.universal.data

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import com.nfcreader.universal.model.MemoryBlock
import com.nfcreader.universal.model.NdefParsedRecord
import com.nfcreader.universal.model.NfcTagData
import com.nfcreader.universal.model.TagStandard
import com.nfcreader.universal.model.TransitCardInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object NfcTagReader {

    /**
     * @param customKeys user-registered MIFARE Classic keys (Key A/B, 6 bytes each) to try
     *        in addition to the built-in well-known default keys. These are keys the user
     *        already knows for their own card — this does not crack or guess unknown keys.
     */
    suspend fun readTag(tag: Tag, customKeys: List<ByteArray> = emptyList()): NfcTagData = withContext(Dispatchers.IO) {
        val idBytes = tag.id ?: byteArrayOf()
        val idHex = idBytes.joinToString(":") { "%02X".format(it) }
        val techList = tag.techList.map { it.substringAfterLast(".") }

        val details = mutableMapOf<String, String>()
        val memoryBlocks = mutableListOf<MemoryBlock>()
        var transitCardInfo: TransitCardInfo? = null
        val ndefRecords = mutableListOf<NdefParsedRecord>()

        var determinedStandard = TagStandard.UNKNOWN

        // 1. Check NfcF (FeliCa / Transit cards / Edy / nanaco / WAON)
        if (techList.contains("NfcF")) {
            val nfcF = NfcF.get(tag)
            if (nfcF != null) {
                try {
                    val sysCodeBytes = nfcF.systemCode
                    val sysCode = if (sysCodeBytes != null && sysCodeBytes.size >= 2) {
                        ((sysCodeBytes[0].toInt() and 0xFF) shl 8) or (sysCodeBytes[1].toInt() and 0xFF)
                    } else 0
                    val pmmBytes = nfcF.manufacturer ?: byteArrayOf()
                    val pmmHex = pmmBytes.joinToString(":") { "%02X".format(it) }

                    details["NFC-F システムコード"] = "0x%04X".format(sysCode)
                    details["FeliCa IDm (製造ID)"] = idHex
                    details["FeliCa PMm (製造パラメータ)"] = pmmHex

                    if (sysCode == 0x0003) {
                        determinedStandard = TagStandard.FELICA_TRANSIT
                        details["カード種別"] = "交通系ICカード (サイバネ規格: Suica/PASMO/ICOCA等)"
                    } else {
                        determinedStandard = TagStandard.FELICA_GENERAL
                        when (sysCode) {
                            0xfe00 -> details["カード種別"] = "楽天Edy / 共通エリア"
                            0x86a7 -> details["カード種別"] = "nanaco"
                            0x8008 -> details["カード種別"] = "WAON"
                            0x88b4 -> details["カード種別"] = "OKICA (沖縄)"
                            else -> details["カード種別"] = "FeliCa 独自システムコード (0x%04X)".format(sysCode)
                        }
                    }

                    // Attempt to read FeliCa transit history blocks (Service 0x090F)
                    val rawFeliCaBlocks = readFeliCaBlocks(nfcF, idBytes, 0x090F, maxBlocks = 20)
                    if (rawFeliCaBlocks.isNotEmpty()) {
                        transitCardInfo = CyberneParser.parseCyberneHistory(rawFeliCaBlocks, sysCode)
                        for ((idx, block) in rawFeliCaBlocks.withIndex()) {
                            val hex = block.joinToString(" ") { "%02X".format(it) }
                            val ascii = bytesToAscii(block)
                            memoryBlocks.add(
                                MemoryBlock(
                                    index = idx,
                                    hex = hex,
                                    ascii = ascii,
                                    isProtected = false,
                                    description = "FeliCa Service 0x090F (利用履歴 ブロック #$idx)"
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    details["FeliCa 読み取りステータス"] = "一部暗号化/制限: ${e.message}"
                }
            }
        }

        // 2. Check MifareClassic
        if (techList.contains("MifareClassic")) {
            val mifare = MifareClassic.get(tag)
            if (mifare != null) {
                if (determinedStandard == TagStandard.UNKNOWN) determinedStandard = TagStandard.MIFARE_CLASSIC
                try {
                    details["MIFARE 種別"] = when (mifare.type) {
                        MifareClassic.TYPE_CLASSIC -> "MIFARE Classic"
                        MifareClassic.TYPE_PLUS -> "MIFARE Plus"
                        MifareClassic.TYPE_PRO -> "MIFARE Pro"
                        else -> "MIFARE 不明 (${mifare.type})"
                    }
                    details["容量 / セクタ数"] = "${mifare.size} bytes / ${mifare.sectorCount} セクタ (${mifare.blockCount} ブロック)"
                    if (customKeys.isNotEmpty()) {
                        details["鍵認証"] = "登録済みカスタム鍵 ${customKeys.size}件 + 既定鍵 ${DEFAULT_MIFARE_KEYS.size}件を試行"
                    } else {
                        details["鍵認証"] = "既定鍵 ${DEFAULT_MIFARE_KEYS.size}件のみ試行（カスタム鍵は未登録）"
                    }

                    readMifareClassicBlocks(mifare, memoryBlocks, customKeys)
                } catch (e: Exception) {
                    details["MIFARE 読み取り警告"] = "${e.message}"
                }
            }
        }

        // 3. Check MifareUltralight / NTAG
        if (techList.contains("MifareUltralight")) {
            val ultralight = MifareUltralight.get(tag)
            if (ultralight != null) {
                if (determinedStandard == TagStandard.UNKNOWN) determinedStandard = TagStandard.MIFARE_ULTRALIGHT
                try {
                    val ultraTypeName = when (ultralight.type) {
                        MifareUltralight.TYPE_ULTRALIGHT -> "MIFARE Ultralight"
                        MifareUltralight.TYPE_ULTRALIGHT_C -> "MIFARE Ultralight C"
                        else -> "MIFARE Ultralight / NTAG"
                    }
                    details["タグ種別"] = ultraTypeName

                    readMifareUltralightPages(ultralight, memoryBlocks)
                } catch (e: Exception) {
                    details["Ultralight 読み取り警告"] = "${e.message}"
                }
            }
        }

        // 4. Check IsoDep (ISO 14443-4)
        if (techList.contains("IsoDep")) {
            val isoDep = IsoDep.get(tag)
            if (isoDep != null) {
                if (determinedStandard == TagStandard.UNKNOWN) determinedStandard = TagStandard.ISO_DEP
                try {
                    val histBytes = isoDep.historicalBytes
                    if (histBytes != null && histBytes.isNotEmpty()) {
                        details["ISO-DEP Historical Bytes"] = histBytes.joinToString(":") { "%02X".format(it) }
                    }
                    val hiLayer = isoDep.hiLayerResponse
                    if (hiLayer != null && hiLayer.isNotEmpty()) {
                        details["ISO-DEP HiLayer Response"] = hiLayer.joinToString(":") { "%02X".format(it) }
                    }
                    details["ISO-DEP Max Transceive"] = "${isoDep.maxTransceiveLength} bytes"
                } catch (e: Exception) {
                    details["ISO-DEP ステータス"] = "${e.message}"
                }
            }
        }

        // 5. Check NfcA (ATQA / SAK)
        if (techList.contains("NfcA")) {
            val nfcA = NfcA.get(tag)
            if (nfcA != null) {
                if (determinedStandard == TagStandard.UNKNOWN) determinedStandard = TagStandard.NFC_A
                val atqaHex = nfcA.atqa.joinToString(":") { "%02X".format(it) }
                val sakHex = "0x%02X".format(nfcA.sak)
                details["NFC-A ATQA (Type A 応答)"] = atqaHex
                details["NFC-A SAK (選択確認)"] = sakHex
            }
        }

        // 6. Check NfcB
        if (techList.contains("NfcB")) {
            val nfcB = NfcB.get(tag)
            if (nfcB != null) {
                if (determinedStandard == TagStandard.UNKNOWN) determinedStandard = TagStandard.NFC_B
                val appData = nfcB.applicationData?.joinToString(":") { "%02X".format(it) } ?: "なし"
                val protoInfo = nfcB.protocolInfo?.joinToString(":") { "%02X".format(it) } ?: "なし"
                details["NFC-B アプリケーションデータ"] = appData
                details["NFC-B プロトコル情報"] = protoInfo
            }
        }

        // 7. Check NfcV (ISO 15693 Vicinity)
        if (techList.contains("NfcV")) {
            val nfcV = NfcV.get(tag)
            if (nfcV != null) {
                if (determinedStandard == TagStandard.UNKNOWN) determinedStandard = TagStandard.NFC_V
                details["NFC-V DSFID"] = "0x%02X".format(nfcV.dsfId)
                details["NFC-V 応答フラグ"] = "0x%02X".format(nfcV.responseFlags)
                readNfcVBlocks(nfcV, memoryBlocks)
            }
        }

        // 8. Check NDEF (NFC Forum)
        if (techList.contains("Ndef") || techList.contains("NdefFormatable")) {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                if (determinedStandard == TagStandard.UNKNOWN) determinedStandard = TagStandard.NDEF
                try {
                    details["NDEF タイプ"] = ndef.type ?: "標準NDEF"
                    details["NDEF 最大サイズ"] = "${ndef.maxSize} bytes"
                    details["NDEF 書き込み可能"] = if (ndef.isWritable) "はい" else "読み取り専用"

                    val message = ndef.cachedNdefMessage
                    if (message != null) {
                        parseNdefRecords(message, ndefRecords)
                    }
                } catch (e: Exception) {
                    details["NDEF 読み取りステータス"] = "${e.message}"
                }
            }
        }

        // Build composite hex dump summary
        val hexDumpSummary = buildString {
            appendLine("=== NFC TAG DUMP (UID: $idHex) ===")
            appendLine("Detected Technologies: ${techList.joinToString(", ")}")
            for ((k, v) in details) {
                appendLine("$k: $v")
            }
            if (memoryBlocks.isNotEmpty()) {
                appendLine("\n--- MEMORY BLOCKS (${memoryBlocks.size} blocks) ---")
                for (b in memoryBlocks) {
                    val desc = if (b.description != null) " [${b.description}]" else ""
                    appendLine("Block #%03d: %-48s | %s%s".format(b.index, b.hex, b.ascii, desc))
                }
            }
        }

        NfcTagData(
            idHex = idHex,
            standard = determinedStandard,
            standardLabel = determinedStandard.displayName,
            techList = techList,
            techDetails = details,
            transitCardInfo = transitCardInfo,
            ndefRecords = ndefRecords,
            memoryBlocks = memoryBlocks,
            allHexDump = hexDumpSummary
        )
    }

    /**
     * Reads FeliCa blocks without encryption for a specific service code (e.g. 0x090F for transit history)
     */
    private fun readFeliCaBlocks(nfcF: NfcF, idm: ByteArray, serviceCode: Int, maxBlocks: Int = 20): List<ByteArray> {
        val blocks = mutableListOf<ByteArray>()
        try {
            nfcF.connect()
            // Read blocks in batches or individually to be resilient
            for (i in 0 until maxBlocks) {
                val out = ByteArrayOutputStream()
                // Command Length placeholder
                out.write(0x10) // 16 bytes for single block read command
                out.write(0x06) // Command: Read Without Encryption
                out.write(idm)  // 8 bytes IDm
                out.write(0x01) // 1 service
                // Service code in Little Endian: e.g. 0x090F -> 0x0F, 0x09
                out.write(serviceCode and 0xFF)
                out.write((serviceCode ushr 8) and 0xFF)
                out.write(0x01) // 1 block
                // 2-byte Block List Element: 0x80 (2-byte element, service index 0), block index i
                out.write(0x80)
                out.write(i)

                val req = out.toByteArray()
                req[0] = req.size.toByte()

                val res = nfcF.transceive(req)
                if (res != null && res.size >= 13) {
                    val status1 = res[10].toInt() and 0xFF
                    val status2 = res[11].toInt() and 0xFF
                    if (status1 == 0 && status2 == 0) {
                        val numBlocks = res[12].toInt() and 0xFF
                        if (numBlocks >= 1 && res.size >= 13 + 16) {
                            val blockData = res.copyOfRange(13, 13 + 16)
                            blocks.add(blockData)
                        }
                    } else {
                        // Reached end of accessible blocks or permission denied
                        break
                    }
                } else {
                    break
                }
            }
        } catch (_: Exception) {
            // Card moved or finished
        } finally {
            try { nfcF.close() } catch (_: Exception) {}
        }
        return blocks
    }

    /** Well-known, publicly documented MIFARE Classic default/test keys. */
    val DEFAULT_MIFARE_KEYS: List<ByteArray> = listOf(
        MifareClassic.KEY_DEFAULT,
        MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY,
        MifareClassic.KEY_NFC_FORUM,
        byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
        byteArrayOf(0xB0.toByte(), 0xB1.toByte(), 0xB2.toByte(), 0xB3.toByte(), 0xB4.toByte(), 0xB5.toByte()),
        byteArrayOf(0x4D, 0x3A, 0x99.toByte(), 0xC3.toByte(), 0x51, 0xDD.toByte()),
        byteArrayOf(0x1A, 0x98.toByte(), 0x2C, 0x7E, 0x45, 0x9A.toByte())
    )

    private fun readMifareClassicBlocks(
        mifare: MifareClassic,
        memoryBlocks: MutableList<MemoryBlock>,
        customKeys: List<ByteArray> = emptyList()
    ) {
        try {
            mifare.connect()
            // Try the user's own registered keys first (most likely to match their card),
            // then fall back to the well-known public default keys.
            val keysToTry = (customKeys + DEFAULT_MIFARE_KEYS).distinctBy { it.joinToString(",") }

            for (sector in 0 until mifare.sectorCount) {
                var authenticated = false
                for (key in keysToTry) {
                    if (mifare.authenticateSectorWithKeyA(sector, key) || mifare.authenticateSectorWithKeyB(sector, key)) {
                        authenticated = true
                        break
                    }
                }

                val firstBlock = mifare.sectorToBlock(sector)
                val blockCount = mifare.getBlockCountInSector(sector)

                for (b in 0 until blockCount) {
                    val blockIndex = firstBlock + b
                    if (authenticated) {
                        try {
                            val blockBytes = mifare.readBlock(blockIndex)
                            memoryBlocks.add(
                                MemoryBlock(
                                    index = blockIndex,
                                    hex = blockBytes.joinToString(" ") { "%02X".format(it) },
                                    ascii = bytesToAscii(blockBytes),
                                    isProtected = false,
                                    description = "Sector $sector, Block $b"
                                )
                            )
                        } catch (_: Exception) {
                            memoryBlocks.add(
                                MemoryBlock(
                                    index = blockIndex,
                                    hex = "-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --",
                                    ascii = "[保護/読取不可]",
                                    isProtected = true,
                                    description = "Sector $sector, Block $b (Read Error)"
                                )
                            )
                        }
                    } else {
                        memoryBlocks.add(
                            MemoryBlock(
                                index = blockIndex,
                                hex = "?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ?? ??",
                                ascii = "[キー保護中]",
                                isProtected = true,
                                description = "Sector $sector, Block $b (Auth Required)"
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            try { mifare.close() } catch (_: Exception) {}
        }
    }

    private fun readMifareUltralightPages(ultralight: MifareUltralight, memoryBlocks: MutableList<MemoryBlock>) {
        try {
            ultralight.connect()
            // Read 16 pages (each call reads 4 pages / 16 bytes)
            for (page in 0 until 16 step 4) {
                try {
                    val bytes = ultralight.readPages(page)
                    for (subPage in 0 until 4) {
                        val pIdx = page + subPage
                        val pageBytes = bytes.copyOfRange(subPage * 4, (subPage + 1) * 4)
                        memoryBlocks.add(
                            MemoryBlock(
                                index = pIdx,
                                hex = pageBytes.joinToString(" ") { "%02X".format(it) },
                                ascii = bytesToAscii(pageBytes),
                                isProtected = false,
                                description = "Page #$pIdx"
                            )
                        )
                    }
                } catch (_: Exception) {
                    break
                }
            }
        } catch (_: Exception) {
        } finally {
            try { ultralight.close() } catch (_: Exception) {}
        }
    }

    private fun readNfcVBlocks(nfcV: NfcV, memoryBlocks: MutableList<MemoryBlock>) {
        try {
            nfcV.connect()
            for (block in 0 until 16) {
                // ISO 15693 Read Single Block Command
                val cmd = byteArrayOf(0x22, 0x20, block.toByte())
                val res = nfcV.transceive(cmd)
                if (res != null && res.isNotEmpty() && res[0] == 0.toByte()) {
                    val data = res.copyOfRange(1, res.size)
                    memoryBlocks.add(
                        MemoryBlock(
                            index = block,
                            hex = data.joinToString(" ") { "%02X".format(it) },
                            ascii = bytesToAscii(data),
                            isProtected = false,
                            description = "ISO 15693 Block #$block"
                        )
                    )
                }
            }
        } catch (_: Exception) {
        } finally {
            try { nfcV.close() } catch (_: Exception) {}
        }
    }

    private fun parseNdefRecords(message: NdefMessage, outList: MutableList<NdefParsedRecord>) {
        for ((idx, record) in message.records.withIndex()) {
            val tnfStr = when (record.tnf) {
                NdefRecord.TNF_WELL_KNOWN -> "Well-Known (RTD)"
                NdefRecord.TNF_MIME_MEDIA -> "MIME Media"
                NdefRecord.TNF_ABSOLUTE_URI -> "Absolute URI"
                NdefRecord.TNF_EXTERNAL_TYPE -> "External Type"
                NdefRecord.TNF_EMPTY -> "Empty"
                NdefRecord.TNF_UNCHANGED -> "Unchanged"
                else -> "Unknown (${record.tnf})"
            }

            val typeStr = String(record.type, Charset.forName("US-ASCII"))
            val payload = record.payload ?: byteArrayOf()
            val payloadHex = payload.joinToString(" ") { "%02X".format(it) }

            var textContent: String? = null
            var uriContent: String? = null
            var mimeType: String? = null

            if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                if (payload.isNotEmpty()) {
                    val isUtf16 = (payload[0].toInt() and 0x80) != 0
                    val langLen = payload[0].toInt() and 0x3F
                    val charset = if (isUtf16) Charset.forName("UTF-16") else Charset.forName("UTF-8")
                    if (payload.size > 1 + langLen) {
                        textContent = String(payload, 1 + langLen, payload.size - 1 - langLen, charset)
                    }
                }
            } else if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_URI)) {
                if (payload.isNotEmpty()) {
                    val prefixCode = payload[0].toInt() and 0xFF
                    val prefix = getUriPrefix(prefixCode)
                    val rest = String(payload, 1, payload.size - 1, Charset.forName("UTF-8"))
                    uriContent = prefix + rest
                }
            } else if (record.tnf == NdefRecord.TNF_MIME_MEDIA) {
                mimeType = typeStr
                textContent = String(payload, Charset.forName("UTF-8"))
            }

            outList.add(
                NdefParsedRecord(
                    index = idx,
                    tnf = tnfStr,
                    type = typeStr,
                    text = textContent,
                    uri = uriContent,
                    mimeType = mimeType,
                    payloadHex = payloadHex
                )
            )
        }
    }

    private fun getUriPrefix(code: Int): String = when (code) {
        0x01 -> "http://www."
        0x02 -> "https://www."
        0x03 -> "http://"
        0x04 -> "https://"
        0x05 -> "tel:"
        0x06 -> "mailto:"
        0x07 -> "ftp://anonymous:anonymous@"
        0x08 -> "ftp://ftp."
        0x09 -> "ftps://"
        0x0A -> "sftp://"
        0x0B -> "smb://"
        0x0C -> "nfs://"
        0x0D -> "ftp://"
        0x0E -> "dav://"
        0x0F -> "news:"
        0x10 -> "telnet://"
        0x11 -> "imap:"
        0x12 -> "rtsp://"
        0x13 -> "urn:"
        0x14 -> "pop:"
        0x15 -> "sip:"
        0x16 -> "sips:"
        0x17 -> "tftp:"
        0x18 -> "btspp://"
        0x19 -> "btl2cap://"
        0x1A -> "btgoep://"
        0x1B -> "tcpobex://"
        0x1C -> "irdaobex://"
        0x1D -> "file://"
        0x1E -> "urn:epc:id:"
        0x1F -> "urn:epc:tag:"
        0x20 -> "urn:epc:pat:"
        0x21 -> "urn:epc:raw:"
        0x22 -> "urn:epc:"
        0x23 -> "urn:nfc:"
        else -> ""
    }

    private fun bytesToAscii(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            val c = (b.toInt() and 0xFF).toChar()
            if (c in ' '..'~') {
                sb.append(c)
            } else {
                sb.append('.')
            }
        }
        return sb.toString()
    }
}
