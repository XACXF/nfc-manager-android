package com.nfcmanager.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.IsoDep
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.util.Log
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import java.nio.charset.Charset
import java.util.*

/**
 * NFC绠＄悊鍣?
 * 璐熻矗NFC璁惧鐨勬娴嬨€佹爣绛捐鍙栧拰鐘舵€佺鐞?
 */
class NFCManager(private val context: Context) {
    
    companion object {
        private const val TAG = "NFCManager"
        
        // NDEF璁板綍绫诲瀷
        private const val RTD_TEXT = "T"
        private const val RTD_URI = "U"
        private const val RTD_SMART_POSTER = "Sp"
        private const val RTD_ALTERNATIVE_CARRIER = "ac"
        private const val RTD_HANDOVER_CARRIER = "Hc"
        private const val RTD_HANDOVER_REQUEST = "Hr"
        private const val RTD_HANDOVER_SELECT = "Hs"
        private const val RTD_VCARD = "text/x-vCard"
    }
    
    private val nfcManager: NfcManager = context.getSystemService(Context.NFC_SERVICE) as NfcManager
    val nfcAdapter: NfcAdapter? = nfcManager.defaultAdapter
    
    /**
     * 妫€鏌ヨ澶囨槸鍚︽敮鎸丯FC
     */
    fun isNFCSupported(): Boolean {
        return nfcAdapter != null
    }
    
