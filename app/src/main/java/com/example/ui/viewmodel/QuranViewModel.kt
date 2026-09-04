package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.QuranDatabase
import com.example.data.model.AyahEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.JuzInfo
import com.example.data.model.LastReadEntity
import com.example.data.model.SurahEntity
import com.example.data.repository.AppLanguage
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.ThemeMode
import com.example.data.repository.TranslationSelection
import com.example.data.repository.UserSettings
import com.example.prayer.AsrJuristicMethod
import com.example.prayer.CityLocation
import com.example.prayer.DailyPrayerSchedule
import com.example.prayer.HijriDate
import com.example.prayer.IslamicCalendar
import com.example.prayer.PrayerCalculationMethod
import com.example.prayer.PrayerTimesCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val db = QuranDatabase.getInstance(application)
    val quranRepository = QuranRepository(db.quranDao())
    val settingsRepository = SettingsRepository(application)

    val userSettings: StateFlow<UserSettings> = settingsRepository.settings

    val allSurahs: StateFlow<List<SurahEntity>> = quranRepository.allSurahs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = quranRepository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastRead: StateFlow<LastReadEntity?> = quranRepository.lastRead
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lastReadSurah: StateFlow<SurahEntity?> = lastRead.flatMapLatest { lr ->
        if (lr != null) quranRepository.getSurah(lr.surahNumber) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current reading selection
    private val _selectedSurahNumber = MutableStateFlow(1)
    val selectedSurahNumber: StateFlow<Int> = _selectedSurahNumber.asStateFlow()

    val currentSurah: StateFlow<SurahEntity?> = _selectedSurahNumber.flatMapLatest { sNum ->
        quranRepository.getSurah(sNum)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentAyahs: StateFlow<List<AyahEntity>> = _selectedSurahNumber.flatMapLatest { sNum ->
        quranRepository.getAyahsForSurah(sNum)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _targetScrollAyah = MutableStateFlow<Int?>(null)
    val targetScrollAyah: StateFlow<Int?> = _targetScrollAyah.asStateFlow()

    // Juz reading mode
    private val _selectedJuzNumber = MutableStateFlow<Int?>(null)
    val selectedJuzNumber: StateFlow<Int?> = _selectedJuzNumber.asStateFlow()

    val currentJuzAyahs: StateFlow<List<AyahEntity>> = _selectedJuzNumber.flatMapLatest { jNum ->
        if (jNum != null) quranRepository.getAyahsForJuz(jNum) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    val searchQuery = MutableStateFlow("")

    val surahSearchResults: StateFlow<List<SurahEntity>> = searchQuery.flatMapLatest { q ->
        if (q.isBlank()) {
            flowOf(emptyList())
        } else {
            quranRepository.searchSurahs(q)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ayahSearchResults: StateFlow<List<AyahEntity>> = searchQuery.flatMapLatest { q ->
        if (q.isBlank()) {
            flowOf(emptyList())
        } else {
            quranRepository.searchAyahs(q)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prayer times state
    private val _currentDate = MutableStateFlow(Date())
    val currentDate: StateFlow<Date> = _currentDate.asStateFlow()

    val hijriDate: StateFlow<HijriDate> = _currentDate.map { date ->
        IslamicCalendar.fromDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IslamicCalendar.fromDate(Date()))

    val prayerSchedule: StateFlow<DailyPrayerSchedule> = combine(
        _currentDate,
        userSettings
    ) { date, settings ->
        val cal = Calendar.getInstance().apply { time = date }
        PrayerTimesCalculator.calculate(
            calendar = cal,
            location = settings.selectedCity,
            method = settings.prayerCalculationMethod,
            asrMethod = settings.asrJuristicMethod,
            is24Hour = settings.is24Hour
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PrayerTimesCalculator.calculate(
            calendar = Calendar.getInstance(),
            location = CityLocation.DEFAULT_DHAKA,
            method = PrayerCalculationMethod.KARACHI,
            asrMethod = AsrJuristicMethod.HANAFI
        )
    )

    init {
        // Ticker to update prayer countdown and current time every 10 seconds
        viewModelScope.launch {
            while (true) {
                delay(10000)
                _currentDate.value = Date()
            }
        }
    }

    fun openSurah(surahNumber: Int, targetAyah: Int? = null) {
        _selectedSurahNumber.value = surahNumber.coerceIn(1, 114)
        _selectedJuzNumber.value = null
        _targetScrollAyah.value = targetAyah
    }

    fun openJuz(juzNumber: Int) {
        _selectedJuzNumber.value = juzNumber.coerceIn(1, 30)
        val juz = JuzInfo.ALL_JUZ.find { it.juzNumber == juzNumber }
        if (juz != null) {
            _selectedSurahNumber.value = juz.startSurah
            _targetScrollAyah.value = juz.startAyah
        }
    }

    fun clearTargetScrollAyah() {
        _targetScrollAyah.value = null
    }

    fun nextSurah() {
        val next = _selectedSurahNumber.value + 1
        if (next <= 114) {
            openSurah(next, 1)
        }
    }

    fun previousSurah() {
        val prev = _selectedSurahNumber.value - 1
        if (prev >= 1) {
            openSurah(prev, 1)
        }
    }

    fun toggleBookmark(surahNumber: Int, ayahNumber: Int, isBookmarked: Boolean) {
        viewModelScope.launch {
            if (isBookmarked) {
                quranRepository.removeBookmark(surahNumber, ayahNumber)
            } else {
                quranRepository.addBookmark(surahNumber, ayahNumber)
            }
        }
    }

    fun isAyahBookmarked(surahNumber: Int, ayahNumber: Int): Flow<Boolean> {
        return quranRepository.isBookmarked(surahNumber, ayahNumber)
    }

    fun updateLastRead(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            quranRepository.setLastRead(surahNumber, ayahNumber)
        }
    }

    // Settings actions
    fun setLanguage(language: AppLanguage) {
        settingsRepository.updateLanguage(language)
    }

    fun setTranslationSelection(selection: TranslationSelection) {
        settingsRepository.updateTranslationSelection(selection)
    }

    fun setArabicFontSize(size: Float) {
        settingsRepository.updateArabicFontSize(size.coerceIn(18f, 44f))
    }

    fun setTranslationFontSize(size: Float) {
        settingsRepository.updateTranslationFontSize(size.coerceIn(12f, 26f))
    }

    fun setThemeMode(mode: ThemeMode) {
        settingsRepository.updateThemeMode(mode)
    }

    fun setIs24Hour(is24: Boolean) {
        settingsRepository.updateIs24Hour(is24)
    }

    fun setPrayerCalculationMethod(method: PrayerCalculationMethod) {
        settingsRepository.updatePrayerMethod(method)
    }

    fun setAsrJuristicMethod(method: AsrJuristicMethod) {
        settingsRepository.updateAsrMethod(method)
    }

    fun setSelectedCity(city: CityLocation) {
        settingsRepository.updateSelectedCity(city)
        _currentDate.value = Date()
    }

    fun updateGpsLocation(lat: Double, lon: Double, name: String = "GPS Location") {
        val isBangladesh = lat in 20.0..27.0 && lon in 88.0..93.0
        val tzId = if (isBangladesh) "Asia/Dhaka" else java.util.TimeZone.getDefault().id
        val gpsCity = CityLocation(
            nameEn = name,
            nameBn = if (isBangladesh) "বর্তমান অবস্থান (বাংলাদেশ)" else name,
            nameAr = name,
            latitude = lat,
            longitude = lon,
            timeZoneId = tzId,
            country = if (isBangladesh) "Bangladesh" else "Current Location"
        )
        settingsRepository.updateSelectedCity(gpsCity)
        _currentDate.value = Date()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settingsRepository.updateNotifications(enabled)
    }
}
