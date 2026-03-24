package com.nfcmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * NFC数据实体类
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
 * NFC数据类型枚举
 */
enum class NFCType {
    TEXT,       // 文本类型
    URL,        // URL类型
    VCARD,      // 名片类型
    PHONE,      // 电话类型
    EMAIL,      // 邮箱类型
    WIFI,       // WiFi配置
    GEO,        // 地理位置
    APP,        // 应用启动
    UNKNOWN,    // 未知类型
    
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