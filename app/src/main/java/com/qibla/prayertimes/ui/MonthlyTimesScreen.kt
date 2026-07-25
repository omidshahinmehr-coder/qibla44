package com.qibla.prayertimes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.LocalLayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.CityStore
import com.qibla.prayertimes.data.MonthPrayerTimes
import com.qibla.prayertimes.data.MonthlyPrayerTimesRepository
import com.qibla.prayertimes.model.defaultCities
import com.qibla.prayertimes.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar

private val TABLE_COLUMNS = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

@Composable
private fun columnLabels(): Map<String, String> = mapOf(
    "Fajr" to stringResource(R.string.col_fajr),
    "Dhuhr" to stringResource(R.string.col_dhuhr),
    "Asr" to stringResource(R.string.col_asr),
    "Maghrib" to stringResource(R.string.col_maghrib),
    "Isha" to stringResource(R.string.col_isha)
)

@Composable
fun MonthlyTimesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val city = remember { CityStore(context).loadSelectedCity() ?: defaultCities(context).first() }
    val repo = remember { MonthlyPrayerTimesRepository() }
    val columnLabels = columnLabels()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val now = remember { Calendar.getInstance() }
    var year by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var month by remember { mutableIntStateOf(now.get(Calendar.MONTH) + 1) }
    val todayDay = if (year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH) + 1)
        now.get(Calendar.DAY_OF_MONTH) else -1

    var data by remember { mutableStateOf<MonthPrayerTimes?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(year, month) {
        loading = true
        data = repo.fetchMonth(city.lat, city.lon, year, month)
        loading = false
    }

    val monthLabel = remember(year, month) {
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }
        SimpleDateFormat("MMMM yyyy", context.resources.configuration.locales[0]).format(cal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NightMid)
            .padding(horizontal = 16.dp)
            .padding(top = 28.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = AmberText)
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(stringResource(R.string.monthly_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(city.name, color = AmberMuted, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (month == 1) { month = 12; year -= 1 } else month -= 1
            }) {
                Icon(
                    if (isRtl) Icons.Filled.ChevronRight else Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.prev_month),
                    tint = BrassLight
                )
            }
            Text(monthLabel, color = AmberText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            IconButton(onClick = {
                if (month == 12) { month = 1; year += 1 } else month += 1
            }) {
                Icon(
                    if (isRtl) Icons.Filled.ChevronLeft else Icons.Filled.ChevronRight,
                    contentDescription = stringResource(R.string.next_month),
                    tint = BrassLight
                )
            }
        }

        if (data?.isOffline == true) {
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.monthly_offline_note),
                color = Color(0xFFF0C9C9),
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brass)
            }
        } else {
            val days = data?.days.orEmpty()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardSurface)
            ) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1AC9A15C))
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Text(stringResource(R.string.col_day), color = AmberMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                    TABLE_COLUMNS.forEach { key ->
                        Text(
                            columnLabels[key] ?: key,
                            color = AmberMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                LazyColumn(modifier = Modifier.heightIn(max = 480.dp)) {
                    items(days) { dayEntry ->
                        val isToday = dayEntry.day == todayDay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isToday) Color(0x26C9A15C) else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${dayEntry.day}",
                                color = if (isToday) BrassLight else AmberText,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.width(32.dp)
                            )
                            TABLE_COLUMNS.forEach { key ->
                                Text(
                                    dayEntry.timings[key] ?: "--:--",
                                    color = if (isToday) AmberText else AmberText.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
