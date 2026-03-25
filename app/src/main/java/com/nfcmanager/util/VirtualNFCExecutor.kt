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
     * 完全模拟系统NFC扫描的Intent格式
     */
    private fun sendVirtualNFCIntent(nfcData: NFCData): Boolean {
        return try {
            // 优先使用保存的完整NDEF消息
            val ndefMessage = if (nfcData.ndefMessage != null) {
                NdefMessage(nfcData.ndefMessage)
            } else {
                createNDEFMessage(nfcData)
            }
            
            // 尝试方式1：模拟完整NFC Intent
            val nfcIntent = createCompleteNFCIntent(nfcData, ndefMessage)
            
            // 确定目标包名
            val targetPackage = findTargetPackage(nfcData)
            
            if (targetPackage != null) {
                nfcIntent.setPackage(targetPackage)
                
                // 尝试启动
                try {
                    context.startActivity(nfcIntent)
                    Log.d(TAG, "Started app with NFC intent: $targetPackage")
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "NFC intent failed: ${e.message}")
                }
            }
            
            // 尝试方式2：直接用URI打开（深度链接）
            if (nfcData.type == NFCType.URL && nfcData.content.startsWith("http")) {
                try {
                    val uriIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(nfcData.content)).apply {
                        if (targetPackage != null) {
                            setPackage(targetPackage)
                        }
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(uriIntent)
                    Log.d(TAG, "Started app with URI intent")
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "URI intent failed: ${e.message}")
                }
            }
            
            // 降级为普通执行
            actionExecutor.execute(nfcData)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send virtual NFC intent", e)
            actionExecutor.execute(nfcData)
        }
    }
    
    /**
     * 创建完整的NFC Intent
     */
    private fun createCompleteNFCIntent(nfcData: NFCData, ndefMessage: NdefMessage): Intent {
        return Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            // 设置URI data
            if (nfcData.type == NFCType.URL && nfcData.content.startsWith("http")) {
                data = android.net.Uri.parse(nfcData.content)
            }
            
            // 添加所有NFC extras
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndefMessage))
            
            // 添加Tag ID
            if (nfcData.rawData != null) {
                putExtra(NfcAdapter.EXTRA_ID, nfcData.rawData)
            }
            
            // 关键flags
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
            
            // 添加category
            addCategory(Intent.CATEGORY_DEFAULT)
        }
    }
    
    /**
     * 查找目标包名
     */
    private fun findTargetPackage(nfcData: NFCData): String? {
        // 如果有AAR包名，直接使用
        if (!nfcData.aarPackage.isNullOrEmpty()) {
            return nfcData.aarPackage
        }
        
        // 光遇链接特殊处理
        if (nfcData.content.contains("sky.thatg.co") || nfcData.content.contains("skygame.com")) {
            return findSkyPackage()
        }
        
        return null
    }
    
    /**
     * 查找已安装的光遇包名
     */
    private fun findSkyPackage(): String? {
        val skyPackages = listOf(
            "com.tgc.sky.cn",      // 光遇国服
            "com.tgc.sky.android"  // 光遇国际服
        )
        
        val pm = context.packageManager
        for (pkg in skyPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                Log.d(TAG, "Found Sky package: $pkg")
                return pkg
            } catch (e: Exception) {
                // 包不存在，继续尝试下一个
            }
        }
        return null
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
