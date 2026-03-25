package com.nfcmanager.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcmanager.MainActivity
import com.nfcmanager.R
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import com.nfcmanager.data.repository.NFCRepository
import com.nfcmanager.nfc.NFCManager
import com.nfcmanager.nfc.NFCEmulationService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val nfcRepository: NFCRepository,
    private val nfcManager: NFCManager,
    @ApplicationContext private val context: Context
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
    
    // NFC妯℃嫙鐩稿叧鐘舵€?
    private val _isEmulating = MutableStateFlow(false)
    val isEmulating: StateFlow<Boolean> = _isEmulating.asStateFlow()
    
    private val _currentEmulatingId = MutableStateFlow<String?>(null)
    val currentEmulatingId: StateFlow<String?> = _currentEmulatingId.asStateFlow()
    
    private val emulationPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(NFCEmulationService.PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    init {
        loadRecentNFCData()
        observeNFCData()
        observeNFCTags()
        loadEmulationState()
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
     * 鐩戝惉鏉ヨ嚜 MainActivity 鐨?NFC 鏍囩浜嬩欢
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
     * 鍔犺浇妯℃嫙鐘舵€?
     */
    private fun loadEmulationState() {
        _isEmulating.value = emulationPrefs.getBoolean("emulation_enabled", false)
        _currentEmulatingId.value = emulationPrefs.getString("emulating_data_id", null)
    }
    
    /**
     * 寮€濮婲FC鍗℃ā鎷?
     */
    fun startEmulation(nfcData: NFCData) {
        // 灏嗘暟鎹浆鎹负NDEF鏍煎紡骞朵繚瀛?
        val ndefData = createNDEFData(nfcData)
        
        emulationPrefs.edit()
            .putBoolean("emulation_enabled", true)
            .putString(NFCEmulationService.KEY_EMULATION_DATA, ndefData)
            .putString("emulating_data_id", nfcData.id)
            .apply()
        
        _isEmulating.value = true
        _currentEmulatingId.value = nfcData.id
    }
    
    /**
     * 鍋滄NFC鍗℃ā鎷?
     */
    fun stopEmulation() {
        emulationPrefs.edit()
            .putBoolean("emulation_enabled", false)
            .remove(NFCEmulationService.KEY_EMULATION_DATA)
            .remove("emulating_data_id")
            .apply()
        
        _isEmulating.value = false
        _currentEmulatingId.value = null
    }
    
    /**
     * 鍒涘缓NDEF鏍煎紡鐨勬暟鎹?
     */
    private fun createNDEFData(nfcData: NFCData): String {
        // 绠€鍖栫増锛氬皢鍐呭杞崲涓篘DEF鏍煎紡
        // 瀹為檯搴旂敤涓渶瑕佹牴鎹暟鎹被鍨嬪垱寤烘纭殑NDEF璁板綍
        val content = nfcData.content
        
        // 鍒涘缓NDEF鏂囨湰璁板綍
        val header = byteArrayOf(0xD1.toByte()) // TNF_WELL_KNOWN + SR + IL
        val typeLength = byteArrayOf(0x01) // "T" for text
        val payloadLength = byteArrayOf((content.toByteArray().size + 3).toByte())
        val type = byteArrayOf(0x54) // "T" for text
        val status = byteArrayOf(0x02) // UTF-8, language code length
        val language = byteArrayOf(0x65, 0x6E) // "en"
        
        val ndefRecord = header + typeLength + payloadLength + type + status + language + content.toByteArray()
        
        // NDEF鏂囦欢鏍煎紡锛氶暱搴?2瀛楄妭) + NDEF娑堟伅
        val length = byteArrayOf(
            ((ndefRecord.size shr 8) and 0xFF).toByte(),
            (ndefRecord.size and 0xFF).toByte()
        )
        
        val ndefFile = length + ndefRecord
        
        // 杞崲涓哄崄鍏繘鍒跺瓧绗︿覆
        return ndefFile.joinToString("") { "%02X".format(it) }
    }
    
    /**
     * 寮€濮嬫壂鎻忔ā寮?
     */
    fun startScanning() {
        _isScanning.value = true
        _scanResult.value = null
    }
    
    /**
     * 娓呴櫎鎵弿缁撴灉
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
                    errorMessage = context.getString(R.string.save_failed, e.message)
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
                    errorMessage = context.getString(R.string.update_failed, e.message)
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
                    errorMessage = context.getString(R.string.delete_failed, e.message)
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
                    errorMessage = context.getString(R.string.batch_delete_failed, e.message)
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
