package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ObsidianAuraColorScheme = darkColorScheme(
    primary = ElectricCobalt,
    onPrimary = PureWhite,
    primaryContainer = CobaltSoft,
    onPrimaryContainer = ObsidianInk,
    secondary = EmeraldMint,
    onSecondary = PureWhite,
    secondaryContainer = EmeraldSoft,
    onSecondaryContainer = ObsidianInk,
    tertiary = FieryCoral,
    onTertiary = PureWhite,
    tertiaryContainer = FieryCoralSoft,
    onTertiaryContainer = ObsidianInk,
    background = ObsidianCanvas,
    surface = DarkGlassCard,
    surfaceVariant = ElevatedGlassSurface,
    onBackground = ObsidianInk,
    onSurface = ObsidianInk,
    onSurfaceVariant = MutedSlate,
    outline = HairlineGlassBorder,
    outlineVariant = SubtleGlassDivider
)

val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Compatibility alias
val EditorialShapes = AuraShapes

@Composable
fun NearGridTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianAuraColorScheme,
        typography = Typography,
        shapes = AuraShapes,
        content = content
    )
}
