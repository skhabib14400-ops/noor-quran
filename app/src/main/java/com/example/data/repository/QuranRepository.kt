package com.example.data.repository

import com.example.data.db.QuranDao
import com.example.data.model.AyahEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.LastReadEntity
import com.example.data.model.SurahEntity
import kotlinx.coroutines.flow.Flow

class QuranRepository(private val quranDao: QuranDao) {

    val allSurahs: Flow<List<SurahEntity>> = quranDao.getAllSurahs()
    val bookmarks: Flow<List<BookmarkEntity>> = quranDao.getBookmarks()
    val lastRead: Flow<LastReadEntity?> = quranDao.getLastRead()

    fun getSurah(number: Int): Flow<SurahEntity?> = quranDao.getSurah(number)

    suspend fun getSurahSync(number: Int): SurahEntity? = quranDao.getSurahSync(number)

    fun getAyahsForSurah(surahNumber: Int): Flow<List<AyahEntity>> =
        quranDao.getAyahsForSurah(surahNumber)

    fun getAyahsForJuz(juz: Int): Flow<List<AyahEntity>> =
        quranDao.getAyahsForJuz(juz)

    fun getAyahsForPage(page: Int): Flow<List<AyahEntity>> =
        quranDao.getAyahsForPage(page)

    fun getAyah(surahNumber: Int, ayahNumber: Int): Flow<AyahEntity?> =
        quranDao.getAyah(surahNumber, ayahNumber)

    fun searchSurahs(query: String): Flow<List<SurahEntity>> =
        quranDao.searchSurahs(query.trim())

    fun searchAyahs(query: String): Flow<List<AyahEntity>> =
        quranDao.searchAyahs(query.trim())

    suspend fun addBookmark(surahNumber: Int, ayahNumber: Int, note: String? = null) {
        quranDao.insertBookmark(
            BookmarkEntity(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                timestamp = System.currentTimeMillis(),
                note = note
            )
        )
    }

    suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        quranDao.deleteBookmark(surahNumber, ayahNumber)
    }

    fun isBookmarked(surahNumber: Int, ayahNumber: Int): Flow<Boolean> =
        quranDao.isBookmarked(surahNumber, ayahNumber)

    suspend fun setLastRead(surahNumber: Int, ayahNumber: Int) {
        quranDao.updateLastRead(
            LastReadEntity(
                id = 1,
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getDatasetCounts(): Pair<Int, Int> {
        val surahs = quranDao.getTotalSurahCount()
        val ayahs = quranDao.getTotalAyahCount()
        return Pair(surahs, ayahs)
    }
}
