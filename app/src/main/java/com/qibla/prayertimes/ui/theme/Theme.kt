package com.qibla.prayertimes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun QiblaAppTheme(content: @Composable () -> Unit) {
    // Built fresh on every recomposition (not cached in a top-level val) so that toggling
    // ThemeState.isDark — read here — actually changes the resolved Material colors.
    val scheme = if (ThemeState.isDark) {
        darkColorScheme(
            primary = Brass,
            onPrimary = NightDeep,
            secondary = EmeraldAccent,
            background = NightMid,
            onBackground = AmberText,
            surface = NightSlate,
            onSurface = AmberText,
            error = RoseError
        )
    } else {
        lightColorScheme(
            primary = Brass,
            onPrimary = NightSlate,
            secondary = EmeraldAccent,
            background = NightMid,
            onBackground = AmberText,
            surface = NightSlate,
            onSurface = AmberText,
            error = RoseError
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