    /**
     * 妫€鏌FC鏄惁宸插惎鐢?
     */
    fun isNFCEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }
    
    /**
     * 鑾峰彇NFC鐘舵€?
     */
    fun getNFCStatus(): NFCStatus {
        return when {
            !isNFCSupported() -> NFCStatus.NOT_SUPPORTED
            !isNFCEnabled() -> NFCStatus.DISABLED
            else -> NFCStatus.ENABLED
        }
    }
    
    /**
     * 浠嶵ag璇诲彇NFC鏁版嵁
     */
    fun readNFCTag(tag: Tag): NFCReadResult {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                readNDEFTag(ndef)
            } else {
                // 灏濊瘯璇诲彇闈濶DEF鏍囩
                readNonNDEFTag(tag)
            }
        } catch (e: Exception) {
            Log.e(TAG, "璇诲彇NFC鏍囩澶辫触", e)
            NFCReadResult.Error("璇诲彇澶辫触: ${e.message}")
        }
    }
    
    /**
     * 璇诲彇NDEF鏍煎紡鏍囩
     */
    private fun readNDEFTag(ndef: Ndef): NFCReadResult {
        return try {
            ndef.connect()
            val ndefMessage = ndef.ndefMessage
            ndef.close()
            
            if (ndefMessage != null) {
                parseNDEFMessage(ndefMessage)
            } else {
                NFCReadResult.Success(NFCData(
                    content = "绌烘爣绛?,
                    type = NFCType.TEXT
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "璇诲彇NDEF鏍囩澶辫触", e)
            NFCReadResult.Error("璇诲彇NDEF鏍囩澶辫触: ${e.message}")
        }
    }
    
    /**
     * 璇诲彇闈濶DEF鏍煎紡鏍囩
     */
    private fun readNonNDEFTag(tag: Tag): NFCReadResult {
        return try {
            // 灏濊瘯璇嗗埆鏍囩绫诲瀷
            val techList = tag.techList
            val tagType = when {
                techList.any { it.contains(NfcA::class.java.name) } -> "NFC-A"
                techList.any { it.contains(NfcB::class.java.name) } -> "NFC-B"
                techList.any { it.contains(NfcF::class.java.name) } -> "NFC-F"
                techList.any { it.contains(NfcV::class.java.name) } -> "NFC-V"
                techList.any { it.contains(MifareClassic::class.java.name) } -> "Mifare Classic"
                techList.any { it.contains(MifareUltralight::class.java.name) } -> "Mifare Ultralight"
                techList.any { it.contains(IsoDep::class.java.name) } -> "ISO-DEP"
                else -> "鏈煡绫诲瀷"
            }
            
            NFCReadResult.Success(NFCData(
                content = "闈濶DEF鏍囩: $tagType",
                type = NFCType.UNKNOWN,
                rawData = tag.id
            ))
        } catch (e: Exception) {
            Log.e(TAG, "璇诲彇闈濶DEF鏍囩澶辫触", e)
            NFCReadResult.Error("璇诲彇鏍囩澶辫触: ${e.message}")
        }
    }
    
    /**
     * 瑙ｆ瀽NDEF娑堟伅
     */
    private fun parseNDEFMessage(ndefMessage: NdefMessage): NFCReadResult {
        val records = ndefMessage.records
        if (records.isEmpty()) {
            return NFCReadResult.Success(NFCData(
                content = "绌篘DEF娑堟伅",
                type = NFCType.TEXT
            ))
        }
        
        // 鍙鐞嗙涓€涓褰曪紙绠€鍖栧鐞嗭級
        val record = records[0]
        return when {
            record.toUri() != null -> parseURIRecord(record)
            record.tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(record.type, NdefRecord.RTD_TEXT) -> 
                parseTextRecord(record)
            record.tnf == NdefRecord.TNF_MIME_MEDIA -> parseMimeRecord(record)
            else -> parseUnknownRecord(record)
        }
    }
    
    /**
     * 瑙ｆ瀽URI璁板綍
     */
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
            NFCReadResult.Error("鏃犳硶瑙ｆ瀽URI璁板綍")
        }
    }
    
    /**
     * 瑙ｆ瀽鏂囨湰璁板綍
     */
    private fun parseTextRecord(record: NdefRecord): NFCReadResult {
        try {
            val payload = record.payload
            val textEncoding = if ((payload[0].toInt() and 0x80) == 0) "UTF-8" else "UTF-16"
            val languageCodeLength = payload[0].toInt() and 0x3F
            val text = String(
                payload,
                languageCodeLength + 1,
                payload.size - languageCodeLength - 1,
                Charset.forName(textEncoding)
            )
            return NFCReadResult.Success(NFCData(content = text, type = NFCType.TEXT))
        } catch (e: Exception) {
            Log.e(TAG, "瑙ｆ瀽鏂囨湰璁板綍澶辫触", e)
            return NFCReadResult.Error("瑙ｆ瀽鏂囨湰璁板綍澶辫触: ${e.message}")
        }
    }
    
    /**
     * 瑙ｆ瀽MIME璁板綍
     */
    private fun parseMimeRecord(record: NdefRecord): NFCReadResult {
        return try {
            val mimeType = String(record.type, Charset.forName("US-ASCII"))
            val payload = record.payload
            val content = String(payload, Charset.forName("UTF-8"))
            
            val type = NFCType.fromMimeType(mimeType)
            NFCReadResult.Success(NFCData(content = content, type = type))
        } catch (e: Exception) {
            Log.e(TAG, "瑙ｆ瀽MIME璁板綍澶辫触", e)
            NFCReadResult.Error("瑙ｆ瀽MIME璁板綍澶辫触: ${e.message}")
        }
    }
    
    /**
     * 瑙ｆ瀽鏈煡璁板綍
     */
    private fun parseUnknownRecord(record: NdefRecord): NFCReadResult {
        return try {
            val payload = record.payload
            val content = String(payload, Charset.forName("UTF-8"))
            NFCReadResult.Success(NFCData(content = content, type = NFCType.UNKNOWN))
        } catch (e: Exception) {
            NFCReadResult.Success(NFCData(
                content = "鏃犳硶瑙ｆ瀽鐨凬DEF璁板綍",
                type = NFCType.UNKNOWN
            ))
        }
    }
    
    /**
     * NFC璇诲彇缁撴灉
     */
    sealed class NFCReadResult {
        data class Success(val data: NFCData) : NFCReadResult()
        data class Error(val message: String) : NFCReadResult()
    }
    
    /**
     * NFC鐘舵€佹灇涓?
     */
    enum class NFCStatus {
        ENABLED,
        DISABLED,
        NOT_SUPPORTED
    }
}