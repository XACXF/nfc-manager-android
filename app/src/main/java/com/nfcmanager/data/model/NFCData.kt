package com.nfcmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * NFC鏁版嵁瀹炰綋绫?
 */
@Entity(tableName = "nfc_data")
data class NFCData(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val type: NFCType,
    val readTime: Date = Date(),
    val note: String = "",
    val rawData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NFCData

        if (id != other.id) return false
        if (content != other.content) return false
        if (type != other.type) return false
        if (readTime != other.readTime) return false
        if (note != other.note) return false
        if (rawData != null) {
            if (other.rawData == null) return false
            if (!rawData.contentEquals(other.rawData)) return false
        } else if (other.rawData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + readTime.hashCode()
        result = 31 * result + note.hashCode()
        result = 31 * result + (rawData?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * NFC鏁版嵁绫诲瀷鏋氫妇
 */
enum class NFCType {
    TEXT,       // 鏂囨湰绫诲瀷
    URL,        // URL绫诲瀷
    VCARD,      // 鍚嶇墖绫诲瀷
    PHONE,      // 鐢佃瘽绫诲瀷
    EMAIL,      // 閭绫诲瀷
    WIFI,       // WiFi閰嶇疆
    GEO,        // 鍦扮悊浣嶇疆
    APP,        // 搴旂敤鍚姩
    UNKNOWN,    // 鏈煡绫诲瀷
    
    companion object {
        fun fromMimeType(mimeType: String): NFCType {
            return when (mimeType) {
                "text/plain" -> TEXT
                "text/x-vcard" -> VCARD
                "text/vcard" -> VCARD
                else -> when {
                    mimeType.startsWith("http://") || mimeType.startsWith("https://") -> URL
                    mimeType.startsWith("tel:") -> PHONE
                    mimeType.startsWith("mailto:") -> EMAIL
                    mimeType.startsWith("geo:") -> GEO
                    else -> UNKNOWN
                }
            }
        }
    }
}