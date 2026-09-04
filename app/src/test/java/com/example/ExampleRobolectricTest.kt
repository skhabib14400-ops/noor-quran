package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.prayer.AsrJuristicMethod
import com.example.prayer.CityLocation
import com.example.prayer.IslamicCalendar
import com.example.prayer.PrayerCalculationMethod
import com.example.prayer.PrayerTimesCalculator
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Noor Islam", appName)
  }

  @Test
  fun `test prayer times calculation for Dhaka`() {
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.MARCH, 15, 12, 0, 0)
    }
    val schedule = PrayerTimesCalculator.calculate(
      calendar = cal,
      location = CityLocation.DEFAULT_DHAKA,
      method = PrayerCalculationMethod.KARACHI,
      asrMethod = AsrJuristicMethod.HANAFI
    )

    assertNotNull(schedule.fajr)
    assertNotNull(schedule.sunrise)
    assertNotNull(schedule.dhuhr)
    assertNotNull(schedule.asr)
    assertNotNull(schedule.maghrib)
    assertNotNull(schedule.isha)

    // Fajr < Sunrise < Dhuhr < Asr < Maghrib < Isha
    assertTrue(schedule.fajr.date.before(schedule.sunrise.date))
    assertTrue(schedule.sunrise.date.before(schedule.dhuhr.date))
    assertTrue(schedule.dhuhr.date.before(schedule.asr.date))
    assertTrue(schedule.asr.date.before(schedule.maghrib.date))
    assertTrue(schedule.maghrib.date.before(schedule.isha.date))
  }

  @Test
  fun `test islamic calendar calculation`() {
    val cal = Calendar.getInstance().apply {
      set(2026, Calendar.FEBRUARY, 18)
    }
    val hijri = IslamicCalendar.fromDate(cal.time)
    assertNotNull(hijri.monthNameEn)
    assertNotNull(hijri.monthNameBn)
    assertNotNull(hijri.monthNameAr)
    assertTrue(hijri.year >= 1446)
  }

  @Test
  fun `test database initialization and query`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.db.QuranDatabase.getInstance(context)
    val surahs = db.quranDao().getSurahSync(1)
    assertNotNull(surahs)
  }

  @Test
  fun `test QuranViewModel initialization and data access`() = kotlinx.coroutines.runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.viewmodel.QuranViewModel(app)
    assertNotNull(vm)
    val surah = vm.quranRepository.getSurahSync(1)
    assertNotNull(surah)
    assertEquals("Al-Faatiha", surah?.nameEnglish)
    assertEquals(7, surah?.numberOfAyahs)
    val ayahs = vm.quranRepository.getAyahsForSurah(1).first()
    assertEquals(7, ayahs.size)
  }

  @Test
  fun `test MainActivity launch and lifecycle`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java)
    controller.setup()
    val activity = controller.get()
    assertNotNull(activity)
    controller.pause().stop().destroy()
  }
}
