package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.prayer.AsrJuristicMethod
import com.example.prayer.CityLocation
import com.example.prayer.PrayerCalculationMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    BENGALI("bn", "Bengali", "বাংলা"),
    ARABIC("ar", "Arabic", "العربية")
}

enum class TranslationSelection(val displayNameEn: String, val displayNameBn: String, val displayNameAr: String) {
    BENGALI_ONLY("Bengali (Muhiuddin Khan)", "বাংলা (মুহিউদ্দীন খান)", "البنغالية (محيي الدين خان)"),
    ENGLISH_ONLY("English (Saheeh International)", "ইংরেজি (সহীহ ইন্টারন্যাশনাল)", "الإنجليزية (صحيح انترناشونال)"),
    BOTH("Both Bengali & English", "বাংলা ও ইংরেজি উভয়ই", "البنغالية والإنجليزية معاً")
}

enum class ThemeMode(val displayNameEn: String, val displayNameBn: String, val displayNameAr: String) {
    SYSTEM("System Default", "সিস্টেম ডিফল্ট", "افتراضي النظام"),
    LIGHT("Light Theme", "লাইট থিম", "فاتح"),
    DARK("Dark Theme", "ডার্ক থিম", "داكن")
}

data class UserSettings(
    val appLanguage: AppLanguage = AppLanguage.BENGALI,
    val translationSelection: TranslationSelection = TranslationSelection.BOTH,
    val arabicFontSize: Float = 28f,
    val translationFontSize: Float = 16f,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val is24Hour: Boolean = false,
    val prayerCalculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.KARACHI,
    val asrJuristicMethod: AsrJuristicMethod = AsrJuristicMethod.HANAFI,
    val selectedCity: CityLocation = CityLocation.DEFAULT_DHAKA,
    val isNotificationEnabled: Boolean = true
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("noor_quran_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val langName = prefs.getString("app_language", AppLanguage.BENGALI.name) ?: AppLanguage.BENGALI.name
        val appLanguage = runCatching { AppLanguage.valueOf(langName) }.getOrDefault(AppLanguage.BENGALI)

        val transName = prefs.getString("translation_selection", TranslationSelection.BOTH.name)
            ?: TranslationSelection.BOTH.name
        val translationSelection = runCatching { TranslationSelection.valueOf(transName) }
            .getOrDefault(TranslationSelection.BOTH)

        val arabicFontSize = prefs.getFloat("arabic_font_size", 28f)
        val translationFontSize = prefs.getFloat("translation_font_size", 16f)

        val themeName = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = runCatching { ThemeMode.valueOf(themeName) }.getOrDefault(ThemeMode.SYSTEM)

        val is24Hour = prefs.getBoolean("is_24_hour", false)

        val prayerMethodName = prefs.getString("prayer_method", PrayerCalculationMethod.KARACHI.name)
            ?: PrayerCalculationMethod.KARACHI.name
        val prayerCalculationMethod = runCatching { PrayerCalculationMethod.valueOf(prayerMethodName) }
            .getOrDefault(PrayerCalculationMethod.KARACHI)

        val asrMethodName = prefs.getString("asr_method", AsrJuristicMethod.HANAFI.name)
            ?: AsrJuristicMethod.HANAFI.name
        val asrJuristicMethod = runCatching { AsrJuristicMethod.valueOf(asrMethodName) }
            .getOrDefault(AsrJuristicMethod.HANAFI)

        val cityName = prefs.getString("selected_city", CityLocation.DEFAULT_DHAKA.nameEn)
            ?: CityLocation.DEFAULT_DHAKA.nameEn
        val selectedCity = CityLocation.PRESET_CITIES.find { it.nameEn == cityName } ?: CityLocation.DEFAULT_DHAKA

        val isNotificationEnabled = prefs.getBoolean("notifications_enabled", true)

        return UserSettings(
            appLanguage = appLanguage,
            translationSelection = translationSelection,
            arabicFontSize = arabicFontSize,
            translationFontSize = translationFontSize,
            themeMode = themeMode,
            is24Hour = is24Hour,
            prayerCalculationMethod = prayerCalculationMethod,
            asrJuristicMethod = asrJuristicMethod,
            selectedCity = selectedCity,
            isNotificationEnabled = isNotificationEnabled
        )
    }

    fun updateLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.name).apply()
        _settings.value = _settings.value.copy(appLanguage = language)
    }

    fun updateTranslationSelection(selection: TranslationSelection) {
        prefs.edit().putString("translation_selection", selection.name).apply()
        _settings.value = _settings.value.copy(translationSelection = selection)
    }

    fun updateArabicFontSize(size: Float) {
        prefs.edit().putFloat("arabic_font_size", size).apply()
        _settings.value = _settings.value.copy(arabicFontSize = size)
    }

    fun updateTranslationFontSize(size: Float) {
        prefs.edit().putFloat("translation_font_size", size).apply()
        _settings.value = _settings.value.copy(translationFontSize = size)
    }

    fun updateThemeMode(theme: ThemeMode) {
        prefs.edit().putString("theme_mode", theme.name).apply()
        _settings.value = _settings.value.copy(themeMode = theme)
    }

    fun updateIs24Hour(is24: Boolean) {
        prefs.edit().putBoolean("is_24_hour", is24).apply()
        _settings.value = _settings.value.copy(is24Hour = is24)
    }

    fun updatePrayerMethod(method: PrayerCalculationMethod) {
        prefs.edit().putString("prayer_method", method.name).apply()
        _settings.value = _settings.value.copy(prayerCalculationMethod = method)
    }

    fun updateAsrMethod(method: AsrJuristicMethod) {
        prefs.edit().putString("asr_method", method.name).apply()
        _settings.value = _settings.value.copy(asrJuristicMethod = method)
    }

    fun updateSelectedCity(city: CityLocation) {
        prefs.edit().putString("selected_city", city.nameEn).apply()
        _settings.value = _settings.value.copy(selectedCity = city)
    }

    fun updateNotifications(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _settings.value = _settings.value.copy(isNotificationEnabled = enabled)
    }
}
