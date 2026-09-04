package com.example.prayer

import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class PrayerTime(
    val type: PrayerType,
    val date: Date,
    val timeFormatted: String
)

enum class PrayerType(val displayNameEn: String, val displayNameBn: String, val displayNameAr: String) {
    FAJR("Fajr", "ফজর", "الفجر"),
    SUNRISE("Sunrise", "সূর্যোদয়", "الشروق"),
    DHUHR("Dhuhr", "যোহর", "الظهر"),
    ASR("Asr", "আসর", "العصر"),
    MAGHRIB("Maghrib", "মাগরিব", "المغرب"),
    ISHA("Isha", "ইশা", "العشاء")
}

data class DailyPrayerSchedule(
    val fajr: PrayerTime,
    val sunrise: PrayerTime,
    val dhuhr: PrayerTime,
    val asr: PrayerTime,
    val maghrib: PrayerTime,
    val isha: PrayerTime,
    val currentPrayer: PrayerType?,
    val nextPrayer: PrayerType,
    val nextPrayerTimeRemainingMillis: Long,
    val calculationMethod: PrayerCalculationMethod,
    val asrMethod: AsrJuristicMethod,
    val location: CityLocation
) {
    val prayerList: List<PrayerTime>
        get() = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
}

object PrayerTimesCalculator {

    private fun d2r(d: Double) = d * Math.PI / 180.0
    private fun r2d(r: Double) = r * 180.0 / Math.PI
    private fun fixHour(a: Double): Double {
        var x = a - 24.0 * floor(a / 24.0)
        if (x < 0) x += 24.0
        return x
    }

