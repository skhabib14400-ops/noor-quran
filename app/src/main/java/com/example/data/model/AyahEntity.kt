package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey val id: Int,
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabicText: String,
    val translationEn: String,
    val translationBn: String,
    val juz: Int,
    val page: Int,
    val manzil: Int,
    val ruku: Int,
    val hizbQuarter: Int,
    val sajda: Int
)
