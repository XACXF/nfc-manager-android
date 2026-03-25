package com.nfcmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "nfc_data")
data class NFCData(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val type: NFCType,
    val readTime: Date = Date(),
    val name: String = "",  // 自定义名称
    val note: String = "",
    val rawData: ByteArray? = null,
    val aarPackage: String? = null,  // Android Application Record包名（用于自动启动应用）
    val ndefMessage: ByteArray? = null,  // 完整的NDEF消息原始字节（用于完整复制）
    val techList: String? = null  // Tag支持的技术列表（如Ndef, NfcA, MifareClassic等）
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NFCData
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int = id.hashCode()
}

enum class NFCType {
    TEXT,
    URL,
    VCARD,
    PHONE,
    EMAIL,
    WIFI,
    GEO,
    APP,
    UNKNOWN;
    
    companion object {
        fun fromMimeType(mimeType: String): NFCType {
            return when {
                mimeType.startsWith("text/plain") -> TEXT
                mimeType.startsWith("text/x-vcard") -> VCARD
                mimeType.startsWith("text/vcard") -> VCARD
                else -> UNKNOWN
            }
        }
    }
}
