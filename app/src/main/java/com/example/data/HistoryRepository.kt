package com.example.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()

    fun searchHistory(query: String): Flow<List<HistoryItem>> = historyDao.searchHistory(query)

    suspend fun insertHistory(item: HistoryItem): Long = historyDao.insertHistory(item)

    suspend fun updateHistory(item: HistoryItem) = historyDao.updateHistory(item)

    suspend fun deleteHistory(item: HistoryItem) = historyDao.deleteHistory(item)

    suspend fun deleteHistoryById(id: Long) = historyDao.deleteHistoryById(id)

    suspend fun deleteMultipleHistory(ids: List<Long>) = historyDao.deleteMultipleHistory(ids)

    suspend fun deleteAllHistory() = historyDao.deleteAllHistory()

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) = 
        historyDao.updateFavoriteStatus(id, isFavorite)
}
