package com.nfcmanager.data.repository

import com.nfcmanager.data.database.NFCDataDao
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class NFCRepository @Inject constructor(
    private val nfcDataDao: NFCDataDao
) {
    // 鑾峰彇鎵€鏈夋暟鎹?
    fun getAllNFCData(): Flow<List<NFCData>> = nfcDataDao.getAllNFCData()
    
    // 鑾峰彇鏈€杩戞暟鎹?
    fun getRecentNFCData(limit: Int = 10): Flow<List<NFCData>> = nfcDataDao.getRecentNFCData(limit)
    
    // 鏍规嵁ID鑾峰彇鏁版嵁
    suspend fun getNFCDataById(id: String): NFCData? = nfcDataDao.getNFCDataById(id)
    
    // 鎻掑叆鏁版嵁
    suspend fun insertNFCData(nfcData: NFCData) = nfcDataDao.insertNFCData(nfcData)
    
    // 鏇存柊鏁版嵁
    suspend fun updateNFCData(nfcData: NFCData) = nfcDataDao.updateNFCData(nfcData)
    
    // 鍒犻櫎鏁版嵁
    suspend fun deleteNFCData(nfcData: NFCData) = nfcDataDao.deleteNFCData(nfcData)
    
    // 鎼滅储鏁版嵁
    fun searchNFCData(query: String): Flow<List<NFCData>> = nfcDataDao.searchNFCData(query)
    
    // 鎸夌被鍨嬬瓫閫夋暟鎹?
    fun getNFCDataByType(type: NFCType): Flow<List<NFCData>> = nfcDataDao.getNFCDataByType(type)
    
    // 鎸夋椂闂磋寖鍥寸瓫閫夋暟鎹?
    fun getNFCDataByDateRange(startDate: Date, endDate: Date): Flow<List<NFCData>> = 
        nfcDataDao.getNFCDataByDateRange(startDate, endDate)
    
    // 缁熻鏁版嵁
    suspend fun getTotalCount(): Int = nfcDataDao.getTotalCount()
    suspend fun getCountByType(type: NFCType): Int = nfcDataDao.getCountByType(type)
    
    // 鎵归噺鎿嶄綔
    suspend fun insertMultipleNFCData(dataList: List<NFCData>) {
        dataList.forEach { nfcDataDao.insertNFCData(it) }
    }
    
    suspend fun deleteMultipleNFCData(dataList: List<NFCData>) {
        dataList.forEach { nfcDataDao.deleteNFCData(it) }
    }
    
    // 娓呯┖鎵€鏈夋暟鎹?
    suspend fun deleteAllNFCData() = nfcDataDao.deleteAllNFCData()
}