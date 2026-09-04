package com.example.prayer

import java.util.Calendar
import java.util.Date
import kotlin.math.floor

data class HijriDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthNameEn: String,
    val monthNameBn: String,
    val monthNameAr: String
) {
    fun formatEn(): String = "$day $monthNameEn $year AH"
    fun formatBn(): String = "$day $monthNameBn $year হিজরি"
    fun formatAr(): String = "$day $monthNameAr $year هـ"
}

object IslamicCalendar {

    private val MONTH_NAMES = listOf(
        Triple("Muharram", "মহররম", "محرم"),
        Triple("Safar", "সফর", "صفر"),
        Triple("Rabi' al-Awwal", "রবিউল আউয়াল", "ربيع الأول"),
        Triple("Rabi' al-Thani", "রবিউস সানি", "ربيع الثاني"),
        Triple("Jumada al-Awwal", "জমাদিউল আউয়াল", "جمادى الأولى"),
        Triple("Jumada al-Thani", "জমাদিউস সানি", "جمادى الثانية"),
        Triple("Rajab", "রজব", "رجب"),
        Triple("Sha'ban", "শাবান", "شعبان"),
        Triple("Ramadan", "রমজান", "رمضان"),
        Triple("Shawwal", "শাওয়াল", "شوال"),
        Triple("Dhu al-Qi'dah", "জিলকদ", "ذو القعدة"),
        Triple("Dhu al-Hijjah", "জিলহজ্জ", "ذو الحجة")
    )

    fun fromDate(date: Date): HijriDate {
        val cal = Calendar.getInstance().apply { time = date }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        var y = year
        var m = month
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        val epoch = 1948439.5
        // Total days since Islamic Epoch (1 Muharram 1 AH = 16 July 622 CE)
        val daysSinceEpoch = jd - epoch
        // The Islamic calendar has 30-year cycle of 10,631 days (19 normal years of 354 days and 11 leap years of 355 days)
        val cycles = floor(daysSinceEpoch / 10631.0)
        val dayInCycle = daysSinceEpoch - cycles * 10631.0

        // Year within 30-year cycle
        val yearInCycle = floor((dayInCycle + 0.5) / 354.366)
        var hYear = (cycles * 30.0 + yearInCycle + 1.0).toInt()

        // Days in years within cycle
        fun daysInHijriYear(y: Int): Int {
            // Leap years in 30 year cycle: 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29
            val mod = ((y - 1) % 30) + 1
            return if (mod in listOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)) 355 else 354
        }

        // Calculate days up to start of this year in cycle
        var accumulatedDays = 0
        for (y in 1..yearInCycle.toInt()) {
            accumulatedDays += daysInHijriYear(y)
        }

        var dayInYear = (dayInCycle - accumulatedDays).toInt()
        if (dayInYear < 1) {
            hYear -= 1
            dayInYear += daysInHijriYear(yearInCycle.toInt())
        }

        var hMonth = 1
        var hDay = dayInYear

        for (m in 1..12) {
            val isLeap = daysInHijriYear(((hYear - 1) % 30) + 1) == 355
            val daysInMonth = if (m % 2 == 1) 30 else if (m == 12 && isLeap) 30 else 29
            if (hDay <= daysInMonth) {
                hMonth = m
                break
            }
            hDay -= daysInMonth
        }

        if (hDay <= 0) hDay = 1
        if (hMonth > 12) hMonth = 12

        val names = MONTH_NAMES.getOrElse(hMonth - 1) { MONTH_NAMES[0] }
        return HijriDate(
            day = hDay,
            month = hMonth,
            year = hYear,
            monthNameEn = names.first,
            monthNameBn = names.second,
            monthNameAr = names.third
        )
    }
}
