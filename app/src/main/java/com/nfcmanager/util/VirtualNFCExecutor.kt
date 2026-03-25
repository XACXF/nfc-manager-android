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
            // 优先使用保存的完整NDEF消息（如果有的话）
            val ndefMessage = if (nfcData.ndefMessage != null) {
                // 使用原始NDEF消息（最完整！）
                NdefMessage(nfcData.ndefMessage)
            } else {
                // 否则重新构建NDEF消息
                createNDEFMessage(nfcData)
            }
            
            // 创建NDEF_DISCOVERED Intent
            val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                // 设置正确的URI（光遇会检查这个）
                if (nfcData.type == NFCType.URL && nfcData.content.startsWith("http")) {
                    data = android.net.Uri.parse(nfcData.content)
                }
                
                // 设置NDEF消息（这是关键！）
                putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndefMessage))
                
                // 添加必要的flags
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            
            // 如果有AAR包名，直接用该包名打开
            if (nfcData.aarPackage != null) {
                intent.setPackage(nfcData.aarPackage)
                try {
                    context.startActivity(intent)
                    Log.d(TAG, "Started app with AAR package: ${nfcData.aarPackage}")
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start with AAR package: ${nfcData.aarPackage}", e)
                }
            }
            
            // 光遇链接特殊处理
            if (nfcData.content.contains("sky.thatg.co") || nfcData.content.contains("skygame.com")) {
                val skyPackages = listOf(
                    "com.tgc.sky.cn",      // 光遇国服
                    "com.tgc.sky.android"  // 光遇国际服
                )
                
                for (pkg in skyPackages) {
                    try {
                        intent.setPackage(pkg)
                        context.startActivity(intent)
                        Log.d(TAG, "Started Sky app with package: $pkg")
                        return true
                    } catch (e: Exception) {
                        Log.d(TAG, "Package $pkg not available: ${e.message}")
                        continue
                    }
                }
            }
            
            // 通用方式启动
            try {
                context.startActivity(intent)
                Log.d(TAG, "Virtual NFC Intent sent successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start activity", e)
                // 降级为直接执行
                actionExecutor.execute(nfcData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send virtual NFC intent", e)
            actionExecutor.execute(nfcData)
        }
    }
    
    /**
     * 根据NFC数据创建NDEF消息
     * 包含URL记录和AAR记录（如果有）
     */
    private fun createNDEFMessage(nfcData: NFCData): NdefMessage {
        val records = mutableListOf<NdefRecord>()
        
        // 添加主记录（URL、文本等）
        val mainRecord = when (nfcData.type) {
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
        records.add(mainRecord)
        
        // 如果有AAR包名，添加AAR记录（这是关键！）
        if (!nfcData.aarPackage.isNullOrEmpty()) {
            val aarRecord = NdefRecord.createApplicationRecord(nfcData.aarPackage)
            records.add(aarRecord)
            Log.d(TAG, "Added AAR record for package: ${nfcData.aarPackage}")
        }
        
        return NdefMessage(records.toTypedArray())
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
