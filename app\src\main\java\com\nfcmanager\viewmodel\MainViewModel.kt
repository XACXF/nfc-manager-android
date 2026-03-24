package com.nfcmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import com.nfcmanager.data.repository.NFCRepository
import com.nfcmanager.nfc.NFCManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val nfcRepository: NFCRepository,
    private val nfcManager: NFCManager
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()
    
    // NFC状态
    private val _nfcStatus = MutableStateFlow(nfcManager.getNFCStatus())
    val nfcStatus: StateFlow<NFCManager.NFCStatus> = _nfcStatus.asStateFlow()
    
    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 筛选类型
    private val _filterType = MutableStateFlow<NFCType?>(null)
    val filterType: StateFlow<NFCType?> = _filterType.asStateFlow()
    
    init {
        loadRecentNFCData()
        observeNFCData()
    }
    
    /**
     * 加载最近的数据
     */
    private fun loadRecentNFCData() {
        viewModelScope.launch {
            nfcRepository.getRecentNFCData(10).collect { dataList ->
                _uiState.update { it.copy(recentData = dataList) }
            }
        }
    }
    
    /**
     * 监听NFC数据变化
     */
    private fun observeNFCData() {
        viewModelScope.launch {
            nfcRepository.getAllNFCData().collect { dataList ->
                _uiState.update { it.copy(allData = dataList) }
            }
        }
    }
    
    /**
     * 更新搜索查询
     */
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
    
    /**
     * 更新筛选类型
     */
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
    
    /**
     * 保存NFC数据
     */
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
                    errorMessage = "保存失败: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * 更新NFC数据
     */
    fun updateNFCData(nfcData: NFCData) {
        viewModelScope.launch {
            try {
                nfcRepository.updateNFCData(nfcData)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "更新失败: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * 删除NFC数据
     */
    fun deleteNFCData(nfcData: NFCData) {
        viewModelScope.launch {
            try {
                nfcRepository.deleteNFCData(nfcData)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "删除失败: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * 批量删除NFC数据
     */
    fun deleteMultipleNFCData(dataList: List<NFCData>) {
        viewModelScope.launch {
            try {
                nfcRepository.deleteMultipleNFCData(dataList)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    errorMessage = "批量删除失败: ${e.message}"
                ) }
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * 清除保存成功消息
     */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(showSaveSuccess = false) }
    }
    
    /**
     * 刷新NFC状态
     */
    fun refreshNFCStatus() {
        _nfcStatus.value = nfcManager.getNFCStatus()
    }
    
    /**
     * 获取显示的数据列表
     */
    fun getDisplayData(): List<NFCData> {
        return when {
            _searchQuery.value.isNotEmpty() -> _uiState.value.searchResults
            _filterType.value != null -> _uiState.value.filteredData
            else -> _uiState.value.allData
        }
    }
    
    /**
     * 获取统计数据
     */
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

/**
 * 主界面UI状态
 */
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

/**
 * 统计数据
 */
data class Statistics(
    val totalCount: Int,
    val typeCounts: Map<NFCType, Int>
)