    private fun fixAngle(a: Double): Double {
        var x = a - 360.0 * floor(a / 360.0)
        if (x < 0) x += 360.0
        return x
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(d2r(g)) + 0.020 * sin(d2r(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = r2d(atan2(cos(d2r(e)) * sin(d2r(l)), cos(d2r(l)))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = r2d(asin(sin(d2r(e)) * sin(d2r(l))))
        return Pair(decl, eqt)
    }

    private fun sunAltitudeAngle(lat: Double, decl: Double, angle: Double, direction: Boolean): Double {
        val term1 = -sin(d2r(angle)) - sin(d2r(lat)) * sin(d2r(decl))
        val term2 = cos(d2r(lat)) * cos(d2r(decl))
        val cosVal = term1 / term2
        if (cosVal > 1.0 || cosVal < -1.0) {
            return Double.NaN
        }
        val hourAngle = r2d(acos(cosVal)) / 15.0
        return if (direction) hourAngle else -hourAngle
    }

    private fun asrAngle(lat: Double, decl: Double, factor: Double): Double {
        val shadow = factor + tan(d2r(abs(lat - decl)))
        val altitude = r2d(atan(1.0 / shadow))
        return sunAltitudeAngle(lat, decl, -altitude, true)
    }

    fun calculate(
        calendar: Calendar,
        location: CityLocation,
        method: PrayerCalculationMethod,
        asrMethod: AsrJuristicMethod,
        is24Hour: Boolean = false
    ): DailyPrayerSchedule {
        val tz = TimeZone.getTimeZone(location.timeZoneId)
        val localCal = Calendar.getInstance(tz).apply {
            timeInMillis = calendar.timeInMillis
        }

        val year = localCal.get(Calendar.YEAR)
        val month = localCal.get(Calendar.MONTH) + 1
        val day = localCal.get(Calendar.DAY_OF_MONTH)

        val jd = julianDate(year, month, day)
        val (decl, eqt) = sunPosition(jd)

        val tzOffsetHours = tz.getOffset(localCal.timeInMillis) / 3600000.0
        val solarNoon = fixHour(12.0 + tzOffsetHours - location.longitude / 15.0 - eqt)

        // Sunrise & Sunset angles (0.833° for refraction)
        val sunriseAngle = 0.833
        val sunriseOffset = sunAltitudeAngle(location.latitude, decl, sunriseAngle, false)
        val sunsetOffset = sunAltitudeAngle(location.latitude, decl, sunriseAngle, true)

        val sunriseHour = fixHour(solarNoon + sunriseOffset)
        val sunsetHour = fixHour(solarNoon + sunsetOffset)

        // Fajr
        val fajrOffset = sunAltitudeAngle(location.latitude, decl, method.fajrAngle, false)
        val fajrHour = fixHour(solarNoon + fajrOffset)

        // Asr
        val asrOffset = asrAngle(location.latitude, decl, asrMethod.shadowFactor)
        val asrHour = fixHour(solarNoon + asrOffset)

        // Maghrib
        val maghribHour = sunsetHour

        // Isha
        val ishaHour = if (method.ishaIntervalMinutes != null) {
            fixHour(maghribHour + method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaOffset = sunAltitudeAngle(location.latitude, decl, method.ishaAngle, true)
            fixHour(solarNoon + ishaOffset)
        }

        fun toDate(hourFrac: Double): Date {
            val h = floor(hourFrac).toInt()
            val m = floor((hourFrac - h) * 60.0).toInt()
            val s = floor(((hourFrac - h) * 60.0 - m) * 60.0).toInt()

            val c = Calendar.getInstance(tz).apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, s)
                set(Calendar.MILLISECOND, 0)
            }
            return c.time
        }

        fun formatTime(date: Date): String {
            val c = Calendar.getInstance(tz).apply { time = date }
            val h24 = c.get(Calendar.HOUR_OF_DAY)
            val m = c.get(Calendar.MINUTE)
            val mm = String.format(Locale.US, "%02d", m)

            return if (is24Hour) {
                String.format(Locale.US, "%02d:%s", h24, mm)
            } else {
                val amPm = if (h24 < 12) "AM" else "PM"
                val h12 = if (h24 % 12 == 0) 12 else h24 % 12
                String.format(Locale.US, "%d:%s %s", h12, mm, amPm)
            }
        }

        val fajrDate = toDate(fajrHour)
        val sunriseDate = toDate(sunriseHour)
        val dhuhrDate = toDate(solarNoon)
        val asrDate = toDate(asrHour)
        val maghribDate = toDate(maghribHour)
        val ishaDate = toDate(ishaHour)

        val fajr = PrayerTime(PrayerType.FAJR, fajrDate, formatTime(fajrDate))
        val sunrise = PrayerTime(PrayerType.SUNRISE, sunriseDate, formatTime(sunriseDate))
        val dhuhr = PrayerTime(PrayerType.DHUHR, dhuhrDate, formatTime(dhuhrDate))
        val asr = PrayerTime(PrayerType.ASR, asrDate, formatTime(asrDate))
        val maghrib = PrayerTime(PrayerType.MAGHRIB, maghribDate, formatTime(maghribDate))
        val isha = PrayerTime(PrayerType.ISHA, ishaDate, formatTime(ishaDate))

        val now = Calendar.getInstance(tz).timeInMillis

        val prayerSchedule = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
        
        var current: PrayerType? = null
        var next: PrayerType = PrayerType.FAJR
        var remainingMillis: Long = 0

        val fajrMillis = fajrDate.time
        val sunriseMillis = sunriseDate.time
        val dhuhrMillis = dhuhrDate.time
        val asrMillis = asrDate.time
        val maghribMillis = maghribDate.time
        val ishaMillis = ishaDate.time

        when {
            now < fajrMillis -> {
                current = PrayerType.ISHA // From yesterday
                next = PrayerType.FAJR
                remainingMillis = fajrMillis - now
            }
            now < sunriseMillis -> {
                current = PrayerType.FAJR
                next = PrayerType.SUNRISE
                remainingMillis = sunriseMillis - now
            }
            now < dhuhrMillis -> {
                current = PrayerType.SUNRISE
                next = PrayerType.DHUHR
                remainingMillis = dhuhrMillis - now
            }
            now < asrMillis -> {
                current = PrayerType.DHUHR
                next = PrayerType.ASR
                remainingMillis = asrMillis - now
            }
            now < maghribMillis -> {
                current = PrayerType.ASR
                next = PrayerType.MAGHRIB
                remainingMillis = maghribMillis - now
            }
            now < ishaMillis -> {
                current = PrayerType.MAGHRIB
                next = PrayerType.ISHA
                remainingMillis = ishaMillis - now
            }
            else -> {
                current = PrayerType.ISHA
                next = PrayerType.FAJR
                // Fajr tomorrow (+24h approx)
                remainingMillis = (fajrMillis + 24 * 3600 * 1000L) - now
            }
        }

        return DailyPrayerSchedule(
            fajr = fajr,
            sunrise = sunrise,
            dhuhr = dhuhr,
            asr = asr,
            maghrib = maghrib,
            isha = isha,
            currentPrayer = current,
            nextPrayer = next,
            nextPrayerTimeRemainingMillis = remainingMillis,
            calculationMethod = method,
            asrMethod = asrMethod,
            location = location
        )
    }
}
