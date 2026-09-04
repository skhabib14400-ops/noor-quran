package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AppLanguage
import com.example.ui.strings.AppStrings
import com.example.ui.theme.QuranGold
import com.example.ui.viewmodel.QuranViewModel

data class AllahNameItem(
    val id: Int,
    val arabic: String,
    val transliteration: String,
    val meaningBn: String,
    val meaningEn: String
)

val NAMES_OF_ALLAH_LIST = listOf(
    AllahNameItem(1, "الرَّحْمَٰنُ", "Ar-Rahman", "পরম দয়ালু", "The Beneficent"),
    AllahNameItem(2, "الرَّحِيمُ", "Ar-Raheem", "অতি দয়ালু", "The Merciful"),
    AllahNameItem(3, "الْمَلِكُ", "Al-Malik", "সার্বভৌম ক্ষমতার অধিকারী", "The King"),
    AllahNameItem(4, "الْقُدُّوسُ", "Al-Quddus", "মহাপবিত্র", "The Most Sacred"),
    AllahNameItem(5, "السَّلَامُ", "As-Salam", "শান্তিদাতা", "The Source of Peace"),
    AllahNameItem(6, "الْمُؤْمِنُ", "Al-Mu'min", "নিরাপত্তা দানকারী", "The Granter of Security"),
    AllahNameItem(7, "الْمُهَيْمِنُ", "Al-Muhaymin", "রক্ষক ও তত্ত্বাবধায়ক", "The Guardian"),
    AllahNameItem(8, "الْعَزِيزُ", "Al-Aziz", "পরাক্রমশালী", "The All Mighty"),
    AllahNameItem(9, "الْجَبَّارُ", "Al-Jabbar", "সর্বশক্তিমান প্রতাপশালী", "The Compeller"),
    AllahNameItem(10, "الْمُتَكَبِّرُ", "Al-Mutakabbir", "সর্বশ্রেষ্ঠ মহিমান্বিত", "The Supreme"),
    AllahNameItem(11, "الْخَالِقُ", "Al-Khaliq", "সৃষ্টিকর্তা", "The Creator"),
    AllahNameItem(12, "الْبَارِئُ", "Al-Bari'", "সৃষ্টির রূপদানকারী", "The Evolver"),
    AllahNameItem(13, "الْمُصَوِّرُ", "Al-Musawwir", "সৌন্দর্য ও আকৃতি দানকারী", "The Fashioner"),
    AllahNameItem(14, "الْغَفَّارُ", "Al-Ghaffar", "মহাক্ষমাশীল", "The Constant Forgiver"),
    AllahNameItem(15, "الْقَهَّارُ", "Al-Qahhar", "কঠোর পরাক্রমশালী", "The Subduer"),
    AllahNameItem(16, "الْوَهَّابُ", "Al-Wahhab", "দানশীল", "The Bestower"),
    AllahNameItem(17, "الرَّزَّاقُ", "Ar-Razzaq", "রিযিকদাতা", "The Provider"),
    AllahNameItem(18, "الْفَتَّاحُ", "Al-Fattah", "বিজয় ও রহমতের দ্বার উন্মোচনকারী", "The Opener"),
    AllahNameItem(19, "الْعَلِيمُ", "Al-Alim", "সর্বজ্ঞ", "The All-Knowing"),
    AllahNameItem(20, "الْقَابِضُ", "Al-Qabid", "সংকোচনকারী", "The Restrainer"),
    AllahNameItem(21, "الْبَاسِطُ", "Al-Basit", "সম্প্রসারণকারী", "The Extender"),
    AllahNameItem(22, "الْخَافِضُ", "Al-Khafid", "অবনমনকারী", "The Reducer"),
    AllahNameItem(23, "الرَّافِعُ", "Ar-Rafi", "উন্নীতকারী", "The Exalter"),
    AllahNameItem(24, "الْمُعِزُّ", "Al-Mu'izz", "সম্মানদানকারী", "The Honourer"),
    AllahNameItem(25, "الْمُذِلُّ", "Al-Mudhill", "অপমানকারী", "The Humiliator"),
    AllahNameItem(26, "السَّمِيعُ", "As-Sami", "সর্বশ্রোতা", "The All-Hearing"),
    AllahNameItem(27, "الْبَصِيرُ", "Al-Basir", "সর্বদ্রষ্টা", "The All-Seeing"),
    AllahNameItem(28, "الْحَكَمُ", "Al-Hakam", "অনুপম বিচারক", "The Judge"),
    AllahNameItem(29, "الْعَدْلُ", "Al-Adl", "পরম ন্যায়পরায়ণ", "The Utterly Just"),
    AllahNameItem(30, "اللَّطِيفُ", "Al-Latif", "সুক্ষ্মদর্শী ও সদয়", "The Subtle One"),
    AllahNameItem(31, "الْخَبِيرُ", "Al-Khabir", "সর্বজ্ঞাতা", "The All-Aware"),
    AllahNameItem(32, "الْحَلِيمُ", "Al-Halim", "অসীম ধৈর্যশীল", "The Most Forbearing"),
    AllahNameItem(33, "الْعَظِيمُ", "Al-Azim", "সুমহান", "The Magnificent"),
    AllahNameItem(34, "الْغَفُورُ", "Al-Ghafur", "মার্জনাকারী", "The Forgiving"),
    AllahNameItem(35, "الشَّكُورُ", "Ash-Shakur", "কৃতজ্ঞতা গ্রহণকারী", "The Most Appreciative"),
    AllahNameItem(36, "الْعَلِيُّ", "Al-Ali", "সর্বোচ্চ", "The Most High"),
    AllahNameItem(37, "الْكَبِيرُ", "Al-Kabir", "সর্বশ্রেষ্ঠ", "The Most Great"),
    AllahNameItem(38, "الْحَفِيظُ", "Al-Hafiz", "মহাহেফাজতকারী", "The Preserver"),
    AllahNameItem(39, "الْمُقِيتُ", "Al-Muqit", "শক্তি ও পুষ্টিদাতা", "The Sustainer"),
    AllahNameItem(40, "الْحَسِيبُ", "Al-Hasib", "হিসাব গ্রহণকারী", "The Reckoner"),
    AllahNameItem(41, "الْجَلِيلُ", "Al-Jalil", "মহিমান্বিত", "The Sublime"),
    AllahNameItem(42, "الْكَرِيمُ", "Al-Karim", "পরম উদার", "The Bountiful"),
    AllahNameItem(43, "الرَّقِيبُ", "Ar-Raqib", "চির জাগ্রত পরিদর্শক", "The Watchful"),
    AllahNameItem(44, "الْمُجِيبُ", "Al-Mujib", "প্রার্থনায় সাড়া দানকারী", "The Responsive"),
    AllahNameItem(45, "الْوَاسِعُ", "Al-Wasi", "সর্বব্যাপী", "The All-Encompassing"),
    AllahNameItem(46, "الْحَكِيمُ", "Al-Hakim", "মহাপ্রজ্ঞাময়", "The All-Wise"),
    AllahNameItem(47, "الْوَدُودُ", "Al-Wadud", "স্নেহময়", "The Loving"),
    AllahNameItem(48, "الْمَجِيدُ", "Al-Majid", "মর্যাদাবান", "The Glorious"),
    AllahNameItem(49, "الْبَاعِثُ", "Al-Ba'ith", "পুনরুত্থানকারী", "The Resurrector"),
    AllahNameItem(50, "الشَّهِيدُ", "Ash-Shahid", "সর্বত্র উপস্থিত প্রত্যক্ষদর্শী", "The Witness"),
    AllahNameItem(51, "الْحَقُّ", "Al-Haqq", "চিরন্তন সত্য", "The Truth"),
    AllahNameItem(52, "الْوَكِيلُ", "Al-Wakil", "উত্তম অভিভাবক", "The Trustee"),
    AllahNameItem(53, "الْقَوِيُّ", "Al-Qawi", "মহা পরাক্রমশালী", "The Possessor of All Strength"),
    AllahNameItem(54, "الْمَتِينُ", "Al-Matin", "সুদৃঢ় ও অটল", "The Firm"),
    AllahNameItem(55, "الْوَلِيُّ", "Al-Wali", "পরম বন্ধু ও সাহায্যকারী", "The Protecting Friend"),
    AllahNameItem(56, "الْحَمِيدُ", "Al-Hamid", "চির প্রশংসিত", "The Praiseworthy"),
    AllahNameItem(57, "الْمُحْصِي", "Al-Muhsi", "সবকিছু পরিমাপকারী", "The Appraiser"),
    AllahNameItem(58, "الْمُبْدِئُ", "Al-Mubdi", "প্রথম সৃষ্টিকর্তা", "The Originator"),
    AllahNameItem(59, "الْمُعِيدُ", "Al-Mu'id", "পুনরায় সৃষ্টিকারী", "The Restorer"),
    AllahNameItem(60, "الْمُحْيِي", "Al-Muhyi", "জীবনদানকারী", "The Giver of Life"),
    AllahNameItem(61, "الْمُمِيتُ", "Al-Mumit", "মৃত্যুদানকারী", "The Destroyer of Life"),
    AllahNameItem(62, "الْحَيُّ", "Al-Hayy", "চিরঞ্জীব", "The Ever-Living"),
    AllahNameItem(63, "الْقَيُّومُ", "Al-Qayyum", "চিরস্থায়ী বিশ্বনিয়ন্তা", "The Self-Subsisting"),
    AllahNameItem(64, "الْوَاجِدُ", "Al-Wajid", "অভাবমুক্ত", "The Perceiver"),
    AllahNameItem(65, "الْمَاجِدُ", "Al-Majid", "মহিমান্বিত", "The Illustrious"),
    AllahNameItem(66, "الْوَاحِدُ", "Al-Wahid", "এক ও অদ্বিতীয়", "The One"),
    AllahNameItem(67, "الْأَحَدُ", "Al-Ahad", "একক ও অতুলনীয়", "The Unique"),
    AllahNameItem(68, "الصَّمَدُ", "As-Samad", "অমুখাপেক্ষী", "The Eternal Refuge"),
    AllahNameItem(69, "الْقَادِرُ", "Al-Qadir", "সর্বশক্তিমান", "The Capable"),
    AllahNameItem(70, "الْمُقْتَدِرُ", "Al-Muqtadir", "সবকিছুর নিয়ন্ত্রক", "The Omnipotent"),
    AllahNameItem(71, "الْمُقَدِّمُ", "Al-Muqaddim", "অগ্রবর্তীকারী", "The Expediter"),
    AllahNameItem(72, "الْمُؤَخِّرُ", "Al-Mu'akhkhir", "পশ্চাদবর্তীকারী", "The Delayer"),
    AllahNameItem(73, "الْأَوَّلُ", "Al-Awwal", "আদিহীন প্রথম", "The First"),
    AllahNameItem(74, "الْآخِرُ", "Al-Akhir", "অন্তহীন শেষ", "The Last"),
    AllahNameItem(75, "الظَّاهِرُ", "Az-Zahir", "প্রকাশ্য", "The Manifest"),
    AllahNameItem(76, "الْبَاطِنُ", "Al-Batin", "অদৃশ্য ও গুপ্ত", "The Hidden"),
    AllahNameItem(77, "الْوَالِي", "Al-Wali", "একচ্ছত্র মালিক ও অভিভাবক", "The Sole Governor"),
    AllahNameItem(78, "الْمُتَعَالِي", "Al-Muta'ali", "সর্বোচ্চ মর্যাদাবান", "The Most Exalted"),
    AllahNameItem(79, "الْبَرُّ", "Al-Barr", "পরম কল্যাণকারী", "The Source of All Goodness"),
    AllahNameItem(80, "التَّوَّابُ", "At-Tawwab", "তওবা কবুলকারী", "The Ever-Pardoning"),
    AllahNameItem(81, "الْمُنْتَقِمُ", "Al-Muntaqim", "প্রতিশোধ গ্রহণকারী", "The Avenger"),
    AllahNameItem(82, "الْعَفُوُّ", "Al-Afuww", "মহাক্ষমাশীল", "The Pardoner"),
    AllahNameItem(83, "الرَّءُوفُ", "Ar-Ra'uf", "পরম স্নেহশীল ও দয়ার্দ্র", "The Most Kind"),
    AllahNameItem(84, "مَالِكُ الْمُلْكِ", "Malik-ul-Mulk", "সমগ্র রাজত্বের চিরস্থায়ী মালিক", "Master of the Kingdom"),
    AllahNameItem(85, "ذُو الْجَلَالِ وَالْإِكْرَامِ", "Dhul-Jalali wal-Ikram", "মহিমান্বিত ও পরম সম্মানিত", "Lord of Glory and Honour"),
    AllahNameItem(86, "الْمُقْسِطُ", "Al-Muqsit", "ন্যায়নিষ্ঠ", "The Equitable"),
    AllahNameItem(87, "الْجَامِعُ", "Al-Jami", "একত্রকারী", "The Gatherer"),
    AllahNameItem(88, "الْغَنِيُّ", "Al-Ghani", "স্বয়ংসম্পূর্ণ প্রাচুর্যময়", "The Self-Sufficient"),
    AllahNameItem(89, "الْمُغْنِي", "Al-Mughni", "প্রাচুর্য দানকারী", "The Enricher"),
    AllahNameItem(90, "الْمَانِعُ", "Al-Mani", "নিবারণকারী", "The Preventer"),
    AllahNameItem(91, "الضَّارُّ", "Ad-Darr", "ক্ষতিসাধনকারী", "The Distressor"),
    AllahNameItem(92, "النَّافِعُ", "An-Nafi", "উপকারসাধনকারী", "The Benefactor"),
    AllahNameItem(93, "النُّورُ", "An-Nur", "আলো ও জ্যোতির্ময়", "The Light"),
    AllahNameItem(94, "الْهَادِي", "Al-Hadi", "সঠিক পথপ্রদর্শক", "The Guide"),
    AllahNameItem(95, "الْبَدِيعُ", "Al-Badi", "অনুপম সৃষ্টির সূচনাকারী", "The Incomparable"),
    AllahNameItem(96, "الْبَاقِي", "Al-Baqi", "চিরন্তন চিরস্থায়ী", "The Everlasting"),
    AllahNameItem(97, "الْوَارِثُ", "Al-Warith", "সর্বস্বত্বের চূড়ান্ত অধিকারী", "The Inheritor"),
    AllahNameItem(98, "الرَّشِيدُ", "Ar-Rashid", "সঠিক পথের নির্দেশক", "The Righteous Teacher"),
    AllahNameItem(99, "الصَّبُورُ", "As-Sabur", "মহা ধৈর্যশীল", "The Patient")
)

@Composable
fun NamesOfAllahScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.userSettings.collectAsState()
    val lang = settings.appLanguage

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = AppStrings.get("names_of_allah", lang),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "وَلِلَّهِ ٱلْأَسْمَآءُ ٱلْحُسْنَىٰ فَٱدْعُوهُ بِهَا",
                    style = MaterialTheme.typography.bodyMedium.copy(color = QuranGold),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(NAMES_OF_ALLAH_LIST) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Number circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${item.id}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        Text(
                            text = item.arabic,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = item.transliteration,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        val meaning = when (lang) {
                            AppLanguage.BENGALI -> item.meaningBn
                            AppLanguage.ARABIC -> item.arabic
                            AppLanguage.ENGLISH -> item.meaningEn
                        }
                        Text(
                            text = meaning,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
