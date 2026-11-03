package com.nfcreader.universal.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-registered MIFARE Classic key (Key A or Key B), tried in addition to the
 * built-in well-known default keys when authenticating sectors during a scan.
 *
 * Users provide this for cards whose key they already know (their own access card,
 * a tag they personally programmed, a documented demo/test key, etc.) — the app does
 * not attempt to crack or brute-force unknown keys.
 */
@Entity(tableName = "custom_mifare_keys")
data class CustomKeyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val keyHex: String, // 12 hex chars (6 bytes), normalized uppercase
    val createdAt: Long = System.currentTimeMillis()
)
