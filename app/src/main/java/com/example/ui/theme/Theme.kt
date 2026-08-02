package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanLight,
    onPrimary = SlateBackgroundDark,
    primaryContainer = CyanDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = VioletLight,
    onSecondary = SlateBackgroundDark,
    secondaryContainer = VioletSecondary,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = AmberTertiary,
    background = SlateBackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SlateSurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SlateSurfaceCardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = SlateBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = CyanLight,
    onPrimaryContainer = SlateBackgroundDark,
    secondary = VioletSecondary,
    onSecondary = TextPrimaryDark,
    tertiary = AmberTertiary,
    background = TextPrimaryDark,
    onBackground = SlateBackgroundDark,
    surface = SlateSurfaceCardDark,
    onSurface = TextPrimaryDark,
    outline = SlateBorder
)

@Composable
fun DevGateTheme(
    darkTheme: Boolean = true, // Default to dark developer theme for high tech contrast
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

