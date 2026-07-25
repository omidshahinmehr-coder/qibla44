package com.qibla.prayertimes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qibla.prayertimes.R
import com.qibla.prayertimes.data.GeocodeResult
import com.qibla.prayertimes.data.GeocodingSearch
import com.qibla.prayertimes.model.City
import com.qibla.prayertimes.model.defaultCities
import com.qibla.prayertimes.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerSheet(
    selected: City,
    customCities: List<City>,
    onSelect: (City) -> Unit,
    onAddCity: (City) -> Unit,
    onRemoveCustom: (City) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var onlineResults by remember { mutableStateOf<List<GeocodeResult>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val geocoder = remember { GeocodingSearch() }
    val context = LocalContext.current
    val builtInCities = remember { defaultCities(context) }

    val allLocalCities = remember(customCities) { customCities + builtInCities }
    val localMatches = remember(query, allLocalCities) {
        if (query.isBlank()) emptyList()
        else allLocalCities.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            onlineResults = emptyList()
            searching = false
            return@LaunchedEffect
        }
        searching = true
        delay(600) // debounce so we don't fire a request on every keystroke
        val results = geocoder.search(context, query.trim())
        onlineResults = results
        searching = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NightSlate
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).heightIn(max = 560.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.city_picker_title), color = AmberText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = AmberMuted)
                }
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.city_search_placeholder), color = AmberFaint, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AmberMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AmberText,
                    unfocusedTextColor = AmberText,
                    focusedBorderColor = Brass,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = Brass
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))

            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                if (query.isNotBlank()) {
                    if (localMatches.isNotEmpty()) {
                        item {
                            Text(stringResource(R.string.city_local_results), color = AmberFaint, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp))
                        }
                        items(localMatches) { city ->
                            CityRow(
                                city = city,
                                isSelected = city.name == selected.name && city.lat == selected.lat,
                                onClick = { onSelect(city) },
                                onRemove = if (customCities.any { it.name == city.name && it.lat == city.lat }) {
                                    { onRemoveCustom(city) }
                                } else null
                            )
                        }
                    }

                    item { Spacer(Modifier.height(10.dp)) }
                    item {
                        Text(stringResource(R.string.city_online_search), color = AmberFaint, fontSize = 11.sp, modifier = Modifier.padding(vertical = 6.dp))
                    }
                    if (searching) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                                CircularProgressIndicator(color = Brass, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.city_searching), color = AmberMuted, fontSize = 12.sp)
                            }
                        }
                    } else if (onlineResults.isEmpty() && query.trim().length >= 2) {
                        item {
                            Text(
                                stringResource(R.string.city_no_results),
                                color = AmberFaint,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    } else {
                        items(onlineResults) { result ->
                            OnlineResultRow(result = result, onClick = {
                                onAddCity(City(shortenDisplayName(result.displayName), result.lat, result.lon))
                            })
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                } else {
                    if (customCities.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.city_my_cities),
                                color = AmberFaint,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        items(customCities) { city ->
                            CityRow(
                                city = city,
                                isSelected = city.name == selected.name && city.lat == selected.lat,
                                onClick = { onSelect(city) },
                                onRemove = { onRemoveCustom(city) }
                            )
                        }
                        item { Spacer(Modifier.height(10.dp)) }
                    }
                    item {
                        Text(
                            stringResource(R.string.city_default_cities),
                            color = AmberFaint,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                    items(builtInCities) { city ->
                        CityRow(
                            city = city,
                            isSelected = city.name == selected.name && city.lat == selected.lat,
                            onClick = { onSelect(city) },
                            onRemove = null
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

private fun shortenDisplayName(fullName: String): String =
    fullName.split(",").firstOrNull()?.trim()?.ifBlank { fullName } ?: fullName

@Composable
private fun CityRow(city: City, isSelected: Boolean, onClick: () -> Unit, onRemove: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0x4DC9A15C) else OverlayFaint)
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = if (isSelected) BrassLight else AmberFaint, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(city.name, color = if (isSelected) AmberText else AmberText.copy(alpha = 0.8f), fontSize = 14.sp)
        }
        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = RoseError.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun OnlineResultRow(result: GeocodeResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(OverlayFaint)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Public, contentDescription = null, tint = AmberFaint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            result.displayName,
            color = AmberText.copy(alpha = 0.85f),
            fontSize = 13.sp,
            maxLines = 2
        )
    }
}
