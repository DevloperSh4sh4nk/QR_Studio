package com.example.data.repository

import com.example.data.local.dao.QrRecordDao
import com.example.data.local.entity.QrRecord
import kotlinx.coroutines.flow.Flow

class QrRepository(private val dao: QrRecordDao) {

    val allRecords: Flow<List<QrRecord>> = dao.getAllRecords()
    val favoriteRecords: Flow<List<QrRecord>> = dao.getFavoriteRecords()

    fun getRecordsByType(type: String): Flow<List<QrRecord>> = dao.getRecordsByType(type)

    suspend fun getRecordById(id: Long): QrRecord? = dao.getRecordById(id)

    suspend fun insertRecord(record: QrRecord): Long = dao.insertRecord(record)

    suspend fun updateRecord(record: QrRecord) = dao.updateRecord(record)

    suspend fun deleteRecord(record: QrRecord) = dao.deleteRecord(record)

    suspend fun deleteRecordById(id: Long) = dao.deleteRecordById(id)

    suspend fun toggleFavorite(record: QrRecord) {
        dao.updateRecord(record.copy(isFavorite = !record.isFavorite))
    }

    suspend fun clearByType(type: String) = dao.clearByType(type)
}
