package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = QuettaNavyDark,
    onPrimary = Color.White,
    primaryContainer = QuettaNavy,
    onPrimaryContainer = QuettaYellow,
    secondary = QuettaYellow,
    onSecondary = QuettaNavyDark,
    secondaryContainer = Color(0xFFFEF9C3),
    onSecondaryContainer = QuettaNavyDark,
    tertiary = QuettaGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = QuettaBackgroundLight,
    onBackground = QuettaTextPrimary,
    surface = QuettaSurfaceLight,
    onSurface = QuettaTextPrimary,
    surfaceVariant = QuettaSurfaceVariantLight,
    onSurfaceVariant = QuettaTextSecondary,
    outline = QuettaBorderLight,
    error = QuettaRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = QuettaYellow,
    onPrimary = QuettaNavyDark,
    primaryContainer = QuettaSurfaceVariantDark,
    onPrimaryContainer = QuettaYellow,
    secondary = QuettaYellow,
    onSecondary = QuettaNavyDark,
    secondaryContainer = Color(0xFF423606),
    onSecondaryContainer = QuettaYellow,
    tertiary = QuettaGreen,
    onTertiary = Color.White,
    background = QuettaBackgroundDark,
    onBackground = Color(0xFFF8FAFC),
    surface = QuettaSurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = QuettaSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155),
    error = QuettaRed,
    onError = Color.White
)

@Composable
fun QuettaRideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Use the signature Quetta Ride light/navy-accent scheme by default for crisp brand consistency
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
