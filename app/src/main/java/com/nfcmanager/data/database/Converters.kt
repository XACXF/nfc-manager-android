package com.nfcmanager.data.database

import androidx.room.TypeConverter
import com.nfcmanager.data.model.NFCType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromNFCType(value: NFCType): String {
        return value.name
    }

    @TypeConverter
    fun toNFCType(value: String): NFCType {
        return try {
            NFCType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            NFCType.UNKNOWN
        }
    }

    @TypeConverter
    fun fromByteArray(value: ByteArray?): String? {
        return value?.let { 
            it.joinToString(",") { byte -> byte.toString() }
        }
    }

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? {
        return value?.let {
            if (it.isEmpty()) null
            else it.split(",").map { s -> s.trim().toByte() }.toByteArray()
        }
    }
}
