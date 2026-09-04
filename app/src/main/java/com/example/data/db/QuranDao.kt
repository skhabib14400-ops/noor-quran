package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AyahEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.LastReadEntity
import com.example.data.model.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs WHERE number = :number LIMIT 1")
    fun getSurah(number: Int): Flow<SurahEntity?>

    @Query("SELECT * FROM surahs WHERE number = :number LIMIT 1")
    suspend fun getSurahSync(number: Int): SurahEntity?

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun getAyahsForSurah(surahNumber: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE juz = :juz ORDER BY surahNumber ASC, ayahNumber ASC")
    fun getAyahsForJuz(juz: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE page = :page ORDER BY surahNumber ASC, ayahNumber ASC")
    fun getAyahsForPage(page: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber LIMIT 1")
    fun getAyah(surahNumber: Int, ayahNumber: Int): Flow<AyahEntity?>

    @Query("""
        SELECT * FROM surahs 
        WHERE nameEnglish LIKE '%' || :query || '%' 
           OR englishMeaning LIKE '%' || :query || '%'
           OR nameBengali LIKE '%' || :query || '%'
           OR bengaliMeaning LIKE '%' || :query || '%'
           OR nameArabic LIKE '%' || :query || '%'
           OR CAST(number AS TEXT) = :query
        ORDER BY number ASC
    """)
    fun searchSurahs(query: String): Flow<List<SurahEntity>>

    @Query("""
        SELECT * FROM ayahs 
        WHERE arabicText LIKE '%' || :query || '%' 
           OR translationEn LIKE '%' || :query || '%'
           OR translationBn LIKE '%' || :query || '%'
        ORDER BY surahNumber ASC, ayahNumber ASC
        LIMIT 100
    """)
    fun searchAyahs(query: String): Flow<List<AyahEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun deleteBookmark(surahNumber: Int, ayahNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber LIMIT 1)")
    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Flow<Boolean>

    @Query("SELECT * FROM last_read WHERE id = 1 LIMIT 1")
    fun getLastRead(): Flow<LastReadEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLastRead(lastRead: LastReadEntity)

    @Query("SELECT count(*) FROM ayahs")
    suspend fun getTotalAyahCount(): Int

    @Query("SELECT count(*) FROM surahs")
    suspend fun getTotalSurahCount(): Int
}
