package com.example.data.repository

import com.example.data.dao.HarmonyDao
import com.example.data.model.HarmonyRecord
import kotlinx.coroutines.flow.Flow

class HarmonyRepository(private val harmonyDao: HarmonyDao) {
    val allRecords: Flow<List<HarmonyRecord>> = harmonyDao.getAllRecords()

    fun getHighScoreForAttraction(attractionName: String): Flow<Int?> {
        return harmonyDao.getHighScoreForAttraction(attractionName)
    }

    suspend fun insertRecord(record: HarmonyRecord) {
        harmonyDao.insertRecord(record)
    }

    suspend fun clearAllRecords() {
        harmonyDao.deleteAllRecords()
    }
}
