package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppLanguage
import com.example.prayer.DailyPrayerSchedule
import com.example.prayer.HijriDate
import com.example.prayer.PrayerTime
import com.example.prayer.PrayerType
import com.example.ui.navigation.Screen
import com.example.ui.strings.AppStrings
import com.example.ui.theme.QuranGold
import com.example.ui.viewmodel.QuranViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    onNavigate: (Screen) -> Unit,
    onOpenSurah: (surahNumber: Int, targetAyah: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val lang = settings.appLanguage

    val prayerSchedule by viewModel.prayerSchedule.collectAsState()
    val hijriDate by viewModel.hijriDate.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val lastReadSurah by viewModel.lastReadSurah.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Section: Location Selector Pill
        item {
            LocationPillSelector(
                schedule = prayerSchedule,
                lang = lang,
                onLocationClick = { onNavigate(Screen.Prayer) }
            )
        }

        // 2. Islamic Hero & Prayer Times Banner
        item {
            HeroPrayerCard(
                schedule = prayerSchedule,
                hijriDate = hijriDate,
                lang = lang,
                onCardClick = { onNavigate(Screen.Prayer) }
            )
        }

        // 3. Sehri & Iftar Quick Status Strip
        item {
            SehriIftarStrip(
                schedule = prayerSchedule,
                lang = lang
            )
        }

        // 4. Mini Horizontal Prayer Schedule Row
        item {
            PrayerMiniScheduleRow(
                schedule = prayerSchedule,
                lang = lang
            )
        }

        // 5. Islamic Features Hub (Tasbih, Qibla, 99 Names, Dua)
        item {
            Text(
                text = if (lang == AppLanguage.BENGALI) "ইসলামিক ফিচারসমূহ" else if (lang == AppLanguage.ARABIC) "الخدمات الإسلامية" else "Islamic Utilities",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            IslamicFeaturesHub(
                onNavigate = onNavigate,
                lang = lang
            )
        }

        // 6. Continue Reading / Last Read Card
        item {
            ContinueReadingCard(
                surahName = lastReadSurah?.nameEnglish ?: "Al-Faatiha",
                surahBengali = lastReadSurah?.nameBengali ?: "আল-ফাতিহা",
                surahArabic = lastReadSurah?.nameArabic ?: "سُورَةُ ٱلْفَاتِحَةِ",
                ayahNumber = lastRead?.ayahNumber ?: 1,
                surahNumber = lastRead?.surahNumber ?: 1,
                lang = lang,
                onClick = {
                    val sNum = lastRead?.surahNumber ?: 1
                    val aNum = lastRead?.ayahNumber ?: 1
                    onOpenSurah(sNum, aNum)
                }
            )
        }

        // 7. Quick Access Hub (Surahs, Juz, Bookmarks, Prayer Times)
        item {
            Text(
                text = AppStrings.get("quick_access", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            QuickAccessHub(
                onNavigate = onNavigate,
                lang = lang
            )
        }

        // 8. Featured Daily Ayah (Ayatul Kursi - Al-Baqarah 2:255)
        item {
            DailyVerseCard(
                lang = lang,
                onReadMore = { onOpenSurah(2, 255) }
            )
        }

        // 9. Non-intrusive Banner Ad (Support / Sponsor section)
        item {
            com.example.ads.AdMobBanner(
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Bottom space
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocationPillSelector(
    schedule: DailyPrayerSchedule,
    lang: AppLanguage,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onLocationClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier.clip(RoundedCornerShape(20.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (lang) {
                    AppLanguage.BENGALI -> schedule.location.nameBn
                    AppLanguage.ARABIC -> schedule.location.nameAr
                    AppLanguage.ENGLISH -> schedule.location.nameEn
                },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeroPrayerCard(
    schedule: DailyPrayerSchedule,
    hijriDate: HijriDate,
    lang: AppLanguage,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
        )
    )

    val nextPrayerName = when (lang) {
        AppLanguage.BENGALI -> schedule.nextPrayer.displayNameBn
        AppLanguage.ARABIC -> schedule.nextPrayer.displayNameAr
        AppLanguage.ENGLISH -> schedule.nextPrayer.displayNameEn
    }

    val nextPrayerTime = when (schedule.nextPrayer) {
        PrayerType.FAJR -> schedule.fajr.timeFormatted
        PrayerType.SUNRISE -> schedule.sunrise.timeFormatted
        PrayerType.DHUHR -> schedule.dhuhr.timeFormatted
        PrayerType.ASR -> schedule.asr.timeFormatted
        PrayerType.MAGHRIB -> schedule.maghrib.timeFormatted
        PrayerType.ISHA -> schedule.isha.timeFormatted
    }

    // Format remaining duration
    val remainingSec = (schedule.nextPrayerTimeRemainingMillis / 1000).coerceAtLeast(0)
    val hours = remainingSec / 3600
    val minutes = (remainingSec % 3600) / 60
    val durationText = if (hours > 0) {
        "$hours hr $minutes min"
    } else {
        "$minutes min"
    }

    val hijriString = when (lang) {
        AppLanguage.BENGALI -> hijriDate.formatBn()
        AppLanguage.ARABIC -> hijriDate.formatAr()
        AppLanguage.ENGLISH -> hijriDate.formatEn()
    }

    val gregorianString = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(java.util.Date())

    Surface(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .border(1.dp, QuranGold.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Top row: Location & Hijri Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location name inside card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = QuranGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.BENGALI -> schedule.location.nameBn
                                AppLanguage.ARABIC -> schedule.location.nameAr
                                AppLanguage.ENGLISH -> schedule.location.nameEn
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }

                    // Hijri Date
                    Text(
                        text = hijriString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = QuranGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Center Highlight: Next Prayer Callout
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = AppStrings.get("next_prayer", lang).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = nextPrayerName,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 28.sp
                            )
                        )
                        Text(
                            text = nextPrayerTime,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = QuranGold,
                                fontSize = 22.sp
                            )
                        )
                    }
                }

                // Bottom row: Countdown pill & Gregorian date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = String.format(AppStrings.get("in_time", lang), durationText),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Text(
                        text = gregorianString,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SehriIftarStrip(
    schedule: DailyPrayerSchedule,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sehri Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3949AB).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = "Sehri",
                        tint = Color(0xFF3949AB),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = AppStrings.get("sehri", lang),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = schedule.fajr.timeFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Iftar Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE65100).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Iftar",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = AppStrings.get("iftar", lang),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = schedule.maghrib.timeFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun IslamicFeaturesHub(
    onNavigate: (Screen) -> Unit,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickHubItem(
            title = AppStrings.get("tasbih", lang),
            icon = Icons.Default.TouchApp,
            color = Color(0xFF2E7D32),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Screen.Tasbih) }
        )
        QuickHubItem(
            title = AppStrings.get("qibla", lang),
            icon = Icons.Default.Explore,
            color = Color(0xFF0277BD),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Screen.Qibla) }
        )
        QuickHubItem(
            title = AppStrings.get("names_of_allah", lang),
            icon = Icons.Default.AutoAwesome,
            color = QuranGold,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Screen.NamesOfAllah) }
        )
        QuickHubItem(
            title = AppStrings.get("dua", lang),
            icon = Icons.Default.Favorite,
            color = Color(0xFF6A1B9A),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Screen.Dua) }
        )
    }
}

