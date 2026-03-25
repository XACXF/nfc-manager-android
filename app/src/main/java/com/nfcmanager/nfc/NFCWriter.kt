package com.nfcmanager.nfc

import android.content.Context
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import com.nfcmanager.data.model.NFCData
import java.io.IOException

/**
 * NFC写入器
 * 将NFC数据写入到空白NFC标签
 */
class NFCWriter(private val context: Context) {
    
    companion object {
        private const val TAG = "NFCWriter"
    }
    
    sealed class WriteResult {
        data class Success(val message: String) : WriteResult()
        data class Error(val message: String) : WriteResult()
    }
    
    /**
     * 将NFCData写入到Tag
     */
    fun writeToTag(tag: Tag, nfcData: NFCData): WriteResult {
        return try {
            // 优先使用保存的完整NDEF消息
            val ndefMessage = if (nfcData.ndefMessage != null) {
                NdefMessage(nfcData.ndefMessage)
            } else {
                createNDEFMessage(nfcData)
            }
            
            // 尝试写入已格式化的NDEF标签
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                return writeNdefTag(ndef, ndefMessage)
            }
            
            // 尝试格式化并写入空标签
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                return formatAndWriteTag(ndefFormatable, ndefMessage)
            }
            
            WriteResult.Error("此NFC标签不支持NDEF格式")
        } catch (e: Exception) {
            Log.e(TAG, "Write failed", e)
            WriteResult.Error("写入失败: ${e.message}")
        }
    }
    
    /**
     * 写入已格式化的NDEF标签
     */
    private fun writeNdefTag(ndef: Ndef, ndefMessage: NdefMessage): WriteResult {
        return try {
            ndef.connect()
            
            // 检查是否可写
            if (!ndef.isWritable) {
                ndef.close()
                return WriteResult.Error("此NFC标签已被锁定，无法写入")
            }
            
            // 检查容量
            if (ndef.maxSize < ndefMessage.byteArrayLength) {
                ndef.close()
                return WriteResult.Error("NFC标签容量不足，需要 ${ndefMessage.byteArrayLength} 字节")
            }
            
            // 写入NDEF消息
            ndef.writeNdefMessage(ndefMessage)
            ndef.close()
            
            Log.d(TAG, "Write successful: ${ndefMessage.byteArrayLength} bytes")
            WriteResult.Success("写入成功！共 ${ndefMessage.byteArrayLength} 字节")
        } catch (e: IOException) {
            Log.e(TAG, "IO Error", e)
            WriteResult.Error("写入失败: 无法连接到NFC标签")
        } catch (e: FormatException) {
            Log.e(TAG, "Format Error", e)
            WriteResult.Error("写入失败: NDEF格式错误")
        } catch (e: Exception) {
            Log.e(TAG, "Unknown Error", e)
            WriteResult.Error("写入失败: ${e.message}")
        }
    }
    
    /**
     * 格式化并写入空标签
     */
    private fun formatAndWriteTag(ndefFormatable: NdefFormatable, ndefMessage: NdefMessage): WriteResult {
        return try {
            ndefFormatable.connect()
            ndefFormatable.format(ndefMessage)
            ndefFormatable.close()
            
            Log.d(TAG, "Format and write successful")
            WriteResult.Success("格式化并写入成功！")
        } catch (e: IOException) {
            Log.e(TAG, "IO Error", e)
            WriteResult.Error("格式化失败: 无法连接到NFC标签")
        } catch (e: FormatException) {
            Log.e(TAG, "Format Error", e)
            WriteResult.Error("格式化失败: 标签不支持NDEF")
        } catch (e: Exception) {
            Log.e(TAG, "Unknown Error", e)
            WriteResult.Error("格式化失败: ${e.message}")
        }
    }
    
    /**
     * 创建NDEF消息
     */
    private fun createNDEFMessage(nfcData: NFCData): NdefMessage {
        val records = mutableListOf<NdefRecord>()
        
        // 添加主记录
        val mainRecord = when (nfcData.type) {
            com.nfcmanager.data.model.NFCType.URL -> NdefRecord.createUri(nfcData.content)
            com.nfcmanager.data.model.NFCType.PHONE -> NdefRecord.createUri("tel:${nfcData.content}")
            com.nfcmanager.data.model.NFCType.EMAIL -> NdefRecord.createUri("mailto:${nfcData.content}")
            com.nfcmanager.data.model.NFCType.GEO -> NdefRecord.createUri(nfcData.content)
            else -> NdefRecord.createTextRecord("en", nfcData.content)
        }
        records.add(mainRecord)
        
        // 添加AAR记录（如果有）
        if (!nfcData.aarPackage.isNullOrEmpty()) {
            records.add(NdefRecord.createApplicationRecord(nfcData.aarPackage))
        }
        
        return NdefMessage(records.toTypedArray())
    }
    
    /**
     * 检查Tag是否可写
     */
    fun isTagWritable(tag: Tag): Boolean {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val writable = ndef.isWritable
                ndef.close()
                return writable
            }
            
            // 如果支持格式化，也可以写入
            NdefFormatable.get(tag) != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取Tag信息
     */
    fun getTagInfo(tag: Tag): TagInfo {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val info = TagInfo(
                    type = "NDEF",
                    size = ndef.maxSize,
                    isWritable = ndef.isWritable,
                    techList = tag.techList.joinToString(", ")
                )
                ndef.close()
                return info
            }
            
            TagInfo(
                type = "Unknown",
                size = 0,
                isWritable = NdefFormatable.get(tag) != null,
                techList = tag.techList.joinToString(", ")
            )
        } catch (e: Exception) {
            TagInfo(
                type = "Error",
                size = 0,
                isWritable = false,
                techList = ""
            )
        }
    }
    
    data class TagInfo(
        val type: String,
        val size: Int,
        val isWritable: Boolean,
        val techList: String
    )
}