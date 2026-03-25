package com.nfcmanager.viewmodel

import android.nfc.Tag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcmanager.MainActivity
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.nfc.NFCWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val nfcWriter: NFCWriter
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WriteUIState())
    val uiState: StateFlow<WriteUIState> = _uiState.asStateFlow()
    
    private val _writeResult = MutableStateFlow<NFCWriter.WriteResult?>(null)
    val writeResult: StateFlow<NFCWriter.WriteResult?> = _writeResult.asStateFlow()
    
    init {
        observeNFCTags()
    }
    
    private fun observeNFCTags() {
        viewModelScope.launch {
            MainActivity.nfcTagFlow.collect { tag ->
                tag?.let {
                    handleTagDiscovered(it)
                }
            }
        }
    }
    
    fun setDataToWrite(data: NFCData) {
        _uiState.update { it.copy(dataToWrite = data) }
    }
    
    fun startWriteScan() {
        _uiState.update { it.copy(isScanning = true) }
        _writeResult.value = null
    }
    
    private fun handleTagDiscovered(tag: Tag) {
        val dataToWrite = _uiState.value.dataToWrite ?: return
        
        // 获取标签信息
        val tagInfo = nfcWriter.getTagInfo(tag)
        _uiState.update { it.copy(tagInfo = tagInfo) }
        
        // 执行写入
        val result = nfcWriter.writeToTag(tag, dataToWrite)
        _writeResult.value = result
        _uiState.update { it.copy(isScanning = false) }
    }
    
    fun clearResult() {
        _writeResult.value = null
    }
}

data class WriteUIState(
    val dataToWrite: NFCData? = null,
    val isScanning: Boolean = false,
    val tagInfo: NFCWriter.TagInfo? = null
)