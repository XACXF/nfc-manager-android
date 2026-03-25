package com.nfcmanager.nfc

import android.content.SharedPreferences
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.nio.charset.Charset

/**
 * NFC鍗℃ā鎷熸湇鍔?
 * 璁╂墜鏈烘ā鎷熸垚NFC鍗＄墖锛岃鍏朵粬璇诲崱鍣ㄨ鍙?
 */
class NFCEmulationService : HostApduService() {
    
    companion object {
        private const val TAG = "NFCEmulationService"
        const val PREFS_NAME = "nfc_emulation_prefs"
        const val KEY_EMULATION_DATA = "emulation_data"
        const val KEY_EMULATION_TYPE = "emulation_type"
        
        // NDEF鐩稿叧甯搁噺
        private val NDEF_CAPABILITY_CONTAINER = byteArrayOf(
            0x00, 0x0F, 0x20, 0x00, 0x3B, 0x00, 0x34, 0x04,
            0x06, 0xE1, 0x04, 0x00, 0xFF, 0x00, 0x00, 0x00
        )
        
        // APDU鍛戒护
        private val SELECT_APDU_HEADER = byteArrayOf(
            0x00, 0xA4.toByte(), 0x04, 0x00
        )
        
        // AID for NDEF Tag Application
        private val NDEF_AID = byteArrayOf(
            0xD2.toByte(), 0x76, 0x00, 0x00, 0x85, 0x01, 0x01
        )
        
        // NDEF鏂囦欢鎺у埗TLV
        private val NDEF_FILE_CONTROL_TLV = byteArrayOf(
            0x04, 0x06, 0xE1, 0x04, 0x00, 0xFF.toByte(), 0x00, 0x00
        )
    }
    
    private var ndefData: ByteArray = byteArrayOf()
    private var emulationEnabled = false
    
    override fun onCreate() {
        super.onCreate()
        loadEmulationData()
    }
    
    private fun loadEmulationData() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val dataHex = prefs.getString(KEY_EMULATION_DATA, null)
        emulationEnabled = prefs.getBoolean("emulation_enabled", false)
        
        if (dataHex != null && emulationEnabled) {
            ndefData = hexStringToByteArray(dataHex)
            Log.d(TAG, "Loaded emulation data: ${ndefData.size} bytes")
        } else {
            // 榛樿绌篘DEF娑堟伅
            ndefData = createEmptyNDEFMessage()
        }
    }
    
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (!emulationEnabled || commandApdu == null) {
            return byteArrayOf(0x6A, 0x82.toByte()) // File not found
        }
        
        Log.d(TAG, "Received APDU: ${bytesToHex(commandApdu)}")
        
        return when {
            // SELECT鍛戒护 - 閫夋嫨NDEF搴旂敤
            isSelectCommand(commandApdu) -> {
                Log.d(TAG, "SELECT command received")
                byteArrayOf(0x90.toByte(), 0x00) // Success
            }
            
            // 璇诲彇Capability Container
            isReadCapabilityContainer(commandApdu) -> {
                Log.d(TAG, "Read CC command")
                NDEF_CAPABILITY_CONTAINER + byteArrayOf(0x90.toByte(), 0x00)
            }
            
            // 璇诲彇NDEF鏂囦欢
            isReadNDEFFile(commandApdu) -> {
                Log.d(TAG, "Read NDEF command")
                ndefData + byteArrayOf(0x90.toByte(), 0x00)
            }
            
            else -> {
                Log.d(TAG, "Unknown command")
                byteArrayOf(0x6A, 0x86.toByte()) // Wrong parameters
            }
        }
    }
    
    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }
    
    // 妫€娴婼ELECT鍛戒护
    private fun isSelectCommand(apdu: ByteArray): Boolean {
        if (apdu.size < 12) return false
        if (!apdu.sliceArray(0..3).contentEquals(SELECT_APDU_HEADER)) return false
        return apdu.sliceArray(5..11).contentEquals(NDEF_AID)
    }
    
    // 妫€娴嬭鍙朇apability Container
    private fun isReadCapabilityContainer(apdu: ByteArray): Boolean {
        return apdu.size >= 5 &&
                apdu[0] == 0x00.toByte() &&
                apdu[1] == 0xB0.toByte() &&
                apdu[2] == 0x00.toByte() &&
                apdu[3] == 0x00.toByte()
    }
    
    // 妫€娴嬭鍙朜DEF鏂囦欢
    private fun isReadNDEFFile(apdu: ByteArray): Boolean {
        return apdu.size >= 5 &&
                apdu[0] == 0x00.toByte() &&
                apdu[1] == 0xB0.toByte()
    }
    
    // 鍒涘缓绌篘DEF娑堟伅
    private fun createEmptyNDEFMessage(): ByteArray {
        return byteArrayOf(
            0x00, 0x00, // NDEF鏂囦欢闀垮害
            0x00, 0x00  // 绌篘DEF娑堟伅
        )
    }
    
    // 鍗佸叚杩涘埗瀛楃涓茶浆瀛楄妭鏁扮粍
    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) +
                    Character.digit(s[i + 1], 16)).toByte()
        }
        return data
    }
    
    // 瀛楄妭鏁扮粍杞崄鍏繘鍒跺瓧绗︿覆
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = HEX_CHARS[v ushr 4]
            hexChars[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(hexChars)
    }
    
    private val HEX_CHARS = "0123456789ABCDEF".toCharArray()
}
