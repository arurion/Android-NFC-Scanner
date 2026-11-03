package com.nfcreader.universal.data

/**
 * Shared helpers for converting between hex strings and byte arrays,
 * used by the custom MIFARE key feature and elsewhere.
 */
object HexUtils {

    /** MIFARE Classic keys (Key A / Key B) are always 6 bytes = 12 hex characters. */
    const val MIFARE_KEY_HEX_LENGTH = 12

    /**
     * Normalizes user input (trims whitespace, strips ":"/"-" separators, uppercases)
     * so "AA:BB-CC DD EE ff" and "AABBCCDDEEFF" are treated the same.
     */
    fun normalizeHex(input: String): String {
        return input.trim().replace(":", "").replace("-", "").replace(" ", "").uppercase()
    }

    fun isValidHexOfLength(input: String, length: Int): Boolean {
        val normalized = normalizeHex(input)
        if (normalized.length != length) return false
        return normalized.all { it in '0'..'9' || it in 'A'..'F' }
    }

    fun isValidMifareKeyHex(input: String): Boolean = isValidHexOfLength(input, MIFARE_KEY_HEX_LENGTH)

    /** Returns null if the hex string is malformed. */
    fun hexToBytesOrNull(input: String): ByteArray? {
        val normalized = normalizeHex(input)
        if (normalized.length % 2 != 0 || normalized.isEmpty()) return null
        return try {
            ByteArray(normalized.length / 2) { i ->
                val byteStr = normalized.substring(i * 2, i * 2 + 2)
                byteStr.toInt(16).toByte()
            }
        } catch (_: Exception) {
            null
        }
    }

    fun bytesToHex(bytes: ByteArray): String = bytes.joinToString("") { "%02X".format(it) }
}
