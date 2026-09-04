package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppLanguage
import com.example.ui.strings.AppStrings
import com.example.ui.theme.QuranGold
import com.example.ui.viewmodel.QuranViewModel

data class DuaItem(
    val id: Int,
    val category: String,
    val categoryBn: String,
    val titleBn: String,
    val titleEn: String,
    val arabic: String,
    val transliteration: String,
    val translationBn: String,
    val translationEn: String,
    val reference: String
)

val DUA_COLLECTION = listOf(
    DuaItem(
        id = 1,
        category = "Morning & Evening",
        categoryBn = "সকাল-সন্ধ্যার জিকির",
        titleBn = "সাইয়্যিদুল ইস্তিগফার (শ্রেষ্ঠ ক্ষমা প্রার্থনা)",
        titleEn = "Sayyidul Istighfar",
        arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ خَلَقْتَنِي وَأَنَا عَبْدُكَ وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
        transliteration = "Allahumma anta Rabbi la ilaha illa Anta, Khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu...",
        translationBn = "হে আল্লাহ! আপনি আমার রব, আপনি ছাড়া কোনো সত্য উপাস্য নেই। আপনি আমাকে সৃষ্টি করেছেন এবং আমি আপনার বান্দা...",
        translationEn = "O Allah, You are my Lord, none has the right to be worshiped but You. You created me and I am Your slave...",
        reference = "সহীহ বুখারী: ৬৩০৬"
    ),
    DuaItem(
        id = 2,
        category = "Morning & Evening",
        categoryBn = "সকাল-সন্ধ্যার জিকির",
        titleBn = "আয়াতে কুরসী",
        titleEn = "Ayat al-Kursi",
        arabic = "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ ۚ لَا تَأْخُذُهُۥ سِنَةٌۭ وَلَا نَوْمٌۭ ۚ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ ۗ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَىْءٍۢ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ ۚ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ ۖ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا ۚ وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ",
        transliteration = "Allahu la ilaha illa Huwa, Al-Hayyul-Qayyum...",
        translationBn = "আল্লাহ, তিনি ছাড়া কোনো সত্য উপাস্য নেই। তিনি চিরঞ্জীব, চিরস্থায়ী নিয়ন্ত্রক...",
        translationEn = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of existence...",
        reference = "সূরা আল-বাকারা: ২৫৫"
    ),
    DuaItem(
        id = 3,
        category = "Daily Prayers",
        categoryBn = "দৈনন্দিন দোয়া",
        titleBn = "ঘুমানোর পূর্বে দোয়া",
        titleEn = "Before Sleeping",
        arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
        transliteration = "Bismika Allahumma amutu wa ahya",
        translationBn = "হে আল্লাহ! আপনার নাম নিয়েই আমি মৃত্যুবরণ করি (ঘুমাই) এবং জীবিত হই (জাগ্রত হই)।",
        translationEn = "In Your Name, O Allah, I die and I live.",
        reference = "সহীহ বুখারী: ৬৩১২"
    ),
    DuaItem(
        id = 4,
        category = "Daily Prayers",
        categoryBn = "দৈনন্দিন দোয়া",
        titleBn = "ঘুম থেকে জাগ্রত হওয়ার দোয়া",
        titleEn = "Upon Waking Up",
        arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
        transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushur",
        translationBn = "সমস্ত প্রশংসা আল্লাহর জন্য, যিনি মৃত্যুর (ঘুমের) পর আমাদের জীবন দান করলেন এবং তাঁর দিকেই সবার প্রত্যাবর্তন।",
        translationEn = "All praise is for Allah who gave us life after having taken it from us, and unto Him is the resurrection.",
        reference = "সহীহ বুখারী: ৬৩১২"
    ),
    DuaItem(
        id = 5,
        category = "Daily Prayers",
        categoryBn = "দৈনন্দিন দোয়া",
        titleBn = "খাওয়ার শুরুর দোয়া",
        titleEn = "Before Eating",
        arabic = "بِسْمِ اللَّهِ",
        transliteration = "Bismillah",
        translationBn = "আল্লাহর নামে শুরু করছি।",
        translationEn = "In the name of Allah.",
        reference = "সহীহ বুখারী: ৫৩৭৬"
    ),
    DuaItem(
        id = 6,
        category = "Fasting & Ramadan",
        categoryBn = "রোজা ও রমজান",
        titleBn = "ইফতারের দোয়া",
        titleEn = "Dua for Iftar",
        arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ",
        transliteration = "Dhahabadh-dhama'u wabtallatil-'uruqu wa thabatal-ajru in sha Allah",
        translationBn = "পিপাসা নিবারিত হলো, শিরা-উপশিরা সিক্ত হলো এবং ইনশাআল্লাহ সওয়াব নির্ধারিত হলো।",
        translationEn = "The thirst is gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
        reference = "আবু দাউদ: ২৩৫৭"
    ),
    DuaItem(
        id = 7,
        category = "Forgiveness & Protection",
        categoryBn = "ক্ষমা ও নিরাপত্তা",
        titleBn = "বিপদ ও দুশ্চিন্তা মুক্তির দোয়া (ইউনুস আ. এর দোয়া)",
        titleEn = "Dua of Yunus (A.S.) in Distress",
        arabic = "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ",
        transliteration = "La ilaha illa anta subhanaka inni kuntu minaz-zalimin",
        translationBn = "তুমি ছাড়া কোনো সত্য উপাস্য নেই, তুমি মহাপবিত্র। নিশ্চয়ই আমি অত্যাচারীদের অন্তর্ভুক্ত ছিলাম।",
        translationEn = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers.",
        reference = "সূরা আল-আম্বিয়া: ৮৭"
    )
)

@Composable
fun DuaScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val lang = settings.appLanguage

    val categories = remember {
        listOf("All") + DUA_COLLECTION.map { it.category }.distinct()
    }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredList = remember(selectedCategory) {
        if (selectedCategory == "All") DUA_COLLECTION
        else DUA_COLLECTION.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Category Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                val label = if (cat == "All") {
                    if (lang == AppLanguage.BENGALI) "সব দোয়া" else if (lang == AppLanguage.ARABIC) "الكل" else "All"
                } else {
                    val item = DUA_COLLECTION.firstOrNull { it.category == cat }
                    if (lang == AppLanguage.BENGALI) item?.categoryBn ?: cat else cat
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    tonalElevation = if (isSelected) 4.dp else 1.dp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Dua List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredList) { dua ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == AppLanguage.BENGALI) dua.titleBn else dua.titleEn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Dua", "${dua.arabic}\n\n${dua.translationBn}")
                                    cm.setPrimaryClip(clip)
                                    Toast.makeText(context, AppStrings.get("ayah_copied", lang), Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = QuranGold
                                )
                            }
                        }

                        Text(
                            text = dua.arabic,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 32.sp
                            ),
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = dua.transliteration,
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (lang == AppLanguage.BENGALI) dua.translationBn else dua.translationEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = dua.reference,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = QuranGold
                        )
                    }
                }
            }
        }
    }
}
