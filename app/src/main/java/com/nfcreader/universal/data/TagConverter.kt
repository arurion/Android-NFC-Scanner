package com.nfcreader.universal.data

import com.nfcreader.universal.data.db.TagScanEntity
import com.nfcreader.universal.model.MemoryBlock
import com.nfcreader.universal.model.NdefParsedRecord
import com.nfcreader.universal.model.NfcTagData
import com.nfcreader.universal.model.TagStandard
import com.nfcreader.universal.model.TransitCardInfo
import com.nfcreader.universal.model.TransitTransaction
import org.json.JSONArray
import org.json.JSONObject

object TagConverter {

    fun toEntity(data: NfcTagData): TagScanEntity {
        val detailsObj = JSONObject()
        for ((k, v) in data.techDetails) {
            detailsObj.put(k, v)
        }

        var transitJson: String? = null
        if (data.transitCardInfo != null) {
            val tc = data.transitCardInfo
            val tcObj = JSONObject()
            tcObj.put("cardType", tc.cardType)
            tcObj.put("currentBalance", tc.currentBalance)
            if (tc.commuterPass != null) {
                val cp = tc.commuterPass
                val cpObj = JSONObject()
                cpObj.put("validStartDate", cp.validStartDate)
                cpObj.put("validEndDate", cp.validEndDate)
                cpObj.put("fromStation", cp.fromStation)
                cpObj.put("toStation", cp.toStation)
                cpObj.put("viaDescription", cp.viaDescription ?: "")
                tcObj.put("commuterPass", cpObj)
            }
            val txArr = JSONArray()
            for (tx in tc.transactions) {
                val o = JSONObject()
                o.put("id", tx.id)
                o.put("terminalType", tx.terminalType)
                o.put("processType", tx.processType)
                o.put("isCharge", tx.isCharge)
                o.put("isShopping", tx.isShopping)
                o.put("isBus", tx.isBus)
                o.put("dateString", tx.dateString)
                o.put("timeString", tx.timeString ?: "")
                o.put("entryLineName", tx.entryLineName ?: "")
                o.put("entryStationName", tx.entryStationName ?: "")
                o.put("exitLineName", tx.exitLineName ?: "")
                o.put("exitStationName", tx.exitStationName ?: "")
                o.put("balance", tx.balance)
                o.put("amountChanged", tx.amountChanged ?: JSONObject.NULL)
                o.put("seqNumber", tx.seqNumber)
                o.put("rawHex", tx.rawHex)
                txArr.put(o)
            }
            tcObj.put("transactions", txArr)
            transitJson = tcObj.toString()
        }

        var ndefJson: String? = null
        if (data.ndefRecords.isNotEmpty()) {
            val arr = JSONArray()
            for (r in data.ndefRecords) {
                val o = JSONObject()
                o.put("index", r.index)
                o.put("tnf", r.tnf)
                o.put("type", r.type)
                o.put("text", r.text ?: "")
                o.put("uri", r.uri ?: "")
                o.put("mimeType", r.mimeType ?: "")
                o.put("payloadHex", r.payloadHex)
                arr.put(o)
            }
            ndefJson = arr.toString()
        }

        var memoryJson: String? = null
        if (data.memoryBlocks.isNotEmpty()) {
            val arr = JSONArray()
            for (m in data.memoryBlocks) {
                val o = JSONObject()
                o.put("index", m.index)
                o.put("hex", m.hex)
                o.put("ascii", m.ascii)
                o.put("isProtected", m.isProtected)
                o.put("description", m.description ?: "")
                arr.put(o)
            }
            memoryJson = arr.toString()
        }

        val title = data.transitCardInfo?.cardType
            ?: data.techDetails["カード種別"]
            ?: data.standardLabel

        return TagScanEntity(
            uidHex = data.idHex,
            standardName = data.standard.name,
            cardTitle = title,
            balance = data.transitCardInfo?.currentBalance,
            timestamp = data.timestamp,
            techListCsv = data.techList.joinToString(","),
            detailsJson = detailsObj.toString(),
            transitJson = transitJson,
            ndefJson = ndefJson,
            memoryBlocksJson = memoryJson,
            fullDumpText = data.allHexDump,
            notes = data.scanNotes
        )
    }

