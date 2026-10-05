package com.example.data.local

import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    val allHistory: Flow<List<CalculationEntity>> = dao.getAllHistory()
    val favoriteHistory: Flow<List<CalculationEntity>> = dao.getFavoriteHistory()

    suspend fun insert(expression: String, result: String, type: String = "BASIC"): Long {
        return dao.insertCalculation(
            CalculationEntity(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis(),
                calculationType = type
            )
        )
    }

    suspend fun delete(entity: CalculationEntity) {
        dao.deleteCalculation(entity)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAllHistory()
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) {
        dao.setFavorite(id, !currentFavorite)
    }
}
