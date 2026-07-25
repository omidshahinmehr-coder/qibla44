package com.qibla.prayertimes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.HijriDate
import com.qibla.prayertimes.data.PRAYER_ORDER
import com.qibla.prayertimes.data.prayerLabels
import com.qibla.prayertimes.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date

private val highlighted = setOf("Fajr", "Dhuhr", "Maghrib")

@Composable
fun PrayerTimesCard(
    timings: Map<String, String>?,
    hijri: HijriDate?,
    loading: Boolean,
    error: String?,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val labels = prayerLabels(context)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.prayer_times_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (isOffline && timings != null) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33E5A3A3))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(stringResource(R.string.offline_approx_badge), color = Color(0xFFF0C9C9), fontSize = 9.sp)
                    }
                }
            }
            Text(
                SimpleDateFormat("d MMMM yyyy", context.resources.configuration.locales[0]).format(Date()),
                color = AmberMuted,
                fontSize = 11.sp
            )
        }

        if (hijri != null) {
            val eraSuffix = stringResource(R.string.hijri_era_suffix)
            val approxSuffix = if (isOffline) stringResource(R.string.approx_suffix) else ""
            Text(
                "${hijri.day} ${hijri.monthName(context)} ${hijri.year}$eraSuffix$approxSuffix",
                color = AmberFaint,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
        } else {
            Spacer(Modifier.height(10.dp))
        }

        when {
            loading -> Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = Brass, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.loading_times), color = AmberMuted, fontSize = 13.sp)
                }
            }

            error != null -> Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(error, color = RoseError, fontSize = 13.sp)
            }

            timings != null -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(340.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(PRAYER_ORDER) { key ->
                    val isHighlighted = key in highlighted
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isHighlighted) Color(0x1AC9A15C) else OverlayFaint)
                            .border(
                                1.dp,
                                if (isHighlighted) Color(0x40F2D8A0) else OverlayFaint,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(labels[key] ?: key, color = AmberMuted, fontSize = 11.sp)
                        Text(
                            timings[key] ?: "--:--",
                            color = AmberText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
