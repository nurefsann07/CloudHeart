package com.example.havadurumu.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CloudHeartColors = lightColorScheme(
    primary = PinkDark,
    onPrimary = White,
    primaryContainer = PinkLight,
    onPrimaryContainer = PinkDark,
    secondary = BlueDark,
    onSecondary = White,
    secondaryContainer = BlueLight,
    onSecondaryContainer = BlueDark,
    background = Background,
    onBackground = Ink,
    surface = White,
    onSurface = Ink
)

@Composable
fun CloudHeartTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CloudHeartColors,
        typography = Typography,
        content = content
    )
}
