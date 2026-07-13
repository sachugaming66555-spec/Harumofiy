package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    secondary = MutedGray,
    tertiary = GradientLime,
    background = DeepBlack,
    surface = DarkCharcoal,
    onPrimary = DeepBlack,
    onSecondary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite,
    surfaceVariant = LightCharcoal,
    onSurfaceVariant = MutedGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for the true premium music experience
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful branded Spotify-green aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
