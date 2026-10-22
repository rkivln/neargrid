package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MinimalistColorScheme = lightColorScheme(
    primary = PrimaryText,
    onPrimary = PureWhite,
    primaryContainer = SoftSurface,
    onPrimaryContainer = PrimaryText,
    secondary = SlateBlue,
    onSecondary = PureWhite,
    secondaryContainer = BlueGray.copy(alpha = 0.2f),
    onSecondaryContainer = PrimaryText,
    tertiary = AccentLime,
    onTertiary = PrimaryText,
    tertiaryContainer = LightLime.copy(alpha = 0.4f),
    onTertiaryContainer = PrimaryText,
    background = BackgroundCanvas,
    surface = PrimarySurface,
    surfaceVariant = SoftSurface,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onSurfaceVariant = SecondaryText,
    outline = CardBorderColor,
    outlineVariant = SubtleDivider
)

private val MinimalistDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = DeepNavy,
    primaryContainer = SlateBlue,
    onPrimaryContainer = PureWhite,
    secondary = AccentLime,
    onSecondary = PrimaryText,
    secondaryContainer = DarkNavy,
    onSecondaryContainer = PureWhite,
    tertiary = LightLime,
    onTertiary = PrimaryText,
    tertiaryContainer = SlateBlue.copy(alpha = 0.5f),
    onTertiaryContainer = PureWhite,
    background = DeepNavy,
    surface = DarkNavy,
    surfaceVariant = SlateBlue,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onSurfaceVariant = BlueGray,
    outline = SlateBlue.copy(alpha = 0.4f),
    outlineVariant = BlueGray.copy(alpha = 0.2f)
)

@Composable
fun NearGridTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MinimalistDarkColorScheme else MinimalistColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
