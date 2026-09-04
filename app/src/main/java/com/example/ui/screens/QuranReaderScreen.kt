package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AyahEntity
import com.example.data.model.SurahEntity
import com.example.data.repository.AppLanguage
import com.example.data.repository.TranslationSelection
import com.example.ui.components.AyahNumberBadge
import com.example.ui.components.BismillahBanner
import com.example.ui.components.IslamicDivider
import com.example.ui.components.SurahHeaderBanner
import com.example.ui.strings.AppStrings
import com.example.ui.theme.QuranGold
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val lang = settings.appLanguage

    val surah by viewModel.currentSurah.collectAsState()
    val ayahs by viewModel.currentAyahs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val targetScrollAyah by viewModel.targetScrollAyah.collectAsState()

    val listState = rememberLazyListState()
    var isDistractionFree by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    // Scroll to target ayah if requested
    LaunchedEffect(targetScrollAyah, ayahs) {
        if (targetScrollAyah != null && ayahs.isNotEmpty()) {
            val index = ayahs.indexOfFirst { it.ayahNumber == targetScrollAyah }
            if (index >= 0) {
                // Account for header banner items
                listState.animateScrollToItem(index + 2)
            }
            viewModel.clearTargetScrollAyah()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Reader Top App Bar
        AnimatedVisibility(visible = !isDistractionFree) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = surah?.nameEnglish ?: "Quran",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (surah != null) {
                            Text(
                                text = "${surah?.nameArabic} • ${String.format(AppStrings.get("ayahs_count", lang), surah?.numberOfAyahs ?: 0)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Font Size quick control
                    IconButton(
                        onClick = { showFontSizeDialog = true },
                        modifier = Modifier.testTag("reader_font_size_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "Font Size"
                        )
                    }

                    // Distraction-free full-screen toggle
                    IconButton(
                        onClick = { isDistractionFree = !isDistractionFree },
                        modifier = Modifier.testTag("reader_fullscreen_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDistractionFree) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Distraction-free Mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }

        // Main Ayah List
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reader_ayah_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Item 0: Surah Header Card
                if (surah != null) {
                    item {
                        SurahHeaderBanner(
                            surah = surah!!,
                            lang = lang
                        )
                    }
                }

                // Item 1: Bismillah Banner (for all surahs except Surah 9 At-Tawbah)
                if (surah != null && surah!!.number != 9) {
                    item {
                        BismillahBanner()
                    }
                }

                // Ayahs list
                itemsIndexed(ayahs, key = { _, ayah -> "${ayah.surahNumber}_${ayah.ayahNumber}" }) { _, ayah ->
                    val isBookmarked = bookmarks.any {
                        it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.ayahNumber
                    }
                    val isLastReadAyah = lastRead?.surahNumber == ayah.surahNumber &&
                            lastRead?.ayahNumber == ayah.ayahNumber

                    AyahCard(
                        ayah = ayah,
                        surahName = surah?.nameEnglish ?: "Surah ${ayah.surahNumber}",
                        arabicFontSize = settings.arabicFontSize,
                        translationFontSize = settings.translationFontSize,
                        translationSelection = settings.translationSelection,
                        isBookmarked = isBookmarked,
                        isLastRead = isLastReadAyah,
                        lang = lang,
                        onToggleBookmark = {
                            viewModel.toggleBookmark(ayah.surahNumber, ayah.ayahNumber, isBookmarked)
                            val msg = if (isBookmarked) {
                                AppStrings.get("bookmark_removed", lang)
                            } else {
                                AppStrings.get("bookmark_added", lang)
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onSetLastRead = {
                            viewModel.updateLastRead(ayah.surahNumber, ayah.ayahNumber)
                            Toast.makeText(context, AppStrings.get("set_last_read", lang), Toast.LENGTH_SHORT).show()
                        },
                        onCopy = {
                            val textToCopy = buildString {
                                append(ayah.arabicText)
                                append("\n\n")
                                if (settings.translationSelection != TranslationSelection.ENGLISH_ONLY) {
                                    append(ayah.translationBn)
                                    append("\n\n")
                                }
                                if (settings.translationSelection != TranslationSelection.BENGALI_ONLY) {
                                    append(ayah.translationEn)
                                    append("\n\n")
                                }
                                append("— Surah ${surah?.nameEnglish ?: ""} [${ayah.surahNumber}:${ayah.ayahNumber}]")
                            }
                            val clip = ClipData.newPlainText("Ayah", textToCopy)
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, AppStrings.get("ayah_copied", lang), Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            val textToShare = buildString {
                                append(ayah.arabicText)
                                append("\n\n")
                                if (settings.translationSelection != TranslationSelection.ENGLISH_ONLY) {
                                    append(ayah.translationBn)
                                    append("\n\n")
                                }
                                if (settings.translationSelection != TranslationSelection.BENGALI_ONLY) {
                                    append(ayah.translationEn)
                                    append("\n\n")
                                }
                                append("— Surah ${surah?.nameEnglish ?: ""} [${ayah.surahNumber}:${ayah.ayahNumber}] • Noor Quran")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, textToShare)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Ayah"))
                        }
                    )
                }

                // Bottom Surah navigation buttons
                item {
                    SurahNavigationFooter(
                        currentSurahNumber = surah?.number ?: 1,
                        onPrevious = { viewModel.previousSurah() },
                        onNext = { viewModel.nextSurah() },
                        lang = lang
                    )
                }
            }

            // Distraction free exit floating badge
            if (isDistractionFree) {
                IconButton(
                    onClick = { isDistractionFree = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Exit Fullscreen"
                    )
                }
            }
        }
    }

    // Font Size Adjuster Dialog
    if (showFontSizeDialog) {
        FontSizeDialog(
            arabicFontSize = settings.arabicFontSize,
            translationFontSize = settings.translationFontSize,
            onArabicChange = { viewModel.setArabicFontSize(it) },
            onTranslationChange = { viewModel.setTranslationFontSize(it) },
            onDismiss = { showFontSizeDialog = false },
            lang = lang
        )
    }
}

@Composable
private fun AyahCard(
    ayah: AyahEntity,
    surahName: String,
    arabicFontSize: Float,
    translationFontSize: Float,
    translationSelection: TranslationSelection,
    isBookmarked: Boolean,
    isLastRead: Boolean,
    lang: AppLanguage,
    onToggleBookmark: () -> Unit,
    onSetLastRead: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLastRead) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("ayah_card_${ayah.ayahNumber}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Ayah Number Badge + Metadata Pills (Juz, Page, Sajda)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AyahNumberBadge(
                    number = ayah.ayahNumber,
                    badgeColor = if (isLastRead) QuranGold else MaterialTheme.colorScheme.primaryContainer,
                    textColor = if (isLastRead) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Juz Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Juz ${ayah.juz}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Page Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Pg ${ayah.page}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (ayah.sajda == 1) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(QuranGold.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Sajdah",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // Arabic Verse Text (Uthmani Script, Right-to-Left)
            Text(
                text = ayah.arabicText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = arabicFontSize.sp,
                    lineHeight = (arabicFontSize * 1.8f).sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Right
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("ayah_arabic_${ayah.ayahNumber}")
            )

            // Divider
            IslamicDivider()

            // Bengali Translation
            if (translationSelection == TranslationSelection.BENGALI_ONLY ||
                translationSelection == TranslationSelection.BOTH) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "বাংলা (মুহিউদ্দীন খান):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = ayah.translationBn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = translationFontSize.sp,
                            lineHeight = (translationFontSize * 1.5f).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // English Translation
            if (translationSelection == TranslationSelection.ENGLISH_ONLY ||
                translationSelection == TranslationSelection.BOTH) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "English (Saheeh International):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = ayah.translationEn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = translationFontSize.sp,
                            lineHeight = (translationFontSize * 1.5f).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Action toolbar (Bookmark, Copy, Share, Set Last Read)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Bookmark
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.testTag("btn_bookmark_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) QuranGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Copy
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.testTag("btn_copy_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Share
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.testTag("btn_share_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Last Read button / indicator
                TextButton(
                    onClick = onSetLastRead,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isLastRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.get("last_read", lang),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isLastRead) FontWeight.Bold else FontWeight.Normal,
                            color = if (isLastRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SurahNavigationFooter(
    currentSurahNumber: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentSurahNumber > 1) {
            OutlinedButton(
                onClick = onPrevious,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(AppStrings.get("previous_surah", lang))
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (currentSurahNumber < 114) {
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(AppStrings.get("next_surah", lang))
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun FontSizeDialog(
    arabicFontSize: Float,
    translationFontSize: Float,
    onArabicChange: (Float) -> Unit,
    onTranslationChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    lang: AppLanguage
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppStrings.get("quran_arabic_font_size", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text(
                        text = "${AppStrings.get("quran_arabic_font_size", lang)}: ${arabicFontSize.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = arabicFontSize,
                        onValueChange = onArabicChange,
                        valueRange = 20f..44f,
                        steps = 12,
                        colors = SliderDefaults.colors(thumbColor = QuranGold, activeTrackColor = QuranGold)
                    )
                    // Live preview of Arabic
                    Text(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                        fontSize = arabicFontSize.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column {
                    Text(
                        text = "${AppStrings.get("translation_font_size", lang)}: ${translationFontSize.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = translationFontSize,
                        onValueChange = onTranslationChange,
                        valueRange = 12f..24f,
                        steps = 6
                    )
                    // Live preview of translation
                    Text(
                        text = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                        fontSize = translationFontSize.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
