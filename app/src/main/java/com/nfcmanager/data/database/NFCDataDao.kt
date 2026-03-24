package com.nfcmanager.data.database

import androidx.room.*
import com.nfcmanager.data.model.NFCData
import com.nfcmanager.data.model.NFCType
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface NFCDataDao {
    @Query("SELECT * FROM nfc_data ORDER BY readTime DESC")
    fun getAllNFCData(): Flow<List<NFCData>>
    
    @Query("SELECT * FROM nfc_data WHERE id = :id")
    suspend fun getNFCDataById(id: String): NFCData?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNFCData(nfcData: NFCData)
    
    @Update
    suspend fun updateNFCData(nfcData: NFCData)
    
    @Delete
    suspend fun deleteNFCData(nfcData: NFCData)
    
    @Query("DELETE FROM nfc_data WHERE id = :id")
    suspend fun deleteNFCDataById(id: String)
    
    @Query("DELETE FROM nfc_data")
    suspend fun deleteAllNFCData()
    
    // 搜索功能
    @Query("SELECT * FROM nfc_data WHERE content LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY readTime DESC")
    fun searchNFCData(query: String): Flow<List<NFCData>>
    
    // 按类型筛选
    @Query("SELECT * FROM nfc_data WHERE type = :type ORDER BY readTime DESC")
    fun getNFCDataByType(type: NFCType): Flow<List<NFCData>>
    
    // 按时间范围筛选
    @Query("SELECT * FROM nfc_data WHERE readTime BETWEEN :startDate AND :endDate ORDER BY readTime DESC")
    fun getNFCDataByDateRange(startDate: Date, endDate: Date): Flow<List<NFCData>>
    
    // 获取最近的数据
    @Query("SELECT * FROM nfc_data ORDER BY readTime DESC LIMIT :limit")
    fun getRecentNFCData(limit: Int): Flow<List<NFCData>>
    
    // 统计
    @Query("SELECT COUNT(*) FROM nfc_data")
    suspend fun getTotalCount(): Int
    
    @Query("SELECT COUNT(*) FROM nfc_data WHERE type = :type")
    suspend fun getCountByType(type: NFCType): Int
}