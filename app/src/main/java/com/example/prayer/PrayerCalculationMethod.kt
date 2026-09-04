package com.example.prayer

enum class PrayerCalculationMethod(
    val displayNameEn: String,
    val displayNameBn: String,
    val displayNameAr: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null
) {
    KARACHI(
        displayNameEn = "University of Islamic Sciences, Karachi",
        displayNameBn = "ইসলামিক সায়েন্সেস বিশ্ববিদ্যালয়, করাচি",
        displayNameAr = "جامعة العلوم الإسلامية بكراتشي",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    MWL(
        displayNameEn = "Muslim World League (MWL)",
        displayNameBn = "মুসলিম ওয়ার্ল্ড লীগ (রাবেতা)",
        displayNameAr = "رابطة العالم الإسلامي",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    EGYPT(
        displayNameEn = "Egyptian General Authority of Survey",
        displayNameBn = "মিশরীয় জেনারেল অথরিটি অফ সার্ভে",
        displayNameAr = "الهيئة المصرية العامة للمساحة",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    UMM_AL_QURA(
        displayNameEn = "Umm Al-Qura University, Makkah",
        displayNameBn = "উম্মুল কুরা বিশ্ববিদ্যালয়, মক্কা",
        displayNameAr = "جامعة أم القرى بمكة المكرمة",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90
    ),
    ISNA(
        displayNameEn = "Islamic Society of North America (ISNA)",
        displayNameBn = "ইসলামিক সোসাইটি অফ নর্থ আমেরিকা",
        displayNameAr = "الجمعية الإسلامية لأمريكا الشمالية",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    DUBAI(
        displayNameEn = "Dubai / Gulf Region",
        displayNameBn = "দুবাই / উপসাগরীয় অঞ্চল",
        displayNameAr = "دبي / منطقة الخليج",
        fajrAngle = 18.2,
        ishaAngle = 18.2
    )
}

enum class AsrJuristicMethod(
    val displayNameEn: String,
    val displayNameBn: String,
    val displayNameAr: String,
    val shadowFactor: Double
) {
    HANAFI("Hanafi (Standard in Bangladesh & South Asia)", "হানাফী (বাংলাদেশ ও দক্ষিণ এশিয়ায় বহুল প্রচলিত)", "حنفي", 2.0),
    SHAFI("Standard / Shafi'i, Maliki, Hanbali", "শাফেয়ী, মালেকী, হাম্বলী", "شافعي / مالكي / حنبلي", 1.0)
}
