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
    // 获取所有数据
    fun getAllNFCData(): Flow<List<NFCData>> = nfcDataDao.getAllNFCData()
    
    // 获取最近数据
    fun getRecentNFCData(limit: Int = 10): Flow<List<NFCData>> = nfcDataDao.getRecentNFCData(limit)
    
    // 根据ID获取数据
    suspend fun getNFCDataById(id: String): NFCData? = nfcDataDao.getNFCDataById(id)
    
    // 插入数据
    suspend fun insertNFCData(nfcData: NFCData) = nfcDataDao.insertNFCData(nfcData)
    
    // 更新数据
    suspend fun updateNFCData(nfcData: NFCData) = nfcDataDao.updateNFCData(nfcData)
    
    // 删除数据
    suspend fun deleteNFCData(nfcData: NFCData) = nfcDataDao.deleteNFCData(nfcData)
    
    // 搜索数据
    fun searchNFCData(query: String): Flow<List<NFCData>> = nfcDataDao.searchNFCData(query)
    
    // 按类型筛选数据
    fun getNFCDataByType(type: NFCType): Flow<List<NFCData>> = nfcDataDao.getNFCDataByType(type)
    
    // 按时间范围筛选数据
    fun getNFCDataByDateRange(startDate: Date, endDate: Date): Flow<List<NFCData>> = 
        nfcDataDao.getNFCDataByDateRange(startDate, endDate)
    
    // 统计数据
    suspend fun getTotalCount(): Int = nfcDataDao.getTotalCount()
    suspend fun getCountByType(type: NFCType): Int = nfcDataDao.getCountByType(type)
    
    // 批量操作
    suspend fun insertMultipleNFCData(dataList: List<NFCData>) {
        dataList.forEach { nfcDataDao.insertNFCData(it) }
    }
    
    suspend fun deleteMultipleNFCData(dataList: List<NFCData>) {
        dataList.forEach { nfcDataDao.deleteNFCData(it) }
    }
    
    // 清空所有数据
    suspend fun deleteAllNFCData() = nfcDataDao.deleteAllNFCData()
}