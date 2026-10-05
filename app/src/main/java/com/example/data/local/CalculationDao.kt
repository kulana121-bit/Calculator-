package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC LIMIT 500")
    fun getAllHistory(): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculation_history WHERE isFavorite = 1 ORDER BY timestamp DESC LIMIT 500")
    fun getFavoriteHistory(): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(entity: CalculationEntity): Long

    @Delete
    suspend fun deleteCalculation(entity: CalculationEntity)

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAllHistory()

    @Update
    suspend fun updateCalculation(entity: CalculationEntity)

    @Query("UPDATE calculation_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
