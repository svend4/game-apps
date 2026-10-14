package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HarmonyRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HarmonyDao {
    @Query("SELECT * FROM harmony_records ORDER BY id DESC")
    fun getAllRecords(): Flow<List<HarmonyRecord>>

    @Query("SELECT MAX(score) FROM harmony_records WHERE attractionName = :attractionName")
    fun getHighScoreForAttraction(attractionName: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HarmonyRecord)

    @Query("DELETE FROM harmony_records")
    suspend fun deleteAllRecords()
}
