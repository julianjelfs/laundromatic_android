package com.julianjelfs.laundromatic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DarkBlue,
    secondary = SoftGreen,
    tertiary = WarmYellow,
    background = OffWhite,
    surface = OffWhite,
    error = SoftRed,
)

@Composable
fun LaundromaticTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
