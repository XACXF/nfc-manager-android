package com.nfcmanager

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nfcmanager.ui.theme.NFCManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        
        // NFC标签读取结果流
        private val _nfcTagFlow = MutableStateFlow<Tag?>(null)
        val nfcTagFlow: StateFlow<Tag?> = _nfcTagFlow.asStateFlow()
    }
    
    private var nfcAdapter: NfcAdapter? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        setContent {
            NFCManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NFCApp()
                }
            }
        }
        
        // 处理启动时的NFC Intent
        handleNFCIntent(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // 启用前台调度，让应用在运行时优先接收NFC事件
        nfcAdapter?.enableReaderMode(
            this,
            { tag ->
                Log.d(TAG, "NFC Tag detected!")
                _nfcTagFlow.value = tag
            },
            NfcAdapter.FLAG_READER_NFC_A or 
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NDEF,
            null
        )
    }
    
    override fun onPause() {
        super.onPause()
        // 禁用前台调度
        nfcAdapter?.disableReaderMode(this)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: $intent")
        intent?.let { handleNFCIntent(it) }
    }
    
    private fun handleNFCIntent(intent: Intent) {
        Log.d(TAG, "handleNFCIntent: action=${intent.action}")
        
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                val tag = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }
                
                tag?.let {
                    Log.d(TAG, "NFC Tag detected from intent: ${it.id.joinToString()}")
                    _nfcTagFlow.value = it
                }
            }
        }
    }
}
