package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.repository.AppLanguage
import com.example.prayer.AsrJuristicMethod
import com.example.prayer.CityLocation
import com.example.prayer.DailyPrayerSchedule
import com.example.prayer.HijriDate
import com.example.prayer.PrayerCalculationMethod
import com.example.prayer.PrayerTime
import com.example.prayer.PrayerType
import com.example.ui.strings.AppStrings
import com.example.ui.theme.QuranGold
import com.example.ui.viewmodel.QuranViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PrayerScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val lang = settings.appLanguage

    val schedule by viewModel.prayerSchedule.collectAsState()
    val hijriDate by viewModel.hijriDate.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }
    var showAsrDialog by remember { mutableStateOf(false) }

    // GPS Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            detectGpsLocation(context, viewModel)
        } else {
            Toast.makeText(context, "Location permission denied. Using preset city.", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card with Hijri Date, Gregorian Date & Location
        item {
            PrayerHeroCard(
                schedule = schedule,
                hijriDate = hijriDate,
                lang = lang,
                onCityClick = { showCityDialog = true },
                onGpsClick = {
                    val hasFine = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasFine || hasCoarse) {
                        detectGpsLocation(context, viewModel)
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            )
        }

        // 2. Full Prayer Schedule Table
        item {
            PrayerScheduleTable(schedule = schedule, lang = lang)
        }

        // 3. Calculation Method & Asr Juristic Method Details
        item {
            CalculationMethodCard(
                schedule = schedule,
                lang = lang,
                onMethodClick = { showMethodDialog = true },
                onAsrClick = { showAsrDialog = true }
            )
        }

        // 4. Time format toggle (12h / 24h)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.get("time_format", lang),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (settings.is24Hour) "24-Hour (18:30)" else "12-Hour (6:30 PM)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.is24Hour,
                        onCheckedChange = { viewModel.setIs24Hour(it) }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    // Dialogs
    if (showCityDialog) {
        CitySelectionDialog(
            selectedCity = settings.selectedCity,
            onSelect = {
                viewModel.setSelectedCity(it)
                showCityDialog = false
            },
            onDismiss = { showCityDialog = false },
            lang = lang
        )
    }

    if (showMethodDialog) {
        MethodSelectionDialog(
            selectedMethod = settings.prayerCalculationMethod,
            onSelect = {
                viewModel.setPrayerCalculationMethod(it)
                showMethodDialog = false
            },
            onDismiss = { showMethodDialog = false },
            lang = lang
        )
    }

    if (showAsrDialog) {
        AsrMethodDialog(
            selectedMethod = settings.asrJuristicMethod,
            onSelect = {
                viewModel.setAsrJuristicMethod(it)
                showAsrDialog = false
            },
            onDismiss = { showAsrDialog = false },
            lang = lang
        )
    }
}

private fun detectGpsLocation(context: Context, viewModel: QuranViewModel) {
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc: Location? = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (loc != null) {
            viewModel.updateGpsLocation(loc.latitude, loc.longitude, "GPS (${loc.latitude.format(2)}, ${loc.longitude.format(2)})")
            Toast.makeText(context, "Location updated from GPS!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Could not acquire GPS fix. Please ensure location is enabled.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "GPS Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(Locale.US, this)

@Composable
private fun PrayerHeroCard(
    schedule: DailyPrayerSchedule,
    hijriDate: HijriDate,
    lang: AppLanguage,
    onCityClick: () -> Unit,
    onGpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        )
    )

    val remainingSec = (schedule.nextPrayerTimeRemainingMillis / 1000).coerceAtLeast(0)
    val hours = remainingSec / 3600
    val minutes = (remainingSec % 3600) / 60
    val durationText = if (hours > 0) "$hours hr $minutes min" else "$minutes min"

    val nextPrayerName = when (lang) {
        AppLanguage.BENGALI -> schedule.nextPrayer.displayNameBn
        AppLanguage.ARABIC -> schedule.nextPrayer.displayNameAr
        AppLanguage.ENGLISH -> schedule.nextPrayer.displayNameEn
    }

    val cityName = when (lang) {
        AppLanguage.BENGALI -> schedule.location.nameBn
        AppLanguage.ARABIC -> schedule.location.nameAr
        AppLanguage.ENGLISH -> schedule.location.nameEn
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .border(1.dp, QuranGold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Location selector header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onCityClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = QuranGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$cityName (${schedule.location.timeZoneId})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select City",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onGpsClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Detect GPS",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Next prayer announcement
                Column {
                    Text(
                        text = "${AppStrings.get("next_prayer", lang)}: $nextPrayerName",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = String.format(AppStrings.get("in_time", lang), durationText),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = QuranGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                // Hijri Date
                Text(
                    text = when (lang) {
                        AppLanguage.BENGALI -> hijriDate.formatBn()
                        AppLanguage.ARABIC -> hijriDate.formatAr()
                        AppLanguage.ENGLISH -> hijriDate.formatEn()
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun PrayerScheduleTable(
    schedule: DailyPrayerSchedule,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = AppStrings.get("prayer_times", lang),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            schedule.prayerList.forEachIndexed { index, prayer ->
                val isNext = prayer.type == schedule.nextPrayer
                val isCurrent = prayer.type == schedule.currentPrayer

                val name = when (lang) {
                    AppLanguage.BENGALI -> prayer.type.displayNameBn
                    AppLanguage.ARABIC -> prayer.type.displayNameAr
                    AppLanguage.ENGLISH -> prayer.type.displayNameEn
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isNext) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else Color.Transparent
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isNext) QuranGold
                                    else if (isCurrent) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                )
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Text(
                        text = prayer.timeFormatted,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isNext) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                if (index < schedule.prayerList.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculationMethodCard(
    schedule: DailyPrayerSchedule,
    lang: AppLanguage,
    onMethodClick: () -> Unit,
    onAsrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Method & Fiqh Configuration",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Calculation Method Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onMethodClick() }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.get("prayer_calculation_method", lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> schedule.calculationMethod.displayNameBn
                            AppLanguage.ARABIC -> schedule.calculationMethod.displayNameAr
                            AppLanguage.ENGLISH -> schedule.calculationMethod.displayNameEn
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Change")
            }

            HorizontalDivider()

            // Asr Method Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onAsrClick() }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.get("asr_calculation_method", lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.BENGALI -> schedule.asrMethod.displayNameBn
                            AppLanguage.ARABIC -> schedule.asrMethod.displayNameAr
                            AppLanguage.ENGLISH -> schedule.asrMethod.displayNameEn
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Change")
            }
        }
    }
}

@Composable
private fun CitySelectionDialog(
    selectedCity: CityLocation,
    onSelect: (CityLocation) -> Unit,
    onDismiss: () -> Unit,
    lang: AppLanguage
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("select_city", lang)) },
        text = {
            LazyColumn(modifier = Modifier.height(320.dp)) {
                items(CityLocation.PRESET_CITIES.size) { i ->
                    val city = CityLocation.PRESET_CITIES[i]
                    val isSelected = city.nameEn == selectedCity.nameEn
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(city) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            val cName = when (lang) {
                                AppLanguage.BENGALI -> city.nameBn
                                AppLanguage.ARABIC -> city.nameAr
                                AppLanguage.ENGLISH -> city.nameEn
                            }
                            Text(
                                text = "$cName (${city.country})",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "TZ: ${city.timeZoneId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun MethodSelectionDialog(
    selectedMethod: PrayerCalculationMethod,
    onSelect: (PrayerCalculationMethod) -> Unit,
    onDismiss: () -> Unit,
    lang: AppLanguage
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("prayer_calculation_method", lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PrayerCalculationMethod.entries.forEach { method ->
                    val isSelected = method == selectedMethod
                    val name = when (lang) {
                        AppLanguage.BENGALI -> method.displayNameBn
                        AppLanguage.ARABIC -> method.displayNameAr
                        AppLanguage.ENGLISH -> method.displayNameEn
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(method) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { onSelect(method) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun AsrMethodDialog(
    selectedMethod: AsrJuristicMethod,
    onSelect: (AsrJuristicMethod) -> Unit,
    onDismiss: () -> Unit,
    lang: AppLanguage
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.get("asr_calculation_method", lang)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AsrJuristicMethod.entries.forEach { method ->
                    val isSelected = method == selectedMethod
                    val name = when (lang) {
                        AppLanguage.BENGALI -> method.displayNameBn
                        AppLanguage.ARABIC -> method.displayNameAr
                        AppLanguage.ENGLISH -> method.displayNameEn
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(method) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isSelected, onClick = { onSelect(method) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
