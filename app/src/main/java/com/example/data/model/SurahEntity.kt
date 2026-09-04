package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val englishMeaning: String,
    val nameBengali: String,
    val bengaliMeaning: String,
    val numberOfAyahs: Int,
    val revelationType: String
)
