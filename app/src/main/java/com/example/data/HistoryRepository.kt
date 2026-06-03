package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()

    suspend fun insertHistory(item: HistoryItem) {
        historyDao.insert(item)
    }

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) {
        historyDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun deleteHistory(item: HistoryItem) {
        historyDao.delete(item)
    }

    suspend fun deleteMultipleHistory(ids: List<Long>) {
        historyDao.deleteMultiple(ids)
    }

    suspend fun deleteAllHistory() {
        historyDao.deleteAll()
    }
}
