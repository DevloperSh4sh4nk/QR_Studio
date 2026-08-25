package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.QrRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface QrRecordDao {

    @Query("SELECT * FROM qr_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<QrRecord>>

    @Query("SELECT * FROM qr_records WHERE type = :type ORDER BY timestamp DESC")
    fun getRecordsByType(type: String): Flow<List<QrRecord>>

    @Query("SELECT * FROM qr_records WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteRecords(): Flow<List<QrRecord>>

    @Query("SELECT * FROM qr_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): QrRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: QrRecord): Long

    @Update
    suspend fun updateRecord(record: QrRecord)

    @Delete
    suspend fun deleteRecord(record: QrRecord)

    @Query("DELETE FROM qr_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM qr_records WHERE type = :type")
    suspend fun clearByType(type: String)
}
