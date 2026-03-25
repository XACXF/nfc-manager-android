package com.nfcmanager.nfc

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.util.Log
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import java.nio.charset.Charset

class NFCManager(private val context: Context) {
    
    companion object {
        private const val TAG = "NFCManager"
    }
    
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    
    fun isNFCSupported(): Boolean = nfcAdapter != null
    
    fun isNFCEnabled(): Boolean = nfcAdapter?.isEnabled == true
    
    fun getNFCStatus(): NFCStatus {
        return when {
            !isNFCSupported() -> NFCStatus.NOT_SUPPORTED
            !isNFCEnabled() -> NFCStatus.DISABLED
            else -> NFCStatus.ENABLED
        }
    }
    
    fun readNFCTag(tag: Tag): NFCReadResult {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                readNDEFTag(ndef)
            } else {
                NFCReadResult.Success(NFCData(
                    content = "Non-NDEF Tag: ${tag.techList.joinToString()}",
                    type = NFCType.UNKNOWN,
                    rawData = tag.id
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read NFC tag", e)
            NFCReadResult.Error("Read failed: ${e.message}")
        }
    }
    
    private fun readNDEFTag(ndef: Ndef): NFCReadResult {
        return try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()
            
            if (ndefMessage != null) {
                parseNDEFMessage(ndefMessage)
            } else {
                NFCReadResult.Success(NFCData(
                    content = "Empty tag",
                    type = NFCType.TEXT
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read NDEF tag", e)
            NFCReadResult.Error("NDEF read failed: ${e.message}")
        }
    }
    
    private fun parseNDEFMessage(ndefMessage: NdefMessage): NFCReadResult {
        val records = ndefMessage.records
        if (records.isEmpty()) {
            return NFCReadResult.Success(NFCData(
                content = "Empty NDEF message",
                type = NFCType.TEXT
            ))
        }
        
        val record = records[0]
        return when {
            record.toUri() != null -> parseURIRecord(record)
            record.tnf == NdefRecord.TNF_WELL_KNOWN && 
                java.util.Arrays.equals(record.type, NdefRecord.RTD_TEXT) -> parseTextRecord(record)
            record.tnf == NdefRecord.TNF_MIME_MEDIA -> parseMimeRecord(record)
            else -> parseUnknownRecord(record)
        }
    }
    
    private fun parseMimeRecord(record: NdefRecord): NFCReadResult {
        val mimeType = String(record.type, Charset.forName("US-ASCII"))
        return when {
            mimeType.startsWith("text/vcard") || mimeType.startsWith("text/x-vcard") -> {
                val content = String(record.payload, Charset.forName("UTF-8"))
                NFCReadResult.Success(NFCData(content = content, type = NFCType.VCARD))
            }
            mimeType.startsWith("application/vnd.wfa.wsc") -> {
                // WiFi configuration
                val content = String(record.payload, Charset.forName("UTF-8"))
                NFCReadResult.Success(NFCData(content = content, type = NFCType.WIFI))
            }
            else -> parseUnknownRecord(record)
        }
    }
    
    private fun parseURIRecord(record: NdefRecord): NFCReadResult {
        val uri = record.toUri()
        return if (uri != null) {
            val content = uri.toString()
            val type = when {
                content.startsWith("http://") || content.startsWith("https://") -> NFCType.URL
                content.startsWith("tel:") -> NFCType.PHONE
                content.startsWith("mailto:") -> NFCType.EMAIL
                content.startsWith("geo:") -> NFCType.GEO
                else -> NFCType.URL
            }
            NFCReadResult.Success(NFCData(content = content, type = type))
        } else {
            NFCReadResult.Error("Failed to parse URI")
        }
    }
    
    private fun parseTextRecord(record: NdefRecord): NFCReadResult {
        return try {
            val payload = record.payload
            val textEncoding = if ((payload[0].toInt() and 0x80) == 0) "UTF-8" else "UTF-16"
            val languageCodeLength = payload[0].toInt() and 0x3F
            val text = String(
                payload,
                languageCodeLength + 1,
                payload.size - languageCodeLength - 1,
                Charset.forName(textEncoding)
            )
            NFCReadResult.Success(NFCData(content = text, type = NFCType.TEXT))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse text record", e)
            NFCReadResult.Error("Text parse failed: ${e.message}")
        }
    }
    
    private fun parseUnknownRecord(record: NdefRecord): NFCReadResult {
        return try {
            val payload = record.payload
            val content = String(payload, Charset.forName("UTF-8"))
            NFCReadResult.Success(NFCData(content = content, type = NFCType.UNKNOWN))
        } catch (e: Exception) {
            NFCReadResult.Success(NFCData(
                content = "Unparseable NDEF record",
                type = NFCType.UNKNOWN
            ))
        }
    }
    
    sealed class NFCReadResult {
        data class Success(val data: NFCData) : NFCReadResult()
        data class Error(val message: String) : NFCReadResult()
    }
    
    enum class NFCStatus {
        ENABLED,
        DISABLED,
        NOT_SUPPORTED
    }
}
