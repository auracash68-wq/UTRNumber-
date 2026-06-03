package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryItem)

    @Query("UPDATE calculation_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Delete
    suspend fun delete(item: HistoryItem)

    @Query("DELETE FROM calculation_history WHERE id IN (:ids)")
    suspend fun deleteMultiple(ids: List<Long>)

    @Query("DELETE FROM calculation_history")
    suspend fun deleteAll()
}
