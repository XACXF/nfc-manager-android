package com.nfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcmanager.MainActivity
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import com.nfcmanager.data.repository.NFCRepository
import com.nfcmanager.nfc.NFCManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val nfcRepository: NFCRepository,
    private val nfcManager: NFCManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()
    
    private val _nfcStatus = MutableStateFlow(nfcManager.getNFCStatus())
    val nfcStatus: StateFlow<NFCManager.NFCStatus> = _nfcStatus.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filterType = MutableStateFlow<NFCType?>(null)
    val filterType: StateFlow<NFCType?> = _filterType.asStateFlow()
    
    private val _scanResult = MutableStateFlow<NFCManager.NFCReadResult?>(null)
    val scanResult: StateFlow<NFCManager.NFCReadResult?> = _scanResult.asStateFlow()
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    init {
        loadRecentNFCData()
        observeNFCData()
        observeNFCTags()
    }
    
    private fun loadRecentNFCData() {
        viewModelScope.launch {
            nfcRepository.getRecentNFCData(10).collect { dataList ->
                _uiState.update { it.copy(recentData = dataList) }
            }
        }
    }
    
    private fun observeNFCData() {
        viewModelScope.launch {
            nfcRepository.getAllNFCData().collect { dataList ->
                _uiState.update { it.copy(allData = dataList) }
            }
        }
    }
    
    /**
     * 监听来自 MainActivity 的 NFC 标签事件
     */
    private fun observeNFCTags() {
        viewModelScope.launch {
            MainActivity.nfcTagFlow.collect { tag ->
                tag?.let {
                    _isScanning.value = true
                    val result = nfcManager.readNFCTag(it)
                    _scanResult.value = result
                    _isScanning.value = false
                }
            }
        }
    }
    
    /**
     * 开始扫描模式
     */
    fun startScanning() {
        _isScanning.value = true
        _scanResult.value = null
    }
    
    /**
     * 清除扫描结果
     */
    fun clearScanResult() {
        _scanResult.value = null
        _isScanning.value = false
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                nfcRepository.searchNFCData(query).collect { searchResults ->
                    _uiState.update { it.copy(searchResults = searchResults) }
                }
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }
    
    fun updateFilterType(type: NFCType?) {
        _filterType.value = type
        if (type != null) {
            viewModelScope.launch {
                nfcRepository.getNFCDataByType(type).collect { filteredData ->
                    _uiState.update { it.copy(filteredData = filteredData) }
                }
            }
        } else {
            _uiState.update { it.copy(filteredData = emptyList()) }
        }
    }
    
    fun saveNFCData(nfcData: NFCData) {
        viewModelScope.launch {
            try {
                nfcRepository.insertNFCData(nfcData)
                _uiState.update { it.copy(
                    showSaveSuccess = true,
                    lastSavedData = nfcData
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Save failed: ${e.message}"
                ) }
            }
        }
    }
    
    fun updateNFCData(nfcData: NFCData) {
        viewModelScope.launch {
            try {
                nfcRepository.updateNFCData(nfcData)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Update failed: ${e.message}"
                ) }
            }
        }
    }
    
    fun deleteNFCData(nfcData: NFCData) {
        viewModelScope.launch {
            try {
                nfcRepository.deleteNFCData(nfcData)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Delete failed: ${e.message}"
                ) }
            }
        }
    }
    
    fun deleteMultipleNFCData(dataList: List<NFCData>) {
        viewModelScope.launch {
            try {
                nfcRepository.deleteMultipleNFCData(dataList)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "Batch delete failed: ${e.message}"
                ) }
            }
        }
    }
    
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSaveSuccess() {
        _uiState.update { it.copy(showSaveSuccess = false) }
    }
    
    fun refreshNFCStatus() {
        _nfcStatus.value = nfcManager.getNFCStatus()
    }
    
    fun getDisplayData(): List<NFCData> {
        return when {
            _searchQuery.value.isNotEmpty() -> _uiState.value.searchResults
            _filterType.value != null -> _uiState.value.filteredData
            else -> _uiState.value.allData
        }
    }
    
    suspend fun getStatistics(): Statistics {
        val totalCount = nfcRepository.getTotalCount()
        val typeCounts = NFCType.values().associateWith { type ->
            nfcRepository.getCountByType(type)
        }
        
        return Statistics(
            totalCount = totalCount,
            typeCounts = typeCounts
        )
    }
}

data class MainUIState(
    val recentData: List<NFCData> = emptyList(),
    val allData: List<NFCData> = emptyList(),
    val searchResults: List<NFCData> = emptyList(),
    val filteredData: List<NFCData> = emptyList(),
    val showSaveSuccess: Boolean = false,
    val lastSavedData: NFCData? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

data class Statistics(
    val totalCount: Int,
    val typeCounts: Map<NFCType, Int>
)
