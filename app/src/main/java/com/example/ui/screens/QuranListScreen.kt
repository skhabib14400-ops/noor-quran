package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookmarkEntity
import com.example.data.model.JuzInfo
import com.example.data.model.SurahEntity
import com.example.data.repository.AppLanguage
import com.example.ui.components.AyahNumberBadge
import com.example.ui.strings.AppStrings
import com.example.ui.theme.QuranGold
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun QuranListScreen(
    viewModel: QuranViewModel,
    onOpenSurah: (surahNumber: Int, targetAyah: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val lang = settings.appLanguage

    val surahs by viewModel.allSurahs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var filterQuery by remember { mutableStateOf("") }

    val filteredSurahs = remember(surahs, filterQuery) {
        if (filterQuery.isBlank()) {
            surahs
        } else {
            val q = filterQuery.trim().lowercase()
            surahs.filter { s ->
                s.nameEnglish.lowercase().contains(q) ||
                s.englishMeaning.lowercase().contains(q) ||
                s.nameBengali.lowercase().contains(q) ||
                s.bengaliMeaning.lowercase().contains(q) ||
                s.nameArabic.contains(q) ||
                s.number.toString() == q
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "${AppStrings.get("surahs", lang)} (114)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                modifier = Modifier.testTag("tab_surahs")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "${AppStrings.get("juz", lang)} (30)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                modifier = Modifier.testTag("tab_juz")
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = {
                    Text(
                        text = "${AppStrings.get("bookmarks", lang)} (${bookmarks.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                modifier = Modifier.testTag("tab_bookmarks")
            )
        }

        when (selectedTabIndex) {
            0 -> {
                // Surah Filter Box
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = {
                        Text(
                            text = AppStrings.get("search_hint", lang),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (filterQuery.isNotEmpty()) {
                            IconButton(onClick = { filterQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("surah_filter_input")
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSurahs, key = { it.number }) { surah ->
                        SurahListItem(
                            surah = surah,
                            lang = lang,
                            onClick = { onOpenSurah(surah.number, 1) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(JuzInfo.ALL_JUZ, key = { it.juzNumber }) { juz ->
                        val startSurah = surahs.find { it.number == juz.startSurah }
                        val endSurah = surahs.find { it.number == juz.endSurah }

                        JuzListItem(
                            juz = juz,
                            startSurahName = startSurah?.nameEnglish ?: "Surah ${juz.startSurah}",
                            endSurahName = endSurah?.nameEnglish ?: "Surah ${juz.endSurah}",
                            lang = lang,
                            onClick = {
                                viewModel.openJuz(juz.juzNumber)
                                onOpenSurah(juz.startSurah, juz.startAyah)
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
            2 -> {
                if (bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = AppStrings.get("no_bookmarks", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = AppStrings.get("no_bookmarks_desc", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(bookmarks, key = { it.id }) { bookmark ->
                            val surah = surahs.find { it.number == bookmark.surahNumber }
                            BookmarkListItem(
                                bookmark = bookmark,
                                surah = surah,
                                lang = lang,
                                onOpen = { onOpenSurah(bookmark.surahNumber, bookmark.ayahNumber) },
                                onDelete = { viewModel.toggleBookmark(bookmark.surahNumber, bookmark.ayahNumber, true) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahListItem(
    surah: SurahEntity,
    lang: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("surah_item_${surah.number}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                AyahNumberBadge(
                    number = surah.number,
                    badgeColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val displayName = if (lang == AppLanguage.BENGALI && surah.nameBengali.isNotBlank()) {
                        "${surah.nameBengali} • ${surah.bengaliMeaning}"
                    } else {
                        "${surah.nameEnglish} • ${surah.englishMeaning}"
                    }

                    Text(
                        text = surah.nameEnglish,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val revText = if (surah.revelationType.equals("Meccan", ignoreCase = true)) {
                            AppStrings.get("meccan", lang)
                        } else {
                            AppStrings.get("madinan", lang)
                        }
                        Text(
                            text = revText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = String.format(AppStrings.get("ayahs_count", lang), surah.numberOfAyahs),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Text(
                text = surah.nameArabic,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Right
            )
        }
    }
}

@Composable
private fun JuzListItem(
    juz: JuzInfo,
    startSurahName: String,
    endSurahName: String,
    lang: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                AyahNumberBadge(
                    number = juz.juzNumber,
                    badgeColor = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = String.format(AppStrings.get("juz_number", lang), juz.juzNumber),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$startSurahName (${juz.startAyah}) - $endSurahName (${juz.endAyah})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = juz.nameArabic,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = QuranGold
                ),
                textAlign = TextAlign.Right
            )
        }
    }
}

@Composable
private fun BookmarkListItem(
    bookmark: BookmarkEntity,
    surah: SurahEntity?,
    lang: AppLanguage,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                AyahNumberBadge(
                    number = bookmark.surahNumber,
                    badgeColor = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val sName = if (lang == AppLanguage.BENGALI && surah?.nameBengali?.isNotBlank() == true) {
                        surah.nameBengali
                    } else {
                        surah?.nameEnglish ?: "Surah ${bookmark.surahNumber}"
                    }

                    Text(
                        text = "$sName : ${bookmark.ayahNumber}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = surah?.nameArabic ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.BookmarkRemove,
                    contentDescription = "Remove Bookmark",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
