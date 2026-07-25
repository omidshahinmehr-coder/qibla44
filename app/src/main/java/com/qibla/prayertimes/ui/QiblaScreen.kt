package com.qibla.prayertimes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.PrayerTimesState
import com.qibla.prayertimes.ui.theme.*
import com.qibla.prayertimes.util.JalaliCalendar
import com.qibla.prayertimes.viewmodel.QiblaViewModel

@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel = viewModel(),
    onOpenAlarms: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenMonthly: () -> Unit = {}
) {
    val selected by viewModel.selectedCity.collectAsState()
    val customCities by viewModel.customCities.collectAsState()
    val prayerState by viewModel.prayerState.collectAsState()
    val locating by viewModel.locating.collectAsState()
    val context = LocalContext.current

    var showCityPicker by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(NightSlate, NightMid, NightDeep),
                    radius = 1400f
                )
            )
    ) {
        // Scales the dial down on small/old-device screens (e.g. 320dp-wide phones) instead of overflowing.
        val isCompact = maxWidth < 360.dp
        val dialSize = if (isCompact) (maxWidth * 0.62f).coerceAtMost(220.dp) else 260.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isCompact) 14.dp else 20.dp)
                .padding(top = 28.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with nav icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    IconButton(onClick = onOpenAlarms) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = stringResource(R.string.nav_alarms), tint = AmberMuted)
                    }
                    IconButton(onClick = onOpenMonthly) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = stringResource(R.string.nav_monthly), tint = AmberMuted)
                    }
                    IconButton(onClick = onOpenAbout) {
                        Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.nav_about), tint = AmberMuted)
                    }
                    IconButton(onClick = { com.qibla.prayertimes.ui.theme.ThemeState.toggle(context) }) {
                        Icon(
                            if (com.qibla.prayertimes.ui.theme.ThemeState.isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = stringResource(R.string.nav_theme_toggle),
                            tint = AmberMuted
                        )
                    }
                }
                Spacer(Modifier.width(1.dp))
            }

            Text(
                "QIBLA & AWQAT",
                color = AmberFaint,
                fontSize = 11.sp,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.home_title),
                color = AmberText,
                fontSize = if (isCompact) 22.sp else 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.home_subtitle),
                color = AmberMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Jalali (Persian-locale only) / Hijri date strip
            val language = context.resources.configuration.locales[0].language
            val jalaliText = if (language == "fa") remember { JalaliCalendar.today().toString() } else null
            val eraSuffix = stringResource(R.string.hijri_era_suffix)
            val hijriText = (prayerState as? PrayerTimesState.Success)?.result?.hijri
                ?.let { "${it.day} ${it.monthName(context)} ${it.year}$eraSuffix" }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = if (jalaliText != null) Arrangement.SpaceBetween else Arrangement.Center
            ) {
                if (jalaliText != null) {
                    Text(jalaliText, color = AmberText, fontSize = 12.sp)
                }
                Text(hijriText ?: "…", color = AmberMuted, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            // City selector card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(OverlayFaint)
                        .clickable { showCityPicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BrassLight, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(selected.name, color = AmberText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { viewModel.locateMe() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB9E4D8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.4f))
                    ) {
                        if (locating) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFFB9E4D8))
                        } else {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.compass_locate_me), fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { showMapPicker = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrassLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Brass.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.compass_add_from_map), fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Compass
            val bearing = viewModel.bearing.toFloat()
            val deviceHeading = com.qibla.prayertimes.sensor.rememberDeviceHeading()
            val needleAngle = if (deviceHeading != null) (bearing - deviceHeading + 360f) % 360f else bearing
            CompassDial(
                bearingDegrees = needleAngle,
                dialSize = dialSize,
                animationMillis = if (deviceHeading != null) 150 else 700,
                centerLabel = "${"%.1f".format(bearing)}°",
                captionText = if (deviceHeading != null)
                    stringResource(R.string.compass_live_caption)
                else
                    stringResource(R.string.compass_static_caption)
            )

            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.compass_distance, viewModel.distanceKm),
                color = AmberMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Prayer times
            val timings = (prayerState as? PrayerTimesState.Success)?.result?.timings
            val hijri = (prayerState as? PrayerTimesState.Success)?.result?.hijri
            val error = if (prayerState is PrayerTimesState.Error) stringResource(R.string.prayer_times_error) else null

            PrayerTimesCard(
                timings = timings,
                hijri = hijri,
                loading = prayerState is PrayerTimesState.Loading,
                error = error,
                isOffline = (prayerState as? PrayerTimesState.Success)?.result?.isOffline ?: false
            )

            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.method_note),
                color = AmberFaint,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        }
    }

    if (showCityPicker) {
        CityPickerSheet(
            selected = selected,
            customCities = customCities,
            onSelect = {
                viewModel.selectCity(it)
                showCityPicker = false
            },
            onAddCity = {
                viewModel.addCustomCity(it)
                showCityPicker = false
            },
            onRemoveCustom = { viewModel.removeCustomCity(it) },
            onDismiss = { showCityPicker = false }
        )
    }

    if (showMapPicker) {
        MapPickerDialog(
            onConfirm = {
                viewModel.addCustomCity(it)
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }
}
