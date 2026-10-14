package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "harmony_records")
data class HarmonyRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val attractionName: String,
    val score: Int,
    val dateString: String,
    val noteChainLength: Int,
    val themePalette: String
)
