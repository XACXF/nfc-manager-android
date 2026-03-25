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
    fun getAllNFCData(): Flow<List<NFCData>> = nfcDataDao.getAllNFCData()
    
    fun getRecentNFCData(limit: Int = 10): Flow<List<NFCData>> = nfcDataDao.getRecentNFCData(limit)
    
    suspend fun getNFCDataById(id: String): NFCData? = nfcDataDao.getNFCDataById(id)
    
    suspend fun insertNFCData(nfcData: NFCData) = nfcDataDao.insertNFCData(nfcData)
    
    suspend fun updateNFCData(nfcData: NFCData) = nfcDataDao.updateNFCData(nfcData)
    
    suspend fun deleteNFCData(nfcData: NFCData) = nfcDataDao.deleteNFCData(nfcData)
    
    fun searchNFCData(query: String): Flow<List<NFCData>> = nfcDataDao.searchNFCData(query)
    
    fun getNFCDataByType(type: NFCType): Flow<List<NFCData>> = nfcDataDao.getNFCDataByType(type)
    
    fun getNFCDataByDateRange(startDate: Date, endDate: Date): Flow<List<NFCData>> = 
        nfcDataDao.getNFCDataByDateRange(startDate, endDate)
    
    suspend fun getTotalCount(): Int = nfcDataDao.getTotalCount()
    
    suspend fun getCountByType(type: NFCType): Int = nfcDataDao.getCountByType(type)
    
    suspend fun insertMultipleNFCData(dataList: List<NFCData>) {
        dataList.forEach { nfcDataDao.insertNFCData(it) }
    }
    
    suspend fun deleteMultipleNFCData(dataList: List<NFCData>) {
        dataList.forEach { nfcDataDao.deleteNFCData(it) }
    }
    
    suspend fun deleteAllNFCData() = nfcDataDao.deleteAllNFCData()
}