@Composable
private fun PrayerMiniScheduleRow(
    schedule: DailyPrayerSchedule,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val items = schedule.prayerList

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { p ->
            val isNext = p.type == schedule.nextPrayer

            val name = when (lang) {
                AppLanguage.BENGALI -> p.type.displayNameBn
                AppLanguage.ARABIC -> p.type.displayNameAr
                AppLanguage.ENGLISH -> p.type.displayNameEn
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isNext) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                tonalElevation = if (isNext) 4.dp else 1.dp,
                shadowElevation = if (isNext) 2.dp else 0.dp,
                modifier = Modifier
                    .width(96.dp)
                    .then(
                        if (isNext) Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(14.dp)
                        )
                        else Modifier
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = p.timeFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(
    surahName: String,
    surahBengali: String,
    surahArabic: String,
    ayahNumber: Int,
    surahNumber: Int,
    lang: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("continue_reading_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = AppStrings.get("continue_reading", lang),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    val title = if (lang == AppLanguage.BENGALI && surahBengali.isNotBlank()) {
                        surahBengali
                    } else {
                        surahName
                    }
                    Text(
                        text = "$title • ${String.format(AppStrings.get("ayah_number", lang), ayahNumber)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = surahArabic,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Read",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun QuickAccessHub(
    onNavigate: (Screen) -> Unit,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickHubItem(
            title = AppStrings.get("surahs", lang),
            icon = Icons.AutoMirrored.Filled.MenuBook,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Screen.Quran) }
        )
        QuickHubItem(
            title = AppStrings.get("juz", lang),
            icon = Icons.Default.ViewAgenda,
            color = Color(0xFF1E88E5),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Screen.Quran) }
        )
        QuickHubItem(
            title = AppStrings.get("bookmarks", lang),
            icon = Icons.Default.Bookmark,
            color = QuranGold,
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Screen.Quran) }
        )
        QuickHubItem(
            title = AppStrings.get("prayer_times", lang),
            icon = Icons.Default.AccessTime,
            color = Color(0xFF00897B),
            modifier = Modifier.weight(1f),
            onClick = { onNavigate(Screen.Prayer) }
        )
    }
}

@Composable
private fun QuickHubItem(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = modifier.height(96.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DailyVerseCard(
    lang: AppLanguage,
    onReadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Ayatul Kursi (Surah Al-Baqarah 2:255)
    val arabicText = "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌۭ وَلَا نَوْمٌۭ ۚ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ"
    val bengaliText = "আল্লাহ, তিনি ছাড়া কোনো সত্য উপাস্য নেই; তিনি চিরঞ্জীব, সবকিছুর ধারক। তন্দ্রা বা নিদ্রা তাঁকে স্পর্শ করে না। আসমান ও যমীনে যা কিছু রয়েছে সবই তাঁর।"
    val englishText = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth."

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("daily_verse", lang),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = "Al-Baqarah 2:255",
                    style = MaterialTheme.typography.labelMedium.copy(color = QuranGold, fontWeight = FontWeight.SemiBold)
                )
            }

            // Arabic text
            Text(
                text = arabicText,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Right
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            // Translation text based on language
            val transText = if (lang == AppLanguage.BENGALI) bengaliText else englishText
            Text(
                text = transText,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = {
                            val clip = ClipData.newPlainText("Ayah", "$arabicText\n\n$transText\n(Surah Al-Baqarah 2:255)")
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, AppStrings.get("ayah_copied", lang), Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "$arabicText\n\n$transText\n(Surah Al-Baqarah 2:255 - Noor Islam)")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Verse"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = onReadMore,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = AppStrings.get("quran", lang),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
