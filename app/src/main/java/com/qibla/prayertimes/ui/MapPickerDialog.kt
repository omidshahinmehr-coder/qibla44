package com.qibla.prayertimes.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qibla.prayertimes.R
import com.qibla.prayertimes.model.City
import com.qibla.prayertimes.ui.theme.*

private const val WORLD_MAP_URL =
    "https://upload.wikimedia.org/wikipedia/commons/8/83/Equirectangular_projection_SW.jpg"

private enum class PickMode { MAP, MANUAL }

@Composable
fun MapPickerDialog(
    onConfirm: (City) -> Unit,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(PickMode.MAP) }
    var pinOffset by remember { mutableStateOf<Offset?>(null) }
    var mapSize by remember { mutableStateOf(IntSize.Zero) }
    var cityName by remember { mutableStateOf("") }

    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }
    var manualError by remember { mutableStateOf<String?>(null) }

    val defaultCityName = stringResource(R.string.default_city_name)
    val errorInvalid = stringResource(R.string.manual_error_invalid)
    val errorLatRange = stringResource(R.string.manual_error_lat_range)
    val errorLonRange = stringResource(R.string.manual_error_lon_range)

    val pinLatLon = remember(pinOffset, mapSize) {
        val off = pinOffset
        if (off == null || mapSize.width == 0) null
        else {
            val lon = (off.x / mapSize.width) * 360.0 - 180.0
            val lat = 90.0 - (off.y / mapSize.height) * 180.0
            lat to lon
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NightSlate)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.map_add_city_title),
                    color = AmberText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = AmberMuted)
                }
            }
            Spacer(Modifier.height(6.dp))

            // Mode switch: map tap vs. typing coordinates directly (fully offline either way)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(OverlayMedium)
                    .padding(3.dp)
            ) {
                ModeChip(
                    text = stringResource(R.string.map_mode_map),
                    selected = mode == PickMode.MAP,
                    modifier = Modifier.weight(1f)
                ) { mode = PickMode.MAP }
                ModeChip(
                    text = stringResource(R.string.map_mode_manual),
                    selected = mode == PickMode.MANUAL,
                    modifier = Modifier.weight(1f)
                ) { mode = PickMode.MANUAL }
            }

            Spacer(Modifier.height(10.dp))

            if (mode == PickMode.MAP) {
                Text(
                    stringResource(R.string.map_tap_hint),
                    color = AmberMuted,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0A1622))
                        .onSizeChanged { mapSize = it }
                        .pointerInput(Unit) {
                            detectTapGestures { offset -> pinOffset = offset }
                        }
                ) {
                    // Offline-first: a lat/lon graticule is always drawn, so tapping works with no
                    // network at all. The real map photo (if internet is available) draws on top of
                    // it and simply fails silently when offline, leaving the grid visible underneath.
                    CoordinateGrid(modifier = Modifier.fillMaxSize())

                    AsyncImage(
                        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(WORLD_MAP_URL)
                            .build(),
                        contentDescription = stringResource(R.string.map_content_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    pinOffset?.let { off ->
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = BrassLight,
                            modifier = Modifier
                                .offset(
                                    x = with(density) { (off.x).toDp() } - 13.dp,
                                    y = with(density) { (off.y).toDp() } - 26.dp
                                )
                                .size(26.dp)
                        )
                    }
                }

                pinLatLon?.let { (lat, lon) ->
                    Spacer(Modifier.height(10.dp))
                    Text(
                        stringResource(R.string.map_coords_display, lat, lon),
                        color = AmberMuted,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    NameAndAddRow(
                        cityName = cityName,
                        onNameChange = { cityName = it },
                        onAdd = { onConfirm(City(cityName.ifBlank { defaultCityName }, lat, lon)) }
                    )
                }
            } else {
                Text(
                    stringResource(R.string.manual_hint),
                    color = AmberMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    CoordinateField(
                        value = latText,
                        onValueChange = { latText = it; manualError = null },
                        label = stringResource(R.string.manual_lat_label),
                        modifier = Modifier.weight(1f)
                    )
                    CoordinateField(
                        value = lonText,
                        onValueChange = { lonText = it; manualError = null },
                        label = stringResource(R.string.manual_lon_label),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (manualError != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(manualError!!, color = RoseError, fontSize = 11.sp)
                }
                Spacer(Modifier.height(12.dp))
                NameAndAddRow(
                    cityName = cityName,
                    onNameChange = { cityName = it },
                    onAdd = {
                        val lat = latText.trim().toDoubleOrNull()
                        val lon = lonText.trim().toDoubleOrNull()
                        when {
                            lat == null || lon == null -> manualError = errorInvalid
                            lat < -90.0 || lat > 90.0 -> manualError = errorLatRange
                            lon < -180.0 || lon > 180.0 -> manualError = errorLonRange
                            else -> onConfirm(City(cityName.ifBlank { defaultCityName }, lat, lon))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ModeChip(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0x4DC9A15C) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) AmberText else AmberMuted,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun CoordinateField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = AmberFaint, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AmberText,
            unfocusedTextColor = AmberText,
            focusedBorderColor = Brass,
            unfocusedBorderColor = CardBorder,
            cursorColor = Brass
        ),
        modifier = modifier
    )
}

@Composable
private fun NameAndAddRow(cityName: String, onNameChange: (String) -> Unit, onAdd: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = cityName,
            onValueChange = onNameChange,
            placeholder = { Text(stringResource(R.string.city_name_placeholder), color = AmberFaint) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AmberText,
                unfocusedTextColor = AmberText,
                focusedBorderColor = Brass,
                unfocusedBorderColor = CardBorder,
                cursorColor = Brass
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = NightDeep)
        ) {
            Text(stringResource(R.string.add_button), fontWeight = FontWeight.Bold)
        }
    }
}

/** Draws a simple lat/lon graticule so the map is tappable even with zero network access. */
@Composable
private fun CoordinateGrid(modifier: Modifier = Modifier) {
    val equatorLabel = stringResource(R.string.grid_equator_label)
    val northLabel = stringResource(R.string.grid_north_label)
    val southLabel = stringResource(R.string.grid_south_label)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val lineColor = Color(0x33F2D8A0)
        val majorLineColor = Color(0x66F2D8A0)

        val textPaint = Paint().apply {
            color = android.graphics.Color.argb(140, 242, 216, 160)
            textSize = 22f
            isAntiAlias = true
        }

        // Meridians every 30°
        var lon = -180
        while (lon <= 180) {
            val x = ((lon + 180) / 360f) * w
            val isPrime = lon == 0
            drawLine(
                color = if (isPrime) majorLineColor else lineColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = if (isPrime) 2f else 1f
            )
            lon += 30
        }

        // Parallels every 30°
        var lat = -90
        while (lat <= 90) {
            val y = ((90 - lat) / 180f) * h
            val isEquator = lat == 0
            drawLine(
                color = if (isEquator) majorLineColor else lineColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = if (isEquator) 2f else 1f
            )
            lat += 30
        }

        drawContext.canvas.nativeCanvas.apply {
            drawText(equatorLabel, w / 2f + 6f, h / 2f - 6f, textPaint)
            drawText(northLabel, 6f, 20f, textPaint)
            drawText(southLabel, 6f, h - 8f, textPaint)
        }
    }
}
