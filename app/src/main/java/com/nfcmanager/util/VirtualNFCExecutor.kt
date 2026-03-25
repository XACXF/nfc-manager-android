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
 * 虚拟NFC执行器
 * 通过发送虚拟NFC Intent来模拟刷卡效果
 */
class VirtualNFCExecutor(private val context: Context) {
    
    companion object {
        private const val TAG = "VirtualNFCExecutor"
    }
    
    private val actionExecutor = NFCActionExecutor(context)
    
    /**
     * 执行虚拟NFC操作
     * 方式1：发送虚拟Intent（可被其他App接收）
     * 方式2：直接执行操作（更快更可靠）
     */
    fun executeVirtualNFC(nfcData: NFCData, useIntent: Boolean = false): Boolean {
        return if (useIntent) {
            // 方式1：发送虚拟NFC Intent
            sendVirtualNFCIntent(nfcData)
        } else {
            // 方式2：直接执行操作（推荐）
            actionExecutor.execute(nfcData)
        }
    }
    
    /**
     * 发送虚拟NFC Intent
     * 这会让系统和其他App以为真的收到了NFC标签
     */
    private fun sendVirtualNFCIntent(nfcData: NFCData): Boolean {
        return try {
            val ndefMessage = createNDEFMessage(nfcData)
            
            // 发送NDEF_DISCOVERED Intent
            val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndefMessage))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.sendBroadcast(intent)
            Log.d(TAG, "Virtual NFC Intent sent for type: ${nfcData.type}")
            
            // 同时也执行实际操作
            actionExecutor.execute(nfcData)
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send virtual NFC intent", e)
            // 降级为直接执行
            actionExecutor.execute(nfcData)
        }
    }
    
    /**
     * 根据NFC数据创建NDEF消息
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
     * 创建URI类型的NDEF记录
     */
    private fun createUriRecord(uri: String): NdefRecord {
        return NdefRecord.createUri(uri)
    }
    
    /**
     * 创建文本类型的NDEF记录
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
     * 创建MIME类型的NDEF记录
     */
    private fun createMimeRecord(mimeType: String, content: String): NdefRecord {
        return NdefRecord.createMime(mimeType, content.toByteArray(Charset.forName("UTF-8")))
    }
    
    /**
     * 获取操作描述
     */
    fun getActionDescription(nfcData: NFCData): String {
        return actionExecutor.getActionDescription(nfcData.type)
    }
    
    /**
     * 执行快捷操作（不发送Intent，直接执行）
     */
    fun quickExecute(nfcData: NFCData): Boolean {
        return actionExecutor.execute(nfcData)
    }
}
