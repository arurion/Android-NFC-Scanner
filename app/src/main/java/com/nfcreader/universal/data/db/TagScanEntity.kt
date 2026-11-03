package com.nfcreader.universal.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_tags")
data class TagScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uidHex: String,
    val standardName: String,
    val cardTitle: String,
    val balance: Int?,
    val timestamp: Long,
    val techListCsv: String,
    val detailsJson: String,
    val transitJson: String?,
    val ndefJson: String?,
    val memoryBlocksJson: String?,
    val fullDumpText: String,
    val notes: String?
)
