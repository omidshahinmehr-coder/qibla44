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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// مسیر صحیح در Compose جدید
import androidx.compose.foundation.layout.LocalLayoutDirection
import androidx.compose.foundation.layout.LayoutDirection

import com.qibla.prayertimes.model.CityStore
import com.qibla.prayertimes.model.defaultCities
import com.qibla.prayertimes.model.MonthlyPrayerTimesRepository
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

val todayDay =
if (year == now.get(Calendar.YEAR) && month == now.get(Calendar.MONTH) + 1)
now.get(Calendar.DAY_OF_MONTH)
else -1

val monthData = remember(year, month) {
repo.getMonthlyTimes(city, year, month)
}

Column(
modifier = Modifier
.fillMaxSize()
.background(MaterialTheme.colorScheme.background)
) {

// Header
Row(
modifier = Modifier
.fillMaxWidth()
.padding(16.dp),
verticalAlignment = Alignment.CenterVertically
) {

IconButton(onClick = onBack) {
Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
}

Spacer(modifier = Modifier.weight(1f))

IconButton(onClick = {
if (month == 1) {
month = 12
year--
} else month--
}) {
Icon(Icons.Filled.ChevronLeft, contentDescription = null)
}

Text(
text = "month",
style = MaterialTheme.typography.titleMedium,
fontWeight = FontWeight.Bold
)

IconButton(onClick = {
if (month == 12) {
month = 1
year++
} else month++
}) {
Icon(Icons.Filled.ChevronRight, contentDescription = null)
}
}

// Table Header
Row(
modifier = Modifier
.fillMaxWidth()
.padding(horizontal = 12.dp)
.clip(RoundedCornerShape(8.dp))
.background(MaterialTheme.colorScheme.primaryContainer)
.padding(12.dp)
) {
Text(
text = stringResource(R.string.col_day),
modifier = Modifier.weight(1f),
fontWeight = FontWeight.Bold
)
TABLE_COLUMNS.forEach {
Text(
text = columnLabels[it] ?: it,
modifier = Modifier.weight(1f),
fontWeight = FontWeight.Bold
)
}
}

// Table Rows
LazyColumn(
modifier = Modifier.fillMaxSize()
) {
items(monthData) { item ->

val isToday = item.day == todayDay

Row(
modifier = Modifier
.fillMaxWidth()
.padding(horizontal = 12.dp, vertical = 6.dp)
.clip(RoundedCornerShape(8.dp))
.background(
if (isToday) MaterialTheme.colorScheme.secondaryContainer
else MaterialTheme.colorScheme.surfaceVariant
)
.padding(12.dp),
verticalAlignment = Alignment.CenterVertically
) {

Text(
text = item.day.toString(),
modifier = Modifier.weight(1f),
fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
)

TABLE_COLUMNS.forEach { col ->
Text(
text = item.times[col] ?: "-",
modifier = Modifier.weight(1f),
fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
)
}
}
}
}
}
}
