package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_read")
data class LastReadEntity(
    @PrimaryKey val id: Int = 1,
    val surahNumber: Int,
    val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
