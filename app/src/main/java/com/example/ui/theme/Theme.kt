package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EditorialDarkColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = PureBlack,
    primaryContainer = Color(0xFF223514),
    onPrimaryContainer = ElectricLime,
    secondary = VividPink,
    onSecondary = EditorialWhite,
    secondaryContainer = Color(0xFF381222),
    onSecondaryContainer = VividPink,
    tertiary = BrightCyan,
    onTertiary = PureBlack,
    tertiaryContainer = Color(0xFF0B2D38),
    onTertiaryContainer = BrightCyan,
    background = CharcoalBlack,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF262626),
    onBackground = EditorialWhite,
    onSurface = EditorialWhite,
    onSurfaceVariant = NeutralGray,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF3A3A3A)
)

private val EditorialLightColorScheme = lightColorScheme(
    primary = PureBlack,
    onPrimary = EditorialWhite,
    primaryContainer = ElectricLime,
    onPrimaryContainer = PureBlack,
    secondary = VividPink,
    onSecondary = EditorialWhite,
    secondaryContainer = Color(0xFFFFE0ED),
    onSecondaryContainer = VividPink,
    tertiary = ElectricBlue,
    onTertiary = EditorialWhite,
    tertiaryContainer = Color(0xFFD6E6FF),
    onTertiaryContainer = ElectricBlue,
    background = EditorialWhite,
    surface = EditorialWhite,
    surfaceVariant = Color(0xFFF2F2F2),
    onBackground = CharcoalBlack,
    onSurface = CharcoalBlack,
    onSurfaceVariant = Color(0xFF555555),
    outline = NeutralGray,
    outlineVariant = Color(0xFFE5E5E5)
)

@Composable
fun NearGridTheme(
    darkTheme: Boolean = true, // Default to stunning dark editorial aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) EditorialDarkColorScheme else EditorialLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    NearGridTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
