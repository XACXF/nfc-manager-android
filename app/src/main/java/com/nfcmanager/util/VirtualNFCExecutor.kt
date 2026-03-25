package com.nfcmanager.util

import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.util.Log
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import java.nio.charset.Charset

/**
 * 铏氭嫙NFC鎵ц鍣?
 * 閫氳繃鍙戦€佽櫄鎷烴FC Intent鏉ユā鎷熷埛鍗℃晥鏋?
 */
class VirtualNFCExecutor(private val context: Context) {
    
    companion object {
        private const val TAG = "VirtualNFCExecutor"
    }
    
    private val actionExecutor = NFCActionExecutor(context)
    
    /**
     * 鎵ц铏氭嫙NFC鎿嶄綔
     * 鏂瑰紡1锛氬彂閫佽櫄鎷烮ntent锛堝彲琚叾浠朅pp鎺ユ敹锛?
     * 鏂瑰紡2锛氱洿鎺ユ墽琛屾搷浣滐紙鏇村揩鏇村彲闈狅級
     */
    fun executeVirtualNFC(nfcData: NFCData, useIntent: Boolean = false): Boolean {
        return if (useIntent) {
            // 鏂瑰紡1锛氬彂閫佽櫄鎷烴FC Intent
            sendVirtualNFCIntent(nfcData)
        } else {
            // 鏂瑰紡2锛氱洿鎺ユ墽琛屾搷浣滐紙鎺ㄨ崘锛?
            actionExecutor.execute(nfcData)
        }
    }
    
    /**
     * 鍙戦€佽櫄鎷烴FC Intent
     * 杩欎細璁╃郴缁熷拰鍏朵粬App浠ヤ负鐪熺殑鏀跺埌浜哊FC鏍囩
     */
    private fun sendVirtualNFCIntent(nfcData: NFCData): Boolean {
        return try {
            val ndefMessage = createNDEFMessage(nfcData)
            
            // 鍙戦€丯DEF_DISCOVERED Intent
            val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndefMessage))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.sendBroadcast(intent)
            Log.d(TAG, "Virtual NFC Intent sent for type: ${nfcData.type}")
            
            // 鍚屾椂涔熸墽琛屽疄闄呮搷浣?
            actionExecutor.execute(nfcData)
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send virtual NFC intent", e)
            // 闄嶇骇涓虹洿鎺ユ墽琛?
            actionExecutor.execute(nfcData)
        }
    }
    
    /**
     * 鏍规嵁NFC鏁版嵁鍒涘缓NDEF娑堟伅
     */
    private fun createNDEFMessage(nfcData: NFCData): NdefMessage {
        val record = when (nfcData.type) {
            NFCType.URL -> createUriRecord(nfcData.content)
            NFCType.PHONE -> createUriRecord("tel:${nfcData.content}")
            NFCType.EMAIL -> createUriRecord("mailto:${nfcData.content}")
            NFCType.GEO -> createUriRecord(nfcData.content)
            NFCType.TEXT -> createTextRecord(nfcData.content)
            NFCType.WIFI -> createMimeRecord("application/vnd.wfa.wsc", nfcData.content)
            NFCType.VCARD -> createMimeRecord("text/vcard", nfcData.content)
            NFCType.APP -> createUriRecord(nfcData.content)
            NFCType.UNKNOWN -> createTextRecord(nfcData.content)
        }
        
        return NdefMessage(record)
    }
    
    /**
     * 鍒涘缓URI绫诲瀷鐨凬DEF璁板綍
     */
    private fun createUriRecord(uri: String): NdefRecord {
        return NdefRecord.createUri(uri)
    }
    
    /**
     * 鍒涘缓鏂囨湰绫诲瀷鐨凬DEF璁板綍
     */
    private fun createTextRecord(text: String): NdefRecord {
        val langBytes = "en".toByteArray(Charset.forName("US-ASCII"))
        val textBytes = text.toByteArray(Charset.forName("UTF-8"))
        val payload = ByteArray(1 + langBytes.size + textBytes.size)
        
        payload[0] = langBytes.size.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + langBytes.size, textBytes.size)
        
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }
    
    /**
     * 鍒涘缓MIME绫诲瀷鐨凬DEF璁板綍
     */
    private fun createMimeRecord(mimeType: String, content: String): NdefRecord {
        return NdefRecord.createMime(mimeType, content.toByteArray(Charset.forName("UTF-8")))
    }
    
    /**
     * 鑾峰彇鎿嶄綔鎻忚堪
     */
    fun getActionDescription(nfcData: NFCData): String {
        return actionExecutor.getActionDescription(nfcData.type)
    }
    
    /**
     * 鎵ц蹇嵎鎿嶄綔锛堜笉鍙戦€両ntent锛岀洿鎺ユ墽琛岋級
     */
    fun quickExecute(nfcData: NFCData): Boolean {
        return actionExecutor.execute(nfcData)
    }
}