    fun toData(entity: TagScanEntity): NfcTagData {
        val standard = try {
            TagStandard.valueOf(entity.standardName)
        } catch (_: Exception) {
            TagStandard.UNKNOWN
        }

        val details = mutableMapOf<String, String>()
        try {
            val obj = JSONObject(entity.detailsJson)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                details[k] = obj.optString(k, "")
            }
        } catch (_: Exception) {}

        var transitCardInfo: TransitCardInfo? = null
        if (!entity.transitJson.isNullOrEmpty()) {
            try {
                val tcObj = JSONObject(entity.transitJson)
                val cardType = tcObj.optString("cardType", "交通系IC")
                val currentBalance = tcObj.optInt("currentBalance", 0)
                val txList = mutableListOf<TransitTransaction>()
                val txArr = tcObj.optJSONArray("transactions")
                if (txArr != null) {
                    for (i in 0 until txArr.length()) {
                        val o = txArr.getJSONObject(i)
                        txList.add(
                            TransitTransaction(
                                id = o.optInt("id", i),
                                terminalType = o.optString("terminalType", ""),
                                processType = o.optString("processType", ""),
                                isCharge = o.optBoolean("isCharge", false),
                                isShopping = o.optBoolean("isShopping", false),
                                isBus = o.optBoolean("isBus", false),
                                dateString = o.optString("dateString", ""),
                                timeString = o.optString("timeString").ifEmpty { null },
                                entryLineName = o.optString("entryLineName").ifEmpty { null },
                                entryStationName = o.optString("entryStationName").ifEmpty { null },
                                exitLineName = o.optString("exitLineName").ifEmpty { null },
                                exitStationName = o.optString("exitStationName").ifEmpty { null },
                                balance = o.optInt("balance", 0),
                                amountChanged = if (o.has("amountChanged") && !o.isNull("amountChanged")) o.optInt("amountChanged") else null,
                                seqNumber = o.optLong("seqNumber", 0),
                                rawHex = o.optString("rawHex", "")
                            )
                        )
                    }
                }
                var cpInfo: com.nfcreader.universal.model.CommuterPassInfo? = null
                val cpObj = tcObj.optJSONObject("commuterPass")
                if (cpObj != null) {
                    cpInfo = com.nfcreader.universal.model.CommuterPassInfo(
                        validStartDate = cpObj.optString("validStartDate", ""),
                        validEndDate = cpObj.optString("validEndDate", ""),
                        fromStation = cpObj.optString("fromStation", ""),
                        toStation = cpObj.optString("toStation", ""),
                        viaDescription = cpObj.optString("viaDescription").ifEmpty { null }
                    )
                }
                transitCardInfo = TransitCardInfo(
                    cardType = cardType,
                    currentBalance = currentBalance,
                    transactions = txList,
                    commuterPass = cpInfo
                )
            } catch (_: Exception) {}
        }

        val ndefRecords = mutableListOf<NdefParsedRecord>()
        if (!entity.ndefJson.isNullOrEmpty()) {
            try {
                val arr = JSONArray(entity.ndefJson)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    ndefRecords.add(
                        NdefParsedRecord(
                            index = o.optInt("index", i),
                            tnf = o.optString("tnf", ""),
                            type = o.optString("type", ""),
                            text = o.optString("text").ifEmpty { null },
                            uri = o.optString("uri").ifEmpty { null },
                            mimeType = o.optString("mimeType").ifEmpty { null },
                            payloadHex = o.optString("payloadHex", "")
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        val memoryBlocks = mutableListOf<MemoryBlock>()
        if (!entity.memoryBlocksJson.isNullOrEmpty()) {
            try {
                val arr = JSONArray(entity.memoryBlocksJson)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    memoryBlocks.add(
                        MemoryBlock(
                            index = o.optInt("index", i),
                            hex = o.optString("hex", ""),
                            ascii = o.optString("ascii", ""),
                            isProtected = o.optBoolean("isProtected", false),
                            description = o.optString("description").ifEmpty { null }
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        return NfcTagData(
            idHex = entity.uidHex,
            standard = standard,
            standardLabel = standard.displayName,
            techList = entity.techListCsv.split(",").filter { it.isNotBlank() },
            timestamp = entity.timestamp,
            techDetails = details,
            transitCardInfo = transitCardInfo,
            ndefRecords = ndefRecords,
            memoryBlocks = memoryBlocks,
            allHexDump = entity.fullDumpText,
            scanNotes = entity.notes
        )
    }
}
