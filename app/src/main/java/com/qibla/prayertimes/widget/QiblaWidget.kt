package com.qibla.prayertimes.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.qibla.prayertimes.MainActivity
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.WidgetDataStore
import com.qibla.prayertimes.data.WidgetSnapshot
import com.qibla.prayertimes.data.prayerLabels

// Solid colors only (no drawable ImageProvider backgrounds) — Glance's RemoteViews-backed
// background() with a plain ColorProvider is the most broadly compatible option; corner
// rounding is applied separately via cornerRadius().
private val bgColor = ColorProvider(Color(0xFF0C1A2B))
private val cellHighlightColor = ColorProvider(Color(0x33C9A15C))
private val cellPlainColor = ColorProvider(Color(0x14FFFFFF))
private val goldText = ColorProvider(Color(0xFFF2D8A0))
private val faintText = ColorProvider(Color(0x99F2D8A0))
private val whiteText = ColorProvider(Color(0xFFFFF6E5))
private val mainPrayers = listOf("Fajr", "Dhuhr", "Maghrib")
private val secondaryPrayers = listOf("Asr", "Isha")
private val cellWidth = 78.dp

class QiblaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = WidgetDataStore(context).load()
        provideContent {
            WidgetContent(snapshot)
        }
    }
}

@Composable
private fun WidgetContent(snapshot: WidgetSnapshot?) {
    val context = LocalContext.current
    val labels = prayerLabels(context)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .cornerRadius(20.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = snapshot?.cityName ?: context.getString(R.string.widget_default_title),
                style = TextStyle(color = whiteText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            )
        }
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = if (snapshot != null) {
                val offlineTag = if (snapshot.isOffline) context.getString(R.string.widget_offline_tag) else ""
                val datePart = listOf(snapshot.jalaliText, snapshot.hijriText).filter { it.isNotBlank() }.joinToString("   |   ")
                "$datePart$offlineTag"
            } else context.getString(R.string.widget_updating),
            style = TextStyle(color = faintText, fontSize = 11.sp)
        )
        Spacer(modifier = GlanceModifier.height(10.dp))

        if (snapshot != null) {
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                mainPrayers.forEachIndexed { index, key ->
                    if (index > 0) Spacer(modifier = GlanceModifier.width(6.dp))
                    PrayerCell(
                        label = labels[key] ?: key,
                        time = snapshot.timings[key] ?: "--:--",
                        highlighted = true
                    )
                }
            }
            Spacer(modifier = GlanceModifier.height(6.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                secondaryPrayers.forEachIndexed { index, key ->
                    if (index > 0) Spacer(modifier = GlanceModifier.width(6.dp))
                    PrayerCell(
                        label = labels[key] ?: key,
                        time = snapshot.timings[key] ?: "--:--",
                        highlighted = false
                    )
                }
            }
        } else {
            Text(
                text = context.getString(R.string.widget_open_app_hint),
                style = TextStyle(color = faintText, fontSize = 12.sp, textAlign = TextAlign.Center)
            )
        }
    }
}

@Composable
private fun PrayerCell(label: String, time: String, highlighted: Boolean) {
    Column(
        modifier = GlanceModifier
            .width(cellWidth)
            .background(if (highlighted) cellHighlightColor else cellPlainColor)
            .cornerRadius(12.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(text = label, style = TextStyle(color = faintText, fontSize = 10.sp))
        Text(text = time, style = TextStyle(color = goldText, fontWeight = FontWeight.Bold, fontSize = 15.sp))
    }
}